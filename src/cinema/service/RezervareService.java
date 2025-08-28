package cinema.service;

import cinema.model.Film;
import cinema.model.Sala;
import cinema.model.Scaun;
import cinema.persistence.PersistentaRezervari;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;

public class RezervareService {
    private List<Film> filme = new ArrayList<>();
    // Cheia: titluFilm -> data (yyyy-MM-dd) -> ora -> Sala
    private Map<String, Map<String, Map<String, Sala>>> rezervari = new HashMap<>();

    public void adaugaFilm(Film film) {
        filme.add(film);

        // Generăm sălile pentru fiecare zi din septembrie-decembrie 2025
        for (int luna = 9; luna <= 12; luna++) {
            YearMonth ym = YearMonth.of(2025, luna);
            for (int zi = 1; zi <= ym.lengthOfMonth(); zi++) {
                LocalDate data = LocalDate.of(2025, luna, zi);
                String dataKey = data.toString(); // ex: "2025-09-15"

                rezervari.putIfAbsent(film.getTitlu(), new HashMap<>());
                rezervari.get(film.getTitlu()).putIfAbsent(dataKey, new HashMap<>());

                for (String ora : film.getOre()) {
                    Sala salaNoua = film.getSala().cloneSala();
                    // încărcăm rezervările salvate (dacă există)
                    PersistentaRezervari.incarcaRezervari(salaNoua, film.getTitlu(), ora + "-" + dataKey);
                    rezervari.get(film.getTitlu()).get(dataKey).put(ora, salaNoua);
                }
            }
        }
    }

    public List<Film> getFilme() {
        return filme;
    }

    public List<Film> getFilmePentruZi(LocalDate data) {
        String dataKey = data.toString();
        List<Film> rezultat = new ArrayList<>();
        for (Film f : filme) {
            if (rezervari.containsKey(f.getTitlu()) && rezervari.get(f.getTitlu()).containsKey(dataKey)) {
                rezultat.add(f);
            }
        }
        return rezultat;
    }

    public Sala getSala(Film film, String ora, LocalDate data) {
        String dataKey = data.toString();
        return rezervari.get(film.getTitlu()).get(dataKey).get(ora);
    }

    public void salveazaRezervare(Film film, String ora, LocalDate data, String email, Sala sala) {
        for (int i = 0; i < sala.getRanduri(); i++) {
            for (int j = 0; j < sala.getColoane(); j++) {
                Scaun scaun = sala.getScaun(i, j);
                if (scaun.esteRezervat() && scaun.getEmailRezervare() == null) {
                    scaun.setEmailRezervare(email);
                    PersistentaRezervari.salveazaRezervare(
                            film.getTitlu(), ora + "-" + data.toString(), i + 1, j + 1, email
                    );
                }
            }
        }
    }
}
