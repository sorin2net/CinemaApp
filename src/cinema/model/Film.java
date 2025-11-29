package cinema.model;

import java.util.List;

public class Film {
    private String titlu;
    private int durata;
    private String imaginePath;
    private List<String> ore;
    private int restrictieVarsta;
    private String gen;
    private List<Integer> zile; // adăugat
    private Sala sala;          // adăugat

    public Film(String titlu, int durata, String imaginePath, List<String> ore, int restrictieVarsta, String gen) {
        this.titlu = titlu;
        this.durata = durata;
        this.imaginePath = imaginePath;
        this.ore = ore;
        this.restrictieVarsta = restrictieVarsta;
        this.gen = gen;
    }

    public List<Integer> getZile() {
        return zile;
    }

    public void setZile(List<Integer> zile) {
        this.zile = zile;
    }

    public Sala getSala() {
        return sala;
    }

    public void setSala(Sala sala) {
        this.sala = sala;
    }

    public String getTitlu() { return titlu; }
    public int getDurata() { return durata; }
    public String getImaginePath() { return imaginePath; }
    public List<String> getOre() { return ore; }
    public int getRestrictieVarsta() { return restrictieVarsta; }
    public String getGen() { return gen; }
}