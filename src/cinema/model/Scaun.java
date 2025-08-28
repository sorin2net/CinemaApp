package cinema.model;

public class Scaun {
    private boolean rezervat = false;
    private String emailRezervare; // opțional, dacă vrei să salvezi cine a rezervat

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

    public void reset() {
        this.rezervat = false;
        this.emailRezervare = null;
    }
}
