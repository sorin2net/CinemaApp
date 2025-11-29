package cinema.service;

import cinema.model.Film;
import cinema.model.Sala;
import cinema.model.Scaun;
import cinema.persistence.PersistentaRezervari;

import java.time.LocalDate;
import java.util.*;

public class RezervareService {
    private List<Film> filme = new ArrayList<>();
    private Map<String, Map<String, Sala>> saliPeZiOra = new HashMap<>();

    private final EmailService emailService = new EmailService();

    // Adauga un film și creeaza clona salilor pentru fiecare zi si ora
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

    public EmailService getEmailService() {
        return emailService;
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

    public Sala getSala(Film film, String oraFilm, LocalDate data) {
        Map<String, Sala> mapZiOra = saliPeZiOra.get(film.getTitlu());
        String key = data.getDayOfMonth() + "-" + oraFilm;
        if (!mapZiOra.containsKey(key)) {
            mapZiOra.put(key, film.getSala().cloneSala());
        }

        Sala sala = mapZiOra.get(key);
        PersistentaRezervari.incarcaRezervari(sala, film.getTitlu(), data, oraFilm);
        return sala;
    }

    // Salvează rezervarile selectate pentru un film + trimite email
    public void salveazaRezervare(Film film, String oraFilm, LocalDate data, String email,
                                  Set<Scaun> scauneSelectate, Sala sala) {

        StringBuilder scauneStr = new StringBuilder();

        //  Rezervam toate scaunele si salvam in baza de date
        for (Scaun scaun : scauneSelectate) {
            scaun.rezerva(email);

            int rand = -1, coloana = -1;
            Scaun[][] matrice = sala.getScaune();
            for (int r = 0; r < matrice.length; r++) {
                for (int c = 0; c < matrice[r].length; c++) {
                    if (matrice[r][c] == scaun) {
                        rand = r + 1;
                        coloana = c + 1;
                        break;
                    }
                }
                if (rand != -1) break;
            }

            if (rand != -1 && coloana != -1) {
                PersistentaRezervari.salveazaRezervare(
                        film.getTitlu(),
                        sala,
                        data,
                        oraFilm,
                        rand,
                        coloana,
                        email,
                        film.getGen(),
                        film.getRestrictieVarsta()
                );

                // adaugam in string pentru email
                scauneStr.append("R").append(rand).append("-C").append(coloana).append("; ");
            }
        }

        //  Trimitem un singur email pentru toate scaunele
        emailService.trimiteConfirmare(
                email,
                film.getTitlu(),
                sala.getNume(),
                oraFilm,
                scauneStr.toString()
        );
    }

}