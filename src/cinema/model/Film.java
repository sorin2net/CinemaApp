package cinema.model;

import java.util.List;

public class Film {
    private String titlu;
    private int durata; // minute
    private String imaginePath;
    private List<String> ore; // orele de difuzare

    public Film(String titlu, int durata, String imaginePath, List<String> ore) {
        this.titlu = titlu;
        this.durata = durata;
        this.imaginePath = imaginePath;
        this.ore = ore;
    }

    public String getTitlu() { return titlu; }
    public int getDurata() { return durata; }
    public String getImaginePath() { return imaginePath; }
    public List<String> getOre() { return ore; }
}
