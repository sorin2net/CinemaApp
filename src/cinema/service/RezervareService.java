package cinema.service;

import cinema.model.Film;
import cinema.model.Sala;
import cinema.model.Scaun;
import cinema.persistence.PersistentaRezervari;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RezervareService {
    private List<Film> filme = new ArrayList<>();
    private Map<String, Map<String, Sala>> saliPeOra = new HashMap<>();

    public void adaugaFilm(Film film) {
        filme.add(film);
        Map<String, Sala> mapOre = new HashMap<>();
        for (String ora : film.getOre()) {
            Sala salaCopy = new Sala(film.getSala().getNume(), film.getSala().getRanduri(), film.getSala().getColoane());
            PersistentaRezervari.incarcaRezervari(salaCopy, film.getTitlu(), ora);
            mapOre.put(ora, salaCopy);
        }
        saliPeOra.put(film.getTitlu(), mapOre);
    }

    public List<Film> getFilme() {
        return filme;
    }

    public Sala getSala(Film film, String ora) {
        return saliPeOra.get(film.getTitlu()).get(ora);
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
