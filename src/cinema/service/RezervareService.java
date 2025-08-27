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

    public boolean rezervaScaun(Film film, int rand, int coloana) {
        Sala sala = film.getSala();
        if (sala == null) return false;

        // verificăm dacă rand/coloana sunt valide
        if (rand < 0 || rand >= sala.getRanduri() || coloana < 0 || coloana >= sala.getColoane()) {
            return false;
        }

        Scaun scaun = sala.getScaun(rand, coloana);
        if (!scaun.esteRezervat()) {
            scaun.rezerva();
            return true;
        } else {
            return false; // deja rezervat
        }
    }
}
