package cinema.model;

public class Scaun {
    private int rand;
    private int coloana;
    private boolean rezervat;

    public Scaun(int rand, int coloana) {
        this.rand = rand;
        this.coloana = coloana;
        this.rezervat = false;
    }
    public Scaun() {
        this.rezervat = false;
    }
    public int getRand() { return rand; }
    public int getColoana() { return coloana; }
    public boolean esteRezervat() { return rezervat; }

    // metoda nu ia niciun argument
    public void rezerva() {
        this.rezervat = true;
    }
}
