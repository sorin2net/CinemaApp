package cinema.service;

import cinema.model.Film;
import cinema.model.Sala;
import cinema.model.Scaun;
import cinema.persistence.PersistentaRezervari;

import java.util.*;

public class RezervareService {
    private List<Film> filme = new ArrayList<>();
    // Cheia: "titlu-film" -> "zi-ora" -> Sala
    private Map<String, Map<String, Sala>> saliPeZiOra = new HashMap<>();
    // Lista de ore disponibile per film
    private Map<String, Set<String>> oreDisponibile = new HashMap<>();

    public void adaugaFilm(Film film) {
        filme.add(film);
        Map<String, Sala> mapZiOra = new HashMap<>();
        Set<String> oreFilm = new TreeSet<>(film.getOre()); // ore ordonate
        oreDisponibile.put(film.getTitlu(), oreFilm);

        // Inițializare pentru o zi implicită (ex: Luni-1)
        for (String ora : film.getOre()) {
            Sala salaCopy = film.getSala().cloneSala();
            PersistentaRezervari.incarcaRezervari(salaCopy, film.getTitlu(), ora);
            mapZiOra.put("Luni-1-" + ora, salaCopy);
        }

        saliPeZiOra.put(film.getTitlu(), mapZiOra);
    }

    public List<Film> getFilme() {
        return filme;
    }

    public Set<String> getOreDisponibile(String titluFilm) {
        return oreDisponibile.getOrDefault(titluFilm, Collections.emptySet());
    }

    public Sala getSala(Film film, String ora, String zi) {
        Map<String, Sala> mapZiOra = saliPeZiOra.get(film.getTitlu());
        String key = zi + "-" + ora;

        if (!mapZiOra.containsKey(key)) {
            Sala salaNoua = film.getSala().cloneSala();
            mapZiOra.put(key, salaNoua);
        }

        return mapZiOra.get(key);
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
