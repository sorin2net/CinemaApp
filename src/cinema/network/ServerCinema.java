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
        // Asculta pe toate interfetele (LAN și Internet)
        ServerSocket serverSocket = new ServerSocket(port, 50, InetAddress.getByName("0.0.0.0"));
        System.out.println("Server Cinema rulează pe portul " + port);

        // Afiasm IP-urile locale disponibile
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
            clienti.add(out);

            new Thread(() -> handleClient(in, out)).start();
        }
    }


    private void handleClient(BufferedReader in, PrintWriter out) {
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
                    out.println(gson.toJson(raspuns));
                    broadcastUpdateSalile();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private synchronized Mesaj proceseazaRezervare(Mesaj cerere) {
        Film film = service.getFilme().stream()
                .filter(f -> f.getTitlu().equals(cerere.film))
                .findFirst().orElse(null);

        if (film == null) {
            Mesaj raspuns = new Mesaj();
            raspuns.tip = "raspuns";
            raspuns.status = "eroare";
            raspuns.mesaj = "Film inexistent!";
            return raspuns;
        }

        LocalDate data = LocalDate.parse(cerere.data);
        Sala sala = service.getSala(film, cerere.ora, data);

        Set<Scaun> scauneSelectate = new HashSet<>();
        for (String s : cerere.scaune) {
            String[] parts = s.replace("R", "").split("-C");
            int rand = Integer.parseInt(parts[0]) - 1;
            int col  = Integer.parseInt(parts[1]) - 1;
            Scaun scaun = sala.getScaun(rand, col);
            if (!scaun.esteRezervat()) {
                scaun.rezerva(cerere.email);
                scauneSelectate.add(scaun);
            }
        }

        service.salveazaRezervare(film, cerere.ora, data, cerere.email, scauneSelectate, sala);

        Mesaj raspuns = new Mesaj();
        raspuns.tip = "raspuns";
        raspuns.status = "ok";
        raspuns.mesaj = "Rezervare efectuată cu succes!";
        return raspuns;
    }

    private synchronized Mesaj proceseazaAnulare(Mesaj cerere) {
        LocalDate data = LocalDate.parse(cerere.data);
        String email = cerere.email;

        List<Film> filmeZi = service.getFilmePentruZi(data);
        boolean rezervareGasita = false;

        for (Film film : filmeZi) {
            for (String ora : film.getOre()) {
                Sala sala = service.getSala(film, ora, data);
                Set<Scaun> scauneDeAnulat = new HashSet<>();

                // Gasește toate scaunele rezervate de acest email
                for (int r = 0; r < sala.getRanduri(); r++) {
                    for (int c = 0; c < sala.getColoane(); c++) {
                        Scaun sc = sala.getScaun(r, c);
                        if (sc.esteRezervat() && email.equals(sc.getEmailRezervare())) {
                            scauneDeAnulat.add(sc);
                        }
                    }
                }

                if (!scauneDeAnulat.isEmpty()) {
                    rezervareGasita = true;

                    // marcheaza scaunele ca libere
                    for (Scaun sc : scauneDeAnulat) {
                        sc.anuleazaRezervare();
                    }

                    // sterge rezervarile din JSON și DB
                    PersistentaRezervari.stergeRezervare(film.getTitlu(), sala.getNume(), data, ora, sala);

                    // trimite email de confirmare anulare
                    service.getEmailService().trimiteAnulare(email, film.getTitlu(), sala.getNume(), ora, data);

                    System.out.println("[LOG] Rezervare anulată pentru email: " + email + " data: " + data);
                }
            }
        }

        // pregatește mesajul de raspuns către client
        Mesaj raspuns = new Mesaj();
        raspuns.tip = "raspuns_anulare";
        if (rezervareGasita) {
            raspuns.status = "ok";
            raspuns.mesaj = "Rezervarea a fost anulată cu succes!";
        } else {
            raspuns.status = "eroare";
            raspuns.mesaj = "Nu s-au găsit rezervări pentru email-ul și data introduse.";
        }
        return raspuns;
    }


    private void broadcastUpdateSalile() {
        Mesaj update = new Mesaj();
        update.tip = "update_sali";
        update.mesaj = "Actualizare scaune ocupate";

        String json = gson.toJson(update);
        for (PrintWriter client : clienti) {
            client.println(json);
        }
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
                film.setZile(fj.zile);
                film.setSala(sala);
                service.adaugaFilm(film);
            }
            System.out.println("Filmele au fost încărcate pe server.");
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
        List<Integer> zile;
        SalaJson sala;
        int restrictieVarsta;
        String gen;
    }

    private static class SalaJson {
        String nume;
        int randuri;
        int coloane;
    }
}
