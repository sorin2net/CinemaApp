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

    public void adaugaFilm(Film film) {
        filme.add(film);
        Map<String, Sala> mapZiOra = new HashMap<>();

        System.out.println("Adăugare film: " + film.getTitlu());

        for (Film.DataRulare dr : film.getDateRulare()) {
            for (String ora : film.getOre()) {
                String key = dr.getLuna() + "-" + dr.getZi() + "-" + ora;
                mapZiOra.put(key, film.getSala().cloneSala());
                System.out.println("  Date rulare: luna=" + dr.getLuna() + ", zi=" + dr.getZi() + ", ora=" + ora);
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
        List<Film> result = new ArrayList<>();
        System.out.println("Căutare filme pentru data: " + data + " (luna=" + data.getMonthValue() + ", zi=" + data.getDayOfMonth() + ")");

        for (Film f : filme) {
            boolean ruleaza = f.ruleazaLaData(data);
            System.out.println("  Film: " + f.getTitlu() + " - rulează: " + ruleaza);
            if (ruleaza) {
                result.add(f);
            }
        }

        System.out.println("Găsite " + result.size() + " filme pentru " + data);
        return result;
    }

    public Sala getSala(Film film, String oraFilm, LocalDate data) {
        Map<String, Sala> mapZiOra = saliPeZiOra.get(film.getTitlu());
        String key = data.getMonthValue() + "-" + data.getDayOfMonth() + "-" + oraFilm;

        if (!mapZiOra.containsKey(key)) {
            mapZiOra.put(key, film.getSala().cloneSala());
        }

        Sala sala = mapZiOra.get(key);

        // IMPORTANT: Resetăm toate scaunele înainte de a încărca rezervările
        // Astfel eliminăm eventualele stări vechi
        for (int r = 0; r < sala.getRanduri(); r++) {
            for (int c = 0; c < sala.getColoane(); c++) {
                sala.getScaun(r, c).reset();
            }
        }

        // Acum încărcăm starea curentă din JSON
        PersistentaRezervari.incarcaRezervari(sala, film.getTitlu(), data, oraFilm);
        return sala;
    }

    public void salveazaRezervare(Film film, String oraFilm, LocalDate data, String email,
                                  Set<Scaun> scauneSelectate, Sala sala) {

        StringBuilder scauneStr = new StringBuilder();

        for (Scaun scaun : scauneSelectate) {
            scaun.rezerva(email);

            int rand = -1, coloana = -1;
            Scaun[][] matrice = sala.getScaune();
            for (int r = 0; r < matrice.length; r++) {
                for (int c = 0; c < matrice[r].length; c++) {
                    if (matrice[r][c] == scaun) {
                        rand = r + 1;  // 1-based pentru persistență
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

                scauneStr.append("R").append(rand).append("-C").append(coloana).append("; ");
            }
        }

        emailService.trimiteConfirmare(
                email,
                film.getTitlu(),
                sala.getNume(),
                oraFilm,
                scauneStr.toString()
        );
    }
}