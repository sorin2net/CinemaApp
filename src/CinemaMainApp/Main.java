package CinemaMainApp;

import cinema.model.Film;
import cinema.model.Sala;
import cinema.model.Scaun;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        // Creăm un film cu ore de difuzare
        Film film1 = new Film("Inception", 148, "inception.jpg", Arrays.asList("18:00", "21:00"));

        // Creăm o sală 5x5
        Sala sala = new Sala("Sala Mare", 5, 5);

        // Afișăm filmul și orele disponibile
        System.out.println("Filmul: " + film1.getTitlu() + " (" + film1.getDurata() + " min)");
        System.out.println("Ore disponibile:");
        List<String> ore = film1.getOre();
        for (int i = 0; i < ore.size(); i++) {
            System.out.println((i + 1) + ". " + ore.get(i));
        }

        // Citim ora aleasă
        Scanner scanner = new Scanner(System.in);
        System.out.print("Alege ora (introdu numărul): ");
        int alegereOra = scanner.nextInt();

        if (alegereOra < 1 || alegereOra > ore.size()) {
            System.out.println("Alegere invalidă.");
            return;
        }

        String oraSelectata = ore.get(alegereOra - 1);
        System.out.println("Ai ales ora: " + oraSelectata);

        // Afisam sala cu scaune libere
        System.out.println("\nStarea sălii (X = rezervat, [ ] = liber):");
        afisareSala(sala);

        // Citim scaunele de rezervat
        while (true) {
            System.out.print("Introdu randul și coloana scaunului de rezervat (0 pentru a termina): ");
            int rand = scanner.nextInt();
            int coloana = scanner.nextInt();

            if (rand == 0 || coloana == 0) break; // termină rezervările

            if (rand > 0 && rand <= sala.getRanduri() && coloana > 0 && coloana <= sala.getColoane()) {
                Scaun scaun = sala.getScaun(rand - 1, coloana - 1);
                if (!scaun.esteRezervat()) {
                    scaun.rezerva();
                    System.out.println("Scaun rezervat!");
                } else {
                    System.out.println("Scaun deja rezervat!");
                }
            } else {
                System.out.println("Coordonate invalide!");
            }

            // afișăm sala după fiecare rezervare
            afisareSala(sala);
        }

        System.out.println("Rezervările au fost înregistrate pentru ora " + oraSelectata + ".");
        scanner.close();
    }

    public static void afisareSala(Sala sala) {
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
