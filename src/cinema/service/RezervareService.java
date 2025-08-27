package cinema.service;

import cinema.model.Film;
import cinema.model.Sala;
import cinema.model.Scaun;
import cinema.persistence.PersistentaRezervari;

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

    // Rezervă un scaun și salvează în JSON
    public boolean rezervaScaun(Film film, String ora, int rand, int coloana, String email) {
        Sala sala = film.getSala();
        if (sala == null) return false;

        // verificare coordonate valide
        if (rand < 0 || rand >= sala.getRanduri() || coloana < 0 || coloana >= sala.getColoane()) {
            return false;
        }

        Scaun scaun = sala.getScaun(rand, coloana);
        if (!scaun.esteRezervat()) {
            scaun.rezerva();
            PersistentaRezervari.salveazaRezervare(film.getTitlu(), ora, rand + 1, coloana + 1, email);
            return true;
        } else {
            return false;
        }
    }

    // Încarcă toate rezervările din JSON la pornire
    public void incarcaRezervari() {
        for (Film film : filme) {
            Sala sala = film.getSala();
            for (String ora : film.getOre()) {
                PersistentaRezervari.incarcaRezervari(sala, film.getTitlu(), ora);
            }
        }
    }
}
