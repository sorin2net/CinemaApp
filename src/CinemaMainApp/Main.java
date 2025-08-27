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
            // Cream service-ul
            RezervareService service = new RezervareService();

            // Cream sălile
            Sala salaMare = new Sala("Sala Mare", 5, 10);   // 5 rânduri x 10 coloane
            Sala salaMica = new Sala("Sala Mică", 4, 8);    // 4 rânduri x 8 coloane

            // Cream filmele
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

            // Adaugam filmele in service
            service.adaugaFilm(inception);
            service.adaugaFilm(interstellar);

            // Pornim GUI-ul
            CinemaGUI gui = new CinemaGUI(service);
            gui.setVisible(true);
        });
    }
}
