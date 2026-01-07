package cinema.model;

public class Scaun {
    private int rand;
    private int numar;
    private boolean rezervat = false;
    private String emailRezervare;

    public Scaun(int rand, int numar) {
        this.rand = rand;
        this.numar = numar;
    }

    public int getRand() {
        return rand;
    }

    public int getNumar() {
        return numar;
    }

    public boolean esteRezervat() {
        return rezervat;
    }

    public void rezerva(String email) {
        this.rezervat = true;
        this.emailRezervare = email;
    }

    public String getEmailRezervare() {
        return emailRezervare;
    }
    public void anuleazaRezervare() {
        this.rezervat = false;
        this.emailRezervare = null;
    }


}