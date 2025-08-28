package cinema.app;

import cinema.gui.CinemaGUI;
import cinema.model.Film;
import cinema.model.Sala;
import cinema.service.RezervareService;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import javax.swing.*;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class CinemaApp {

    public CinemaApp() {
        SwingUtilities.invokeLater(() -> {
            RezervareService service = new RezervareService();

            try {
                // Citim JSON-ul
                String json = new String(Files.readAllBytes(Paths.get("resources/filme.json")));
                Gson gson = new Gson();

                // Mapăm lista de filme din JSON
                Type listaFilmeType = new TypeToken<List<FilmJson>>() {}.getType();
                List<FilmJson> filmeJson = gson.fromJson(json, listaFilmeType);

                // Convertim în obiecte Film și Sala
                for (FilmJson fj : filmeJson) {
                    Sala sala = new Sala(fj.sala.nume, fj.sala.randuri, fj.sala.coloane);
                    Film film = new Film(
                            fj.titlu,
                            fj.durata,
                            fj.imaginePath,
                            fj.ore,
                            fj.restrictieVarsta,
                            fj.gen
                    );

                    // Păstrăm lista de zile și sala pentru rezervări
                    film.setZile(fj.zile);
                    film.setSala(sala);

                    service.adaugaFilm(film);
                }

            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, "Eroare la încărcarea fișierului filme.json");
            }

            // Pornim GUI-ul
            CinemaGUI gui = new CinemaGUI(service);
            gui.setVisible(true);
        });
    }

    // Clase interne pentru maparea JSON
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

    public static void main(String[] args) {
        new CinemaApp();
    }
}
