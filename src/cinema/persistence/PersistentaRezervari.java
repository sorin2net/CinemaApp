package cinema.persistence;

import cinema.model.Sala;
import cinema.model.Scaun;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.*;

public class PersistentaRezervari {

    private static final String FILE_PATH = "data/rezervari.json";

    // Încarcă rezervările pentru un anumit film și oră
    public static void incarcaRezervari(Sala sala, String film, String ora) {
        File file = new File(FILE_PATH);
        if (!file.exists() || file.length() == 0) return;

        try (Reader reader = new FileReader(file)) {
            JSONArray rezervariArray = (JSONArray) new JSONParser().parse(reader);

            for (Object obj : rezervariArray) {
                JSONObject rez = (JSONObject) obj;
                String filmJson = (String) rez.get("film");
                String oraJson = (String) rez.get("ora");

                if (!filmJson.equals(film) || !oraJson.equals(ora)) continue;

                int rand = ((Long) rez.get("rand")).intValue();
                int coloana = ((Long) rez.get("coloana")).intValue();

                Scaun scaun = sala.getScaun(rand, coloana);
                if (!scaun.esteRezervat()) {
                    scaun.rezerva();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Salvează o rezervare nouă
    public static void salveazaRezervare(String film, String ora, int rand, int coloana, String email) {
        JSONArray rezervariArray = new JSONArray();
        File file = new File(FILE_PATH);

        try {
            // Încarcă array-ul existent dacă fișierul există și nu e gol
            if (file.exists() && file.length() > 0) {
                try (Reader reader = new FileReader(file)) {
                    Object parsed = new JSONParser().parse(reader);
                    if (parsed instanceof JSONArray) {
                        rezervariArray = (JSONArray) parsed;
                    } else if (parsed instanceof JSONObject) {
                        // dacă era doar un obiect, îl punem într-un array
                        rezervariArray.add(parsed);
                    }
                }
            }

            // Cream obiectul JSON cu ordinea dorită: email, film, ora, rand, coloana
            JSONObject rez = new JSONObject();
            rez.put("email", email);
            rez.put("film", film);
            rez.put("ora", ora);
            rez.put("rand", rand);
            rez.put("coloana", coloana);

            rezervariArray.add(rez);

            // Salvăm întreg array-ul în fișier
            try (Writer writer = new FileWriter(file)) {
                writer.write(rezervariArray.toJSONString());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
