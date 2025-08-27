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

    // returnează sala unui film
    public Sala getSala(Film film) {
        return film.getSala();
    }

    // rezervare scaun în sala unui film
    public boolean rezervaScaun(Film film, int rand, int coloana, String email, String ora) {
        Sala sala = film.getSala();
        if (sala == null) return false;

        // validare index
        if (rand < 0 || rand >= sala.getRanduri() || coloana < 0 || coloana >= sala.getColoane()) {
            return false;
        }

        Scaun scaun = sala.getScaun(rand, coloana);
        if (!scaun.esteRezervat()) {
            scaun.rezerva();

            // salvăm în JSON
            PersistentaRezervari.salveazaRezervare(
                    film.getTitlu(),
                    ora,
                    rand + 1,   // +1 pentru index uman
                    coloana + 1,
                    email
            );
            return true;
        } else {
            return false;
        }
    }
}
