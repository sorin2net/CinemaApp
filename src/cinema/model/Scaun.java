package cinema.model;

public class Scaun {
    private boolean rezervat;
    private String emailRezervare; // cine a rezervat locul

    public Scaun() {
        this.rezervat = false;
        this.emailRezervare = null;
    }

    public boolean esteRezervat() {
        return rezervat;
    }

    public void rezerva() {
        this.rezervat = true;
    }

    public void anuleaza() {
        this.rezervat = false;
        this.emailRezervare = null;
    }

    // Getter și Setter pentru email
    public String getEmailRezervare() {
        return emailRezervare;
    }

    public void setEmailRezervare(String emailRezervare) {
        this.emailRezervare = emailRezervare;
    }
}
