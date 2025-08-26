package CinemaMainApp;

import cinema.model.Film;
import cinema.model.Sala;
import cinema.model.Scaun;
import cinema.persistence.PersistentaRezervari;

import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        // Creăm filme și ore
        Film film1 = new Film("Inception", 148, "inception.jpg", Arrays.asList("18:00", "21:00"));
        Film film2 = new Film("Interstellar", 169, "interstellar.jpg", Arrays.asList("17:00", "20:00"));

        List<Film> filme = Arrays.asList(film1, film2);

        // Creăm o sală 5x5
        Sala sala = new Sala("Sala Mare", 5, 5);

        Scanner scanner = new Scanner(System.in);

        // Alegerea filmului
        System.out.println("Filme disponibile:");
        for (int i = 0; i < filme.size(); i++) {
            System.out.println((i + 1) + ". " + filme.get(i).getTitlu() + " (" + filme.get(i).getDurata() + " min)");
        }

        System.out.print("Alege filmul (introdu numărul): ");
        int alegereFilm = scanner.nextInt();
        if (alegereFilm < 1 || alegereFilm > filme.size()) {
            System.out.println("Alegere invalidă!");
            return;
        }

        Film filmSelectat = filme.get(alegereFilm - 1);

        // Alegerea orei
        System.out.println("Ore disponibile:");
        List<String> ore = filmSelectat.getOre();
        for (int i = 0; i < ore.size(); i++) {
            System.out.println((i + 1) + ". " + ore.get(i));
        }

        System.out.print("Alege ora (introdu numărul): ");
        int alegereOra = scanner.nextInt();
        if (alegereOra < 1 || alegereOra > ore.size()) {
            System.out.println("Alegere invalidă!");
            return;
        }

        String oraSelectata = ore.get(alegereOra - 1);

        // Introducem email-ul utilizatorului
        System.out.print("Introdu email-ul tău: ");
        String email = scanner.next();

        // Încărcăm rezervările existente
        PersistentaRezervari.incarcaRezervari(sala, filmSelectat.getTitlu(), oraSelectata);

        // Afișăm starea sălii
        afisareSala(sala);

        // Citirea scaunelor de rezervat
        while (true) {
            System.out.print("Introdu randul și coloana scaunului de rezervat (-1 -1 pentru a termina, indexare de la 1): ");
            int rand = scanner.nextInt();
            int coloana = scanner.nextInt();

            if (rand == -1 && coloana == -1) break;

            // validare input uman (de la 1)
            if (rand >= 1 && rand <= sala.getRanduri() && coloana >= 1 && coloana <= sala.getColoane()) {
                // conversie la index intern (de la 0)
                Scaun scaun = sala.getScaun(rand - 1, coloana - 1);
                if (!scaun.esteRezervat()) {
                    scaun.rezerva();
                    // salvăm cu rand și coloană umane (nu cu -1)
                    PersistentaRezervari.salveazaRezervare(
                            filmSelectat.getTitlu(),
                            oraSelectata,
                            rand,
                            coloana,
                            email
                    );
                    System.out.println("Scaun rezervat!");
                } else {
                    System.out.println("Scaun deja rezervat!");
                }
            } else {
                System.out.println("Coordonate invalide!");
            }

            afisareSala(sala);
        }

        System.out.println("Rezervările pentru filmul " + filmSelectat.getTitlu() +
                " la ora " + oraSelectata + " au fost salvate.");
        scanner.close();
    }

    public static void afisareSala(Sala sala) {
        System.out.println("\nStarea sălii (X = rezervat, [ ] = liber):");

        // afișăm cu indexare de la 1
        System.out.print("    ");
        for (int j = 0; j < sala.getColoane(); j++) {
            System.out.print((j + 1) + "   ");
        }
        System.out.println();

        for (int i = 0; i < sala.getRanduri(); i++) {
            System.out.print((i + 1) + " ");
            for (int j = 0; j < sala.getColoane(); j++) {
                Scaun scaun = sala.getScaun(i, j);
                System.out.print(scaun.esteRezervat() ? "[X] " : "[ ] ");
            }
            System.out.println();
        }
        System.out.println();
    }
}
