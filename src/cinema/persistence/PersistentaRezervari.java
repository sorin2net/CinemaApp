package cinema.persistence;

import cinema.model.Sala;
import cinema.model.Scaun;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.*;
import java.time.LocalDate;

public class PersistentaRezervari {

    private static final String FILE_PATH = "data/rezervari.json";

    // Încarcă rezervările existente
    public static void incarcaRezervari(Sala sala, String film, LocalDate data, String oraFilm) {
        File file = new File(FILE_PATH);

        // Dacă JSON-ul nu există sau e gol => ștergem rezervările DB
        if (!file.exists() || file.length() == 0) {
            DatabaseManager.clearAllRezervari();
            return;
        }

        try (Reader reader = new FileReader(file)) {
            JSONArray rezervariArray = (JSONArray) new JSONParser().parse(reader);

            for (Object obj : rezervariArray) {
                JSONObject rez = (JSONObject) obj;
                String filmJson = (String) rez.get("film");
                String oraJson = (String) rez.get("ora");
                String salaJson = (String) rez.get("sala");
                long ziJson = ((Number) rez.get("zi")).longValue();
                long lunaJson = ((Number) rez.get("luna")).longValue();

                if (!filmJson.equals(film)) continue;
                if (!oraJson.equals(oraFilm)) continue;
                if (!salaJson.equals(sala.getNume())) continue;
                if (ziJson != data.getDayOfMonth()) continue;
                if (lunaJson != data.getMonthValue()) continue;

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

    // Salvează o rezervare nouă
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
            rez.put("luna", data.getMonthValue());
            rez.put("zi", data.getDayOfMonth());
            rez.put("ora", oraFilm);  // ora filmului
            rez.put("rand", rand);
            rez.put("coloana", coloana);
            rez.put("gen", gen);
            rez.put("varsta", varsta);

            rezervariArray.add(rez);

            try (Writer writer = new FileWriter(file)) {
                writer.write(rezervariArray.toJSONString());
            }

            // Sincronizare cu baza de date
            DatabaseManager.insertRezervare(
                    film,
                    gen,
                    varsta,
                    data.toString(),
                    oraFilm,   // ora filmului
                    rand,
                    coloana,
                    email
            );

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}