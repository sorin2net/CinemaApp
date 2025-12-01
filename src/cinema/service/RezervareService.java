package cinema.service;

import cinema.model.Film;
import cinema.model.Sala;
import cinema.model.Scaun;
import cinema.persistence.PersistentaRezervari;

import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class RezervareService {
    private List<Film> filme = new ArrayList<>();
    // SCHIMBAT: Folosim ConcurrentHashMap pentru thread safety
    private Map<String, Map<String, Sala>> saliPeZiOra = new ConcurrentHashMap<>();

    private final EmailService emailService = new EmailService();

    public void adaugaFilm(Film film) {
        filme.add(film);
        Map<String, Sala> mapZiOra = new ConcurrentHashMap<>();

        System.out.println("Adăugare film: " + film.getTitlu());

        for (Film.DataRulare dr : film.getDateRulare()) {
            for (String ora : film.getOre()) {
                String key = dr.getLuna() + "-" + dr.getZi() + "-" + ora;
                mapZiOra.put(key, film.getSala().cloneSala());
                System.out.println("  Date rulare: luna=" + dr.getLuna() + ", zi=" + dr.getZi() + ", ora=" + ora);
            }
        }

        saliPeZiOra.put(film.getTitlu(), mapZiOra);
    }

    public List<Film> getFilme() {
        return new ArrayList<>(filme); // Returnăm o copie pentru thread safety
    }

    public EmailService getEmailService() {
        return emailService;
    }

    public List<Film> getFilmePentruZi(LocalDate data) {
        List<Film> result = new ArrayList<>();
        System.out.println("Căutare filme pentru data: " + data + " (luna=" + data.getMonthValue() + ", zi=" + data.getDayOfMonth() + ")");

        for (Film f : filme) {
            boolean ruleaza = f.ruleazaLaData(data);
            System.out.println("  Film: " + f.getTitlu() + " - rulează: " + ruleaza);
            if (ruleaza) {
                result.add(f);
            }
        }

        System.out.println("Găsite " + result.size() + " filme pentru " + data);
        return result;
    }

    // VERSIUNE ÎMBUNĂTĂȚITĂ - Fix pentru bug-ul de sincronizare
    public synchronized Sala getSala(Film film, String oraFilm, LocalDate data) {
        Map<String, Sala> mapZiOra = saliPeZiOra.get(film.getTitlu());
        if (mapZiOra == null) {
            System.err.println("EROARE: Nu există mapare pentru filmul " + film.getTitlu());
            return null;
        }

        String key = data.getMonthValue() + "-" + data.getDayOfMonth() + "-" + oraFilm;

        // Dacă sala nu există pentru această combinație, o creăm
        if (!mapZiOra.containsKey(key)) {
            Sala nouaSala = film.getSala().cloneSala();
            mapZiOra.put(key, nouaSala);
            System.out.println("Sala nouă creată pentru: " + key);
        }

        Sala sala = mapZiOra.get(key);

        // IMPORTANT FIX: Nu mai resetăm toate scaunele!
        // În loc să resetăm, doar încărcăm starea din persistență
        // Scaunele vor fi actualizate doar dacă există modificări în JSON

        // Creăm un set cu scaunele care ar trebui să fie rezervate conform JSON
        Set<String> scauneRezervateInJSON = getScauneRezervateFromJSON(film.getTitlu(), data, oraFilm, sala.getNume());

        // Sincronizăm starea sălii cu JSON-ul
        for (int r = 0; r < sala.getRanduri(); r++) {
            for (int c = 0; c < sala.getColoane(); c++) {
                Scaun scaun = sala.getScaun(r, c);
                String scaunKey = "R" + (r + 1) + "-C" + (c + 1);

                if (scauneRezervateInJSON.contains(scaunKey)) {
                    // Scaunul trebuie să fie rezervat
                    if (!scaun.esteRezervat()) {
                        // Trebuie să îl rezervăm
                        String email = getEmailPentruScaun(film.getTitlu(), data, oraFilm, r + 1, c + 1);
                        if (email != null) {
                            scaun.rezerva(email);
                        }
                    }
                } else {
                    // Scaunul trebuie să fie liber
                    if (scaun.esteRezervat()) {
                        scaun.anuleazaRezervare();
                    }
                }
            }
        }

        return sala;
    }

    // Metodă helper pentru a obține scaunele rezervate din JSON
    private Set<String> getScauneRezervateFromJSON(String film, LocalDate data, String ora, String numeSala) {
        Set<String> scaune = new HashSet<>();

        try {
            java.io.File file = new java.io.File("data/rezervari.json");
            if (!file.exists() || file.length() == 0) {
                return scaune;
            }

            try (java.io.Reader reader = new java.io.FileReader(file)) {
                org.json.simple.JSONArray rezervariArray = (org.json.simple.JSONArray) new org.json.simple.parser.JSONParser().parse(reader);

                for (Object obj : rezervariArray) {
                    org.json.simple.JSONObject rez = (org.json.simple.JSONObject) obj;
                    String filmJson = (String) rez.get("film");
                    String oraJson = (String) rez.get("ora");
                    String salaJson = (String) rez.get("sala");
                    String dataJson = (String) rez.get("data");

                    if (filmJson.equals(film) && oraJson.equals(ora) &&
                            salaJson.equals(numeSala) && dataJson.equals(data.toString())) {

                        int rand = ((Number) rez.get("rand")).intValue();
                        int coloana = ((Number) rez.get("coloana")).intValue();
                        scaune.add("R" + rand + "-C" + coloana);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return scaune;
    }

    // Metodă helper pentru a obține email-ul pentru un anumit scaun
    private String getEmailPentruScaun(String film, LocalDate data, String ora, int rand, int coloana) {
        try {
            java.io.File file = new java.io.File("data/rezervari.json");
            if (!file.exists() || file.length() == 0) {
                return null;
            }

            try (java.io.Reader reader = new java.io.FileReader(file)) {
                org.json.simple.JSONArray rezervariArray = (org.json.simple.JSONArray) new org.json.simple.parser.JSONParser().parse(reader);

                for (Object obj : rezervariArray) {
                    org.json.simple.JSONObject rez = (org.json.simple.JSONObject) obj;
                    String filmJson = (String) rez.get("film");
                    String oraJson = (String) rez.get("ora");
                    String dataJson = (String) rez.get("data");
                    int randJson = ((Number) rez.get("rand")).intValue();
                    int coloanaJson = ((Number) rez.get("coloana")).intValue();

                    if (filmJson.equals(film) && oraJson.equals(ora) &&
                            dataJson.equals(data.toString()) && randJson == rand && coloanaJson == coloana) {
                        return (String) rez.get("email");
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public void salveazaRezervare(Film film, String oraFilm, LocalDate data, String email,
                                  Set<Scaun> scauneSelectate, Sala sala) {

        StringBuilder scauneStr = new StringBuilder();

        for (Scaun scaun : scauneSelectate) {
            scaun.rezerva(email);

            int rand = -1, coloana = -1;
            Scaun[][] matrice = sala.getScaune();
            for (int r = 0; r < matrice.length; r++) {
                for (int c = 0; c < matrice[r].length; c++) {
                    if (matrice[r][c] == scaun) {
                        rand = r + 1;  // 1-based pentru persistență
                        coloana = c + 1;
                        break;
                    }
                }
                if (rand != -1) break;
            }

            if (rand != -1 && coloana != -1) {
                PersistentaRezervari.salveazaRezervare(
                        film.getTitlu(),
                        sala,
                        data,
                        oraFilm,
                        rand,
                        coloana,
                        email,
                        film.getGen(),
                        film.getRestrictieVarsta()
                );

                scauneStr.append("R").append(rand).append("-C").append(coloana).append("; ");
            }
        }

        emailService.trimiteConfirmare(
                email,
                film.getTitlu(),
                sala.getNume(),
                oraFilm,
                scauneStr.toString()
        );
    }
}