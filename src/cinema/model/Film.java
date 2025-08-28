package cinema.model;

import java.util.List;

public class Film {
    private String titlu;
    private int durata; // minute
    private String imaginePath;
    private List<String> ore; // orele de difuzare
    private List<Integer> zile; // zilele în care se difuzează
    private Sala sala;

    public Film(String titlu, int durata, String imaginePath, List<String> ore, List<Integer> zile, Sala sala) {
        this.titlu = titlu;
        this.durata = durata;
        this.imaginePath = imaginePath;
        this.ore = ore;
        this.zile = zile;
        this.sala = sala;
    }

    public String getTitlu() { return titlu; }
    public int getDurata() { return durata; }
    public String getImaginePath() { return imaginePath; }
    public List<String> getOre() { return ore; }
    public List<Integer> getZile() { return zile; } // nou
    public Sala getSala() { return sala; }
}
