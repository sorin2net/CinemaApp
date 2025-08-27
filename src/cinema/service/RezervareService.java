package cinema.service;

import cinema.model.Film;
import cinema.model.Sala;
import cinema.model.Scaun;

import java.util.ArrayList;
import java.util.List;

public class RezervareService {
    private List<Film> filme = new ArrayList<>();

    public void adaugaFilm(Film film) {
        filme.add(film);
    }

    public List<Film> getFilme() {
        return filme;
    }

    // Returnează matricea de scaune pentru un film
    public Scaun[][] getSala(Film film, String ora) {
        Sala sala = film.getSala();
        // Încarcă rezervările existente din JSON
        cinema.persistence.PersistentaRezervari.incarcaRezervari(sala, film.getTitlu(), ora);
        return sala.getScaune();
    }

    // Salvează rezervarea în JSON
    public void salveazaRezervare(Film film, String ora, String email, Scaun[][] scaune) {
        Sala sala = film.getSala();
        for (int r = 0; r < sala.getRanduri(); r++) {
            for (int c = 0; c < sala.getColoane(); c++) {
                if (scaune[r][c].esteRezervat()) {
                    cinema.persistence.PersistentaRezervari.salveazaRezervare(
                            film.getTitlu(), ora, r + 1, c + 1, email);
                }
            }
        }
    }
}
