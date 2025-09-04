package cinema.network;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import cinema.model.Film;
import cinema.model.Sala;
import cinema.model.Scaun;
import cinema.service.RezervareService;
import cinema.persistence.DatabaseManager;

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
        ServerSocket serverSocket = new ServerSocket(port);
        System.out.println("Server Cinema rulează pe portul " + port);

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
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private synchronized Mesaj proceseazaRezervare(Mesaj cerere) {
        // 1) film + sală
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

        // 2) scaune selectate
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

        // 3) AICI se salvează în JSON + DB prin service
        service.salveazaRezervare(film, cerere.ora, data, cerere.email, scauneSelectate, sala);

        Mesaj raspuns = new Mesaj();
        raspuns.tip = "raspuns";
        raspuns.status = "ok";
        raspuns.mesaj = "Rezervare efectuată cu succes!";
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

        // Creează tabelul DB dacă nu există
        DatabaseManager.createTableIfNotExists();

        // Încarcă filmele din JSON
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

    // Clase pentru citirea JSON-ului
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
