package CinemaMainApp;

import cinema.model.Sala;
import cinema.model.Scaun;

public class Main {
    public static void main(String[] args) {
        // Creăm o sală cu 5 rânduri și 5 coloane
        Sala sala = new Sala("Sala Mare", 5, 5);

        // Rezervăm câteva scaune
        sala.getScaun(0, 0).rezerva(); // primul rând, prima coloană
        sala.getScaun(2, 3).rezerva(); // rândul 3, coloana 4

        // Afișăm starea sălii
        for (int i = 0; i < sala.getRanduri(); i++) {
            for (int j = 0; j < sala.getColoane(); j++) {
                Scaun scaun = sala.getScaun(i, j);
                System.out.print(scaun.esteRezervat() ? "[X] " : "[ ] ");
            }
            System.out.println();
        }
    }
}
