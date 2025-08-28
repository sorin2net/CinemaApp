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
    public static void incarcaRezervari(Sala sala, String film, LocalDate data, String ora) {
        File file = new File(FILE_PATH);
        if (!file.exists() || file.length() == 0) return;

        try (Reader reader = new FileReader(file)) {
            JSONArray rezervariArray = (JSONArray) new JSONParser().parse(reader);

            for (Object obj : rezervariArray) {
                JSONObject rez = (JSONObject) obj;
                String filmJson = (String) rez.get("film");
                String oraJson = (String) rez.get("ora");
                String salaJson = (String) rez.get("sala");
                long ziJson = (Long) rez.get("zi");
                long lunaJson = (Long) rez.get("luna");

                if (!filmJson.equals(film)) continue;
                if (!oraJson.equals(ora)) continue;
                if (!salaJson.equals(sala.getNume())) continue;
                if (ziJson != data.getDayOfMonth()) continue;
                if (lunaJson != data.getMonthValue()) continue;

                int rand = ((Long) rez.get("rand")).intValue() - 1;
                int coloana = ((Long) rez.get("coloana")).intValue() - 1;
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
    public static void salveazaRezervare(String film, Sala sala, LocalDate data, String ora,
                                         int rand, int coloana, String email) {
        try {
            File file = new File(FILE_PATH);
            file.getParentFile().mkdirs(); // creează folderul "data" dacă nu există
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
            rez.put("ora", ora);
            rez.put("rand", rand);
            rez.put("coloana", coloana);

            rezervariArray.add(rez);

            try (Writer writer = new FileWriter(file)) {
                writer.write(rezervariArray.toJSONString());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
