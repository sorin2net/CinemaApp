package cinema.service;

import cinema.model.Film;
import cinema.model.Sala;
import cinema.model.Scaun;
import cinema.persistence.PersistentaRezervari;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RezervareService {
    private Map<Film, Map<String, Sala>> filmeSiOre = new HashMap<>();

    public void adaugaFilm(Film film) {
        Map<String, Sala> oreMap = new HashMap<>();
        for (String ora : film.getOre()) {
            // creează o copie a sălii pentru fiecare oră
            Sala salaCopy = new Sala(film.getSala().getNume(),
                    film.getSala().getRanduri(),
                    film.getSala().getColoane());
            // încarcă rezervările existente
            PersistentaRezervari.incarcaRezervari(salaCopy, film.getTitlu(), ora);
            oreMap.put(ora, salaCopy);
        }
        filmeSiOre.put(film, oreMap);
    }

    public List<Film> getFilme() {
        return List.copyOf(filmeSiOre.keySet());
    }

    public Sala getSala(Film film, String ora) {
        return filmeSiOre.get(film).get(ora);
    }

    public void salveazaRezervare(Film film, String ora, String email, Sala sala) {
        // parcurge toate scaunele și salvează cele rezervate în JSON
        Scaun[][] scaune = sala.getScaune();
        for (int r = 0; r < scaune.length; r++) {
            for (int c = 0; c < scaune[r].length; c++) {
                if (scaune[r][c].esteRezervat()) {
                    PersistentaRezervari.salveazaRezervare(film.getTitlu(),
                            ora,
                            r + 1,
                            c + 1,
                            email);
                }
            }
        }
    }
}
