package cinema.service;

import cinema.model.Film;
import cinema.model.Sala;
import cinema.model.Scaun;
import cinema.persistence.PersistentaRezervari;
import cinema.persistence.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class RezervareService {
    private List<Film> filme = new ArrayList<>();
    private Map<String, Map<String, Sala>> saliPeZiOra = new HashMap<>();

    public void adaugaFilm(Film film) {
        filme.add(film);
        Map<String, Sala> mapZiOra = new HashMap<>();
        for (Integer zi : film.getZile()) {
            for (String ora : film.getOre()) {
                mapZiOra.put(zi + "-" + ora, film.getSala().cloneSala());
            }
        }
        saliPeZiOra.put(film.getTitlu(), mapZiOra);
    }

    public List<Film> getFilme() {
        return filme;
    }

    public List<Film> getFilmePentruZi(LocalDate data) {
        int zi = data.getDayOfMonth();
        List<Film> result = new ArrayList<>();
        for (Film f : filme) {
            if (f.getZile().contains(zi)) {
                result.add(f);
            }
        }
        return result;
    }

    public Sala getSala(Film film, String ora, LocalDate data) {
        Map<String, Sala> mapZiOra = saliPeZiOra.get(film.getTitlu());
        String key = data.getDayOfMonth() + "-" + ora;
        if (!mapZiOra.containsKey(key)) {
            mapZiOra.put(key, film.getSala().cloneSala());
        }

        Sala sala = mapZiOra.get(key);
        PersistentaRezervari.incarcaRezervari(sala, film.getTitlu(), data, ora);

        return sala;
    }

    public void salveazaRezervare(Film film, String ora, LocalDate data, String email, Set<Scaun> scauneSelectate, Sala sala) {
        for (Scaun scaun : scauneSelectate) {
            scaun.rezerva(email);

            // 1. Salvare în fișier (există deja)
            int rand = -1, coloana = -1;
            Scaun[][] matrice = sala.getScaune();
            for (int r = 0; r < matrice.length; r++) {
                for (int c = 0; c < matrice[r].length; c++) {
                    if (matrice[r][c] == scaun) {
                        rand = r + 1;
                        coloana = c + 1;
                    }
                }
            }
            if (rand != -1 && coloana != -1) {
                PersistentaRezervari.salveazaRezervare(film.getTitlu(), sala, data, ora, rand, coloana, email);
            }

            // 2. Salvare în baza de date
            try {
                DatabaseManager.insertRezervare(
                        film.getTitlu(),
                        film.getGen(),
                        film.getRestrictieVarsta(),
                        data.toString(),
                        ora,
                        rand,
                        coloana,
                        email
                );
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

}
