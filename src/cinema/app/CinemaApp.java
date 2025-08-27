package cinema.app;

import cinema.gui.CinemaGUI;
import cinema.model.Film;
import cinema.model.Sala;
import cinema.service.RezervareService;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import javax.swing.*;

public class CinemaApp {

    public CinemaApp() {
        SwingUtilities.invokeLater(() -> {
            RezervareService service = new RezervareService();

            try {
                // Citim JSON-ul
                String json = new String(Files.readAllBytes(Paths.get("resources/filme.json")));
                Gson gson = new Gson();

                Type listaFilmeType = new TypeToken<List<FilmJson>>(){}.getType();
                List<FilmJson> filmeJson = gson.fromJson(json, listaFilmeType);

                // Convertim în obiecte Film și Sala
                for (FilmJson fj : filmeJson) {
                    Sala sala = new Sala(fj.sala.nume, fj.sala.randuri, fj.sala.coloane);
                    Film film = new Film(fj.titlu, fj.durata, fj.imaginePath, fj.ore, sala);
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
        SalaJson sala;
    }

    private static class SalaJson {
        String nume;
        int randuri;
        int coloane;
    }
}
