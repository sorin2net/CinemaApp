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
            // Creează service-ul
            RezervareService service = new RezervareService();

            // Creează sălile
            Sala salaMare = new Sala("Sala Mare", 5, 10);
            Sala salaMica = new Sala("Sala Mică", 4, 8);

            // Creează filmele
            Film inception = new Film("Inception", 148, "inception.jpg",
                    Arrays.asList("18:00", "21:00"), salaMare);

            Film interstellar = new Film("Interstellar", 169, "interstellar.jpg",
                    Arrays.asList("17:00", "20:00"), salaMica);

            // Adaugă filmele în service
            service.adaugaFilm(inception);
            service.adaugaFilm(interstellar);

            // Pornire GUI
            CinemaGUI gui = new CinemaGUI(service);
            gui.setVisible(true);
        });
    }
}
