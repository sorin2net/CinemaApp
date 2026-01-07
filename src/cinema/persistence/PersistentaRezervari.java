package cinema.persistence;

import cinema.model.Sala;
import cinema.model.Scaun;
import java.io.*;
import java.time.LocalDate;
import java.util.Set;
import java.util.HashSet;

public class PersistentaRezervari {

    private static final String LOG_PATH = "logs/server.log";

    public static void salveazaRezervare(String film, Sala sala, LocalDate data, String oraFilm,
                                         int rand, int coloana, String email,
                                         String gen, int varsta) {
        try {
            DatabaseManager.insertRezervare(
                    film, gen, varsta, data.toString(), oraFilm, rand, coloana, email
            );

            logRezervare(film, sala.getNume(), data, oraFilm, rand, coloana, email);
        } catch (Exception e) {
            System.err.println("Eroare la salvarea rezervării: " + e.getMessage());
        }
    }

    public static Set<Scaun> stergeRezervare(String email, String film, LocalDate data, String oraFilm, Sala sala) {
        Set<Scaun> scauneSterse = new HashSet<>();
        try {
            for (int r = 0; r < sala.getRanduri(); r++) {
                for (int c = 0; c < sala.getColoane(); c++) {
                    Scaun scaun = sala.getScaun(r, c);

                    if (scaun.esteRezervat() && email.equals(scaun.getEmailRezervare())) {
                        DatabaseManager.stergeRezervare(email, film, data.toString(), oraFilm, r + 1, c + 1);

                        scaun.anuleazaRezervare();
                        scauneSterse.add(scaun);

                        logAnulare(film, sala.getNume(), data, oraFilm, r + 1, c + 1, email);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Eroare la anularea rezervării: " + e.getMessage());
        }
        return scauneSterse;
    }

    private static void logRezervare(String film, String sala, LocalDate data, String oraFilm, int rand, int coloana, String email) {
        writeLog(String.format("[%s] REZERVARE: film=%s, sala=%s, data=%s, ora=%s, loc=R%d-C%d, email=%s",
                java.time.LocalDateTime.now(), film, sala, data, oraFilm, rand, coloana, email));
    }

    private static void logAnulare(String film, String sala, LocalDate data, String oraFilm, int rand, int coloana, String email) {
        writeLog(String.format("[%s] ANULARE: film=%s, sala=%s, data=%s, ora=%s, loc=R%d-C%d, email=%s",
                java.time.LocalDateTime.now(), film, sala, data, oraFilm, rand, coloana, email));
    }

    private static void writeLog(String message) {
        try {
            File file = new File(LOG_PATH);
            file.getParentFile().mkdirs();
            try (PrintWriter pw = new PrintWriter(new FileWriter(file, true))) {
                pw.println(message);
            }
        } catch (IOException e) { e.printStackTrace(); }
    }
}