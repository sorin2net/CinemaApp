package cinema.network;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import cinema.model.Film;
import cinema.model.Sala;
import cinema.model.Scaun;
import cinema.service.RezervareService;
import cinema.persistence.DatabaseManager;
import cinema.persistence.PersistentaRezervari;

import java.io.*;
import java.lang.reflect.Type;
import java.net.*;
import java.time.LocalDate;
import java.util.*;

public class ServerCinema {

    private int port;
    private List<PrintWriter> clienti = new ArrayList<>();
    private Gson gson = new Gson();
    private RezervareService service;

    public ServerCinema(int port, RezervareService service) {
        this.port = port;
        this.service = service;
    }

    public void start() throws IOException {
        ServerSocket serverSocket = new ServerSocket(port, 50, InetAddress.getByName("0.0.0.0"));
        System.out.println("Server Cinema rulează pe portul " + port);

        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface ni = interfaces.nextElement();
                Enumeration<InetAddress> addresses = ni.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress addr = addresses.nextElement();
                    if (!addr.isLoopbackAddress() && addr instanceof Inet4Address) {
                        System.out.println("Server disponibil la: " + addr.getHostAddress() + ":" + port);
                    }
                }
            }
        } catch (SocketException e) {
            System.out.println("Nu am putut obține IP-urile locale.");
        }

        while (true) {
            Socket socket = serverSocket.accept();
            System.out.println("Client conectat: " + socket.getInetAddress());

            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

            synchronized (clienti) {
                clienti.add(out);
            }

            new Thread(() -> handleClient(in, out, socket)).start();
        }
    }

    private void handleClient(BufferedReader in, PrintWriter out, Socket socket) {
        try {
            String line;
            while ((line = in.readLine()) != null) {
                Mesaj msg = gson.fromJson(line, Mesaj.class);

                if ("cerere_rezervare".equals(msg.tip)) {
                    Mesaj raspuns = proceseazaRezervare(msg);
                    out.println(gson.toJson(raspuns));
                    broadcastUpdateSalile();
                } else if ("cerere_anulare".equals(msg.tip)) {
                    Mesaj raspuns = proceseazaAnulare(msg);
                    if (raspuns != null) {
                        out.println(gson.toJson(raspuns));
                        broadcastUpdateSalile();
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Client deconectat: " + socket.getInetAddress());
        } finally {
            synchronized (clienti) {
                clienti.remove(out);
            }
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // METODĂ HELPER pentru a crea mesaje de eroare
    private Mesaj createErrorResponse(String mesajEroare) {
        Mesaj raspuns = new Mesaj();
        raspuns.tip = "raspuns";
        raspuns.status = "eroare";
        raspuns.mesaj = mesajEroare;
        return raspuns;
    }

    // METODĂ HELPER pentru a crea mesaje de succes
    private Mesaj createSuccessResponse(String mesajSucces) {
        Mesaj raspuns = new Mesaj();
        raspuns.tip = "raspuns";
        raspuns.status = "ok";
        raspuns.mesaj = mesajSucces;
        return raspuns;
    }

    // VERSIUNE ÎMBUNĂTĂȚITĂ cu validare și thread safety
    private synchronized Mesaj proceseazaRezervare(Mesaj cerere) {
        // Validare input
        if (cerere.film == null || cerere.film.trim().isEmpty()) {
            return createErrorResponse("Nume film lipsă!");
        }
        if (cerere.ora == null || cerere.ora.trim().isEmpty()) {
            return createErrorResponse("Ora lipsă!");
        }
        if (cerere.data == null || cerere.data.trim().isEmpty()) {
            return createErrorResponse("Data lipsă!");
        }
        if (cerere.email == null || cerere.email.trim().isEmpty()) {
            return createErrorResponse("Email lipsă!");
        }
        if (cerere.scaune == null || cerere.scaune.isEmpty()) {
            return createErrorResponse("Nu ați selectat niciun scaun!");
        }

        // Căutare film
        Film film = service.getFilme().stream()
                .filter(f -> f.getTitlu().equals(cerere.film))
                .findFirst().orElse(null);

        if (film == null) {
            return createErrorResponse("Film inexistent: " + cerere.film);
        }

        // Parsare dată
        LocalDate data;
        try {
            data = LocalDate.parse(cerere.data);
        } catch (Exception e) {
            return createErrorResponse("Format dată invalid: " + cerere.data);
        }

        // Obținere sală
        Sala sala = service.getSala(film, cerere.ora, data);
        if (sala == null) {
            return createErrorResponse("Sala nu este disponibilă!");
        }

        // VALIDARE: Verificăm dacă scaunele sunt disponibile
        Set<Scaun> scauneSelectate = new HashSet<>();
        List<String> scauneOcupate = new ArrayList<>();

        for (String s : cerere.scaune) {
            try {
                String[] parts = s.replace("R", "").split("-C");
                if (parts.length != 2) {
                    return createErrorResponse("Format scaun invalid: " + s);
                }

                int rand = Integer.parseInt(parts[0].trim()) - 1;
                int col = Integer.parseInt(parts[1].trim()) - 1;

                // Verificare limite
                if (rand < 0 || rand >= sala.getRanduri() || col < 0 || col >= sala.getColoane()) {
                    return createErrorResponse("Scaun în afara limitelor: " + s);
                }

                Scaun scaun = sala.getScaun(rand, col);

                if (scaun.esteRezervat()) {
                    scauneOcupate.add(s);
                } else {
                    scauneSelectate.add(scaun);
                }
            } catch (NumberFormatException e) {
                return createErrorResponse("Format scaun invalid: " + s);
            } catch (Exception e) {
                return createErrorResponse("Eroare la procesarea scaunului: " + s);
            }
        }

        // Dacă există scaune deja ocupate, returnăm eroare
        if (!scauneOcupate.isEmpty()) {
            Mesaj raspuns = new Mesaj();
            raspuns.tip = "raspuns";
            raspuns.status = "eroare";
            raspuns.mesaj = "Scaunele următoare sunt deja rezervate: " + String.join(", ", scauneOcupate);
            raspuns.scauneOcupate = scauneOcupate; // Câmp nou în Mesaj
            return raspuns;
        }

        // Rezervăm atomic toate scaunele
        try {
            for (Scaun scaun : scauneSelectate) {
                scaun.rezerva(cerere.email);
            }

            service.salveazaRezervare(film, cerere.ora, data, cerere.email, scauneSelectate, sala);

            System.out.println("[LOG] Rezervare efectuată: " + cerere.email + " - " + cerere.film +
                    " - " + cerere.ora + " - " + scauneSelectate.size() + " scaun(e)");

            return createSuccessResponse("Rezervare efectuată cu succes! " + scauneSelectate.size() + " scaun(e) rezervat(e).");
        } catch (Exception e) {
            // Rollback - anulăm rezervările în caz de eroare
            for (Scaun scaun : scauneSelectate) {
                scaun.anuleazaRezervare();
            }
            e.printStackTrace();
            return createErrorResponse("Eroare la salvarea rezervării: " + e.getMessage());
        }
    }

    private synchronized Mesaj proceseazaAnulare(Mesaj cerere) {
        // Validare input
        if (cerere.email == null || cerere.email.trim().isEmpty()) {
            return createErrorResponse("Email lipsă!");
        }
        if (cerere.film == null || cerere.film.trim().isEmpty()) {
            return createErrorResponse("Nume film lipsă!");
        }
        if (cerere.ora == null || cerere.ora.trim().isEmpty()) {
            return createErrorResponse("Ora lipsă!");
        }
        if (cerere.data == null || cerere.data.trim().isEmpty()) {
            return createErrorResponse("Data lipsă!");
        }

        LocalDate data;
        try {
            data = LocalDate.parse(cerere.data);
        } catch (Exception e) {
            return createErrorResponse("Format dată invalid: " + cerere.data);
        }

        String email = cerere.email.trim();
        String film = cerere.film.trim();
        String ora = cerere.ora.trim();

        Film filmObj = service.getFilme().stream()
                .filter(f -> f.getTitlu().equals(film))
                .findFirst().orElse(null);

        if (filmObj == null) {
            return createErrorResponse("Film inexistent: " + film);
        }

        try {
            // Obținem sala DUPĂ reset (pentru a sincroniza starea)
            Sala sala = service.getSala(filmObj, ora, data);

            // Ștergem rezervarea din JSON și actualizăm scaunele
            Set<Scaun> scauneSterse = PersistentaRezervari.stergeRezervare(email, film, data, ora, sala);

            Mesaj raspuns = new Mesaj();
            raspuns.tip = "raspuns_anulare";

            if (!scauneSterse.isEmpty()) {
                raspuns.status = "ok";
                raspuns.mesaj = "Rezervare anulată cu succes! " + scauneSterse.size() + " scaun(e) eliberat(e).";

                // Trimite email de confirmare anulare
                service.getEmailService().trimiteAnulare(email, film, sala.getNume(), ora, data);

                System.out.println("[LOG] Rezervare anulată: " + email + " - " + film +
                        " - " + ora + " - " + scauneSterse.size() + " scaun(e)");

                return raspuns;
            } else {
                raspuns.status = "eroare";
                raspuns.mesaj = "Nu există rezervare pentru acest film, ora și email.";
                return raspuns;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return createErrorResponse("Eroare la anularea rezervării: " + e.getMessage());
        }
    }

    private void broadcastUpdateSalile() {
        Mesaj update = new Mesaj();
        update.tip = "update_sali";
        update.mesaj = "Actualizare scaune ocupate";

        String json = gson.toJson(update);

        synchronized (clienti) {
            for (PrintWriter client : clienti) {
                client.println(json);
            }
        }

        System.out.println("[LOG] Broadcast trimis către " + clienti.size() + " client(i)");
    }

    public static void main(String[] args) throws IOException {
        RezervareService service = new RezervareService();

        DatabaseManager.createTableIfNotExists();

        try {
            String json = new String(java.nio.file.Files.readAllBytes(java.nio.file.Paths.get("resources/filme.json")));
            Gson gson = new Gson();
            Type listaFilmeType = new TypeToken<List<FilmJson>>() {}.getType();
            List<FilmJson> filmeJson = gson.fromJson(json, listaFilmeType);

            for (FilmJson fj : filmeJson) {
                Sala sala = new Sala(fj.sala.nume, fj.sala.randuri, fj.sala.coloane);
                Film film = new Film(fj.titlu, fj.durata, fj.imaginePath, fj.ore, fj.restrictieVarsta, fj.gen);

                List<Film.DataRulare> dateRulare = new ArrayList<>();
                for (DataRulareJson drj : fj.dateRulare) {
                    dateRulare.add(new Film.DataRulare(drj.luna, drj.zi));
                }

                film.setDateRulare(dateRulare);
                film.setSala(sala);
                service.adaugaFilm(film);
            }
            System.out.println("Filmele au fost încărcate pe server: " + filmeJson.size() + " filme");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Eroare la încărcarea resources/filme.json pe server!");
        }

        ServerCinema server = new ServerCinema(12345, service);
        server.start();
    }

    private static class FilmJson {
        String titlu;
        int durata;
        String imaginePath;
        List<String> ore;
        List<DataRulareJson> dateRulare;
        SalaJson sala;
        int restrictieVarsta;
        String gen;
    }

    private static class DataRulareJson {
        int luna;
        int zi;
    }

    private static class SalaJson {
        String nume;
        int randuri;
        int coloane;
    }
}