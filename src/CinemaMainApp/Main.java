package CinemaMainApp;

import cinema.gui.CinemaGUI;
import cinema.model.Film;
import cinema.model.Sala;
import cinema.service.RezervareService;

import javax.swing.*;
import java.util.Arrays;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            // inițializăm service-ul
            RezervareService service = new RezervareService();

            // facem câteva săli
            Sala salaMare = new Sala("Sala Mare", 5, 5);
            Sala salaMica = new Sala("Sala Mică", 4, 4);

            // filme
            Film inception = new Film(
                    "Inception",
                    148,
                    "inception.jpg",
                    Arrays.asList("18:00", "21:00"),
                    salaMare
            );
            Film interstellar = new Film(
                    "Interstellar",
                    169,
                    "interstellar.jpg",
                    Arrays.asList("17:00", "20:00"),
                    salaMica
            );

            // adăugăm filmele în service
            service.adaugaFilm(inception);
            service.adaugaFilm(interstellar);

            // pornim GUI-ul
            CinemaGUI gui = new CinemaGUI(service);
            gui.setVisible(true);
        });
    }
}
