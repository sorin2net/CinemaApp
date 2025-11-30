package cinema.model;

import java.time.LocalDate;
import java.util.List;
import java.util.ArrayList;

public class Film {
    private String titlu;
    private int durata;
    private String imaginePath;
    private List<String> ore;
    private int restrictieVarsta;
    private String gen;
    private List<DataRulare> dateRulare; // MODIFICAT: în loc de List<Integer> zile
    private Sala sala;

    public Film(String titlu, int durata, String imaginePath, List<String> ore, int restrictieVarsta, String gen) {
        this.titlu = titlu;
        this.durata = durata;
        this.imaginePath = imaginePath;
        this.ore = ore;
        this.restrictieVarsta = restrictieVarsta;
        this.gen = gen;
        this.dateRulare = new ArrayList<>();
    }

    // Clasă internă pentru date de rulare
    public static class DataRulare {
        private int luna;
        private int zi;

        public DataRulare(int luna, int zi) {
            this.luna = luna;
            this.zi = zi;
        }

        public int getLuna() { return luna; }
        public int getZi() { return zi; }

        public LocalDate toLocalDate(int an) {
            return LocalDate.of(an, luna, zi);
        }
    }

    public List<DataRulare> getDateRulare() {
        return dateRulare;
    }

    public void setDateRulare(List<DataRulare> dateRulare) {
        this.dateRulare = dateRulare;
    }

    // Verifică dacă filmul rulează într-o anumită dată
    public boolean ruleazaLaData(LocalDate data) {
        if (dateRulare == null || dateRulare.isEmpty()) {
            System.out.println("    [DEBUG] " + titlu + " - dateRulare este null sau gol!");
            return false;
        }

        for (DataRulare dr : dateRulare) {
            if (dr.getLuna() == data.getMonthValue() && dr.getZi() == data.getDayOfMonth()) {
                return true;
            }
        }
        return false;
    }

    // Metodă pentru compatibilitate cu codul vechi (deprecated)
    @Deprecated
    public List<Integer> getZile() {
        List<Integer> zile = new ArrayList<>();
        for (DataRulare dr : dateRulare) {
            zile.add(dr.getZi());
        }
        return zile;
    }

    @Deprecated
    public void setZile(List<Integer> zile) {
        // Pentru compatibilitate - nu se mai folosește
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