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
        scanner.nextLine(); // consumăm linia rămasă după nextInt()
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
        scanner.nextLine(); // consumăm linia rămasă
        if (alegereOra < 1 || alegereOra > ore.size()) {
            System.out.println("Alegere invalidă!");
            return;
        }

        String oraSelectata = ore.get(alegereOra - 1);

        // Introducem email-ul utilizatorului
        System.out.print("Introdu email-ul tău: ");
        String email = scanner.nextLine().trim();

        // Încărcăm rezervările existente
        PersistentaRezervari.incarcaRezervari(sala, filmSelectat.getTitlu(), oraSelectata);

        // Afișăm starea sălii
        afisareSala(sala);

        // Citirea scaunelor de rezervat (0-based)
        while (true) {
            System.out.print("Introdu randul și coloana scaunului de rezervat (-1 -1 pentru a termina): ");
            String linie = scanner.nextLine().trim();

            if (linie.isEmpty()) continue;

            String[] parti = linie.split("\\s+");
            if (parti.length != 2) {
                System.out.println("Introdu exact două numere separate prin spațiu!");
                continue;
            }

            int rand, coloana;
            try {
                rand = Integer.parseInt(parti[0]);
                coloana = Integer.parseInt(parti[1]);
            } catch (NumberFormatException e) {
                System.out.println("Trebuie să introduci numere întregi!");
                continue;
            }

            if (rand == -1 && coloana == -1) break;

            if (rand >= 0 && rand < sala.getRanduri() && coloana >= 0 && coloana < sala.getColoane()) {
                Scaun scaun = sala.getScaun(rand, coloana);
                if (!scaun.esteRezervat()) {
                    scaun.rezerva();
                    PersistentaRezervari.salveazaRezervare(filmSelectat.getTitlu(), oraSelectata, rand, coloana, email);
                    System.out.println("Scaun rezervat!");
                } else {
                    System.out.println("Scaun deja rezervat!");
                }
            } else {
                System.out.println("Coordonate invalide!");
            }

            afisareSala(sala);
        }

        System.out.println("Rezervările tale pentru filmul " + filmSelectat.getTitlu() + " la ora " + oraSelectata + " au fost salvate.");
        scanner.close();
    }

    public static void afisareSala(Sala sala) {
        System.out.println("\nStarea sălii (X = rezervat, [ ] = liber):");
        for (int i = 0; i < sala.getRanduri(); i++) {
            for (int j = 0; j < sala.getColoane(); j++) {
                Scaun scaun = sala.getScaun(i, j);
                System.out.print(scaun.esteRezervat() ? "[X] " : "[ ] ");
            }
            System.out.println();
        }
        System.out.println();
    }
}
