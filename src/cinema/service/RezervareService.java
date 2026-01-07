package cinema.service;

import cinema.model.Film;
import cinema.model.Sala;
import cinema.model.Scaun;
import cinema.persistence.DatabaseManager;
import cinema.persistence.PersistentaRezervari;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class RezervareService {
    private List<Film> filme = new ArrayList<>();
    private Map<String, Map<String, Sala>> saliPeZiOra = new ConcurrentHashMap<>();
    private final EmailService emailService = new EmailService();

    public void adaugaFilm(Film film) {
        filme.add(film);
        Map<String, Sala> mapZiOra = new ConcurrentHashMap<>();
        for (Film.DataRulare dr : film.getDateRulare()) {
            for (String ora : film.getOre()) {
                String key = dr.getLuna() + "-" + dr.getZi() + "-" + ora;
                mapZiOra.put(key, film.getSala().cloneSala());
            }
        }
        saliPeZiOra.put(film.getTitlu(), mapZiOra);
    }

    public synchronized Sala getSala(Film film, String oraFilm, LocalDate data) {
        Map<String, Sala> mapZiOra = saliPeZiOra.get(film.getTitlu());
        String key = data.getMonthValue() + "-" + data.getDayOfMonth() + "-" + oraFilm;

        if (!mapZiOra.containsKey(key)) {
            mapZiOra.put(key, film.getSala().cloneSala());
        }

        Sala sala = mapZiOra.get(key);

        Set<String> ocupateInDB = DatabaseManager.getScauneOcupate(film.getTitlu(), data.toString(), oraFilm);

        for (int r = 0; r < sala.getRanduri(); r++) {
            for (int c = 0; c < sala.getColoane(); c++) {
                Scaun scaun = sala.getScaun(r, c);
                String scaunKey = "R" + (r + 1) + "-C" + (c + 1);

                if (ocupateInDB.contains(scaunKey)) {
                    if (!scaun.esteRezervat()) scaun.rezerva("sistem@cinema.ro");
                } else {
                    if (scaun.esteRezervat()) scaun.anuleazaRezervare();
                }
            }
        }
        return sala;
    }

    public void salveazaRezervare(Film film, String oraFilm, LocalDate data, String email, Set<Scaun> scauneSelectate, Sala sala) {
        StringBuilder scauneStr = new StringBuilder();
        for (Scaun scaun : scauneSelectate) {
            scaun.rezerva(email);
            PersistentaRezervari.salveazaRezervare(film.getTitlu(), sala, data, oraFilm, scaun.getRand(), scaun.getNumar(), email, film.getGen(), film.getRestrictieVarsta());
            scauneStr.append("R").append(scaun.getRand()).append("-C").append(scaun.getNumar()).append("; ");
        }
        emailService.trimiteConfirmare(email, film.getTitlu(), sala.getNume(), oraFilm, scauneStr.toString());
    }

    public List<Film> getFilme() { return new ArrayList<>(filme); }
    public List<Film> getFilmePentruZi(LocalDate data) {
        List<Film> result = new ArrayList<>();
        for (Film f : filme) { if (f.ruleazaLaData(data)) result.add(f); }
        return result;
    }
    public EmailService getEmailService() {
        return emailService;
    }
}