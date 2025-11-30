package cinema.persistence;

import cinema.model.Sala;
import cinema.model.Scaun;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.*;
import java.time.LocalDate;
import java.util.Set;
import java.util.HashSet;

public class PersistentaRezervari {

    private static final String FILE_PATH = "data/rezervari.json";
    private static final String LOG_PATH = "logs/server.log";

    public static void incarcaRezervari(Sala sala, String film, LocalDate data, String oraFilm) {
        File file = new File(FILE_PATH);
        if (!file.exists() || file.length() == 0) {
            return;
        }

        try (Reader reader = new FileReader(file)) {
            JSONArray rezervariArray = (JSONArray) new JSONParser().parse(reader);

            for (Object obj : rezervariArray) {
                JSONObject rez = (JSONObject) obj;
                String filmJson = (String) rez.get("film");
                String oraJson = (String) rez.get("ora");
                String salaJson = (String) rez.get("sala");

                // Verificăm data completă (an-lună-zi) în loc de doar zi și lună separate
                String dataJson = (String) rez.get("data");

                if (!filmJson.equals(film)) continue;
                if (!oraJson.equals(oraFilm)) continue;
                if (!salaJson.equals(sala.getNume())) continue;
                if (!dataJson.equals(data.toString())) continue; // Comparăm direct cu "2025-01-15"

                int rand = ((Number) rez.get("rand")).intValue() - 1;
                int coloana = ((Number) rez.get("coloana")).intValue() - 1;
                String email = (String) rez.get("email");

                Scaun scaun = sala.getScaun(rand, coloana);
                if (!scaun.esteRezervat()) {
                    scaun.rezerva(email);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void salveazaRezervare(String film, Sala sala, LocalDate data, String oraFilm,
                                         int rand, int coloana, String email,
                                         String gen, int varsta) {
        try {
            File file = new File(FILE_PATH);
            file.getParentFile().mkdirs();

            JSONArray rezervariArray = new JSONArray();
            if (file.exists() && file.length() > 0) {
                try (Reader reader = new FileReader(file)) {
                    Object parsed = new JSONParser().parse(reader);
                    if (parsed instanceof JSONArray) {
                        rezervariArray = (JSONArray) parsed;
                    }
                }
            }

            JSONObject rez = new JSONObject();
            rez.put("email", email);
            rez.put("film", film);
            rez.put("sala", sala.getNume());
            rez.put("data", data.toString()); // Salvăm data completă ca "2025-01-15"
            rez.put("ora", oraFilm);
            rez.put("rand", rand);
            rez.put("coloana", coloana);
            rez.put("gen", gen);
            rez.put("varsta", varsta);

            rezervariArray.add(rez);

            try (Writer writer = new FileWriter(file)) {
                writer.write(rezervariArray.toJSONString());
            }

            DatabaseManager.insertRezervare(
                    film,
                    gen,
                    varsta,
                    data.toString(),
                    oraFilm,
                    rand,
                    coloana,
                    email
            );

            logRezervare(film, sala.getNume(), data, oraFilm, rand, coloana, email);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Set<Scaun> stergeRezervare(String email, String film, LocalDate data, String oraFilm, Sala sala) {
        Set<Scaun> scauneSterse = new HashSet<>();
        File file = new File(FILE_PATH);
        if (!file.exists() || file.length() == 0) return scauneSterse;

        try {
            JSONArray rezervariArray;
            try (Reader reader = new FileReader(file)) {
                Object parsed = new JSONParser().parse(reader);
                rezervariArray = (parsed instanceof JSONArray) ? (JSONArray) parsed : new JSONArray();
            }

            JSONArray nouArray = new JSONArray();
            for (Object obj : rezervariArray) {
                JSONObject rez = (JSONObject) obj;
                String emailJson = (String) rez.get("email");
                String filmJson = (String) rez.get("film");
                String oraJson = (String) rez.get("ora");
                String salaJson = (String) rez.get("sala");
                String dataJson = (String) rez.get("data");

                boolean deStergere = emailJson.equals(email) &&
                        filmJson.equals(film) &&
                        oraJson.equals(oraFilm) &&
                        salaJson.equals(sala.getNume()) &&
                        dataJson.equals(data.toString()); // Comparăm data completă

                if (deStergere) {
                    int rand = ((Number) rez.get("rand")).intValue() - 1;
                    int coloana = ((Number) rez.get("coloana")).intValue() - 1;
                    Scaun scaun = sala.getScaun(rand, coloana);
                    if (scaun.esteRezervat()) {
                        scaun.anuleazaRezervare();
                        scauneSterse.add(scaun);
                    }
                    DatabaseManager.stergeRezervare(email, film, data.toString(), oraFilm, rand + 1, coloana + 1);

                    logAnulare(film, sala.getNume(), data, oraFilm, rand, coloana, email);
                } else {
                    nouArray.add(rez);
                }
            }

            try (Writer writer = new FileWriter(file)) {
                writer.write(nouArray.toJSONString());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return scauneSterse;
    }

    private static void logRezervare(String film, String sala, LocalDate data, String oraFilm,
                                     int rand, int coloana, String email) {
        try {
            File file = new File(LOG_PATH);
            file.getParentFile().mkdirs();

            try (FileWriter fw = new FileWriter(file, true);
                 PrintWriter pw = new PrintWriter(fw)) {
                pw.printf("[%s] Rezervare: film=%s, sala=%s, data=%s, ora=%s, rand=%d, coloana=%d, email=%s%n",
                        java.time.LocalDateTime.now(),
                        film, sala, data, oraFilm, rand, coloana, email);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void logAnulare(String film, String sala, LocalDate data, String oraFilm,
                                   int rand, int coloana, String email) {
        try {
            File file = new File(LOG_PATH);
            file.getParentFile().mkdirs();

            try (FileWriter fw = new FileWriter(file, true);
                 PrintWriter pw = new PrintWriter(fw)) {
                pw.printf("[%s] Anulare rezervare: film=%s, sala=%s, data=%s, ora=%s, rand=%d, coloana=%d, email=%s%n",
                        java.time.LocalDateTime.now(),
                        film, sala, data, oraFilm, rand, coloana, email);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}