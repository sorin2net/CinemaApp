package cinema.model;

import java.util.List;

public class Film {
    private String titlu;
    private int durata;
    private String imaginePath;
    private List<String> ore;
    private Sala sala;

    public Film(String titlu, int durata, String imaginePath, List<String> ore, Sala sala) {
        this.titlu = titlu;
        this.durata = durata;
        this.imaginePath = imaginePath;
        this.ore = ore;
        this.sala = sala;
    }

    public String getTitlu() {
        return titlu;
    }

    public int getDurata() {
        return durata;
    }

    public String getImaginePath() {
        return imaginePath;
    }

    public List<String> getOre() {
        return ore;
    }

    public Sala getSala() {
        return sala;
    }
}
