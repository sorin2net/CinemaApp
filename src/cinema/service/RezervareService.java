package cinema.service;

import cinema.model.Film;
import cinema.model.Sala;
import cinema.model.Scaun;
import cinema.persistence.PersistentaRezervari;

import java.util.*;

public class RezervareService {
    private List<Film> filme = new ArrayList<>();
    // cheia: "titlu-film" -> "zi-ora" -> Sala
    private Map<String, Map<String, Sala>> saliPeOraSiZi = new HashMap<>();

    public void adaugaFilm(Film film) {
        filme.add(film);
        Map<String, Sala> mapZiOra = new HashMap<>();
        for (String ora : film.getOre()) {
            // pentru fiecare ora clonăm sala
            Sala salaCopy = film.getSala().cloneSala();
            PersistentaRezervari.incarcaRezervari(salaCopy, film.getTitlu(), ora);
            mapZiOra.put("Luni-1-" + ora, salaCopy); // inițial zi implicită
        }
        saliPeOraSiZi.put(film.getTitlu(), mapZiOra);
    }

    public List<Film> getFilme() {
        return filme;
    }

    public Sala getSala(Film film, String ora, String zi) {
        Map<String, Sala> mapZiOra = saliPeOraSiZi.get(film.getTitlu());
        if (!mapZiOra.containsKey(zi + "-" + ora)) {
            // clonăm sala dacă nu există încă pentru ziua respectivă
            Sala salaNoua = film.getSala().cloneSala();
            mapZiOra.put(zi + "-" + ora, salaNoua);
        }
        return mapZiOra.get(zi + "-" + ora);
    }

    public void salveazaRezervare(Film film, String ora, String email, Sala sala) {
        for (int i = 0; i < sala.getRanduri(); i++) {
            for (int j = 0; j < sala.getColoane(); j++) {
                Scaun scaun = sala.getScaun(i, j);
                if (scaun.esteRezervat()) {
                    PersistentaRezervari.salveazaRezervare(film.getTitlu(), ora, i + 1, j + 1, email);
                }
            }
        }
    }
}
