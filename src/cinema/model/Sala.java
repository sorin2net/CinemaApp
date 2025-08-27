package cinema.model;

public class Sala {
    private String nume;
    private int randuri;
    private int coloane;
    private Scaun[][] scaune;

    public Sala(String nume, int randuri, int coloane) {
        this.nume = nume;
        this.randuri = randuri;
        this.coloane = coloane;
        scaune = new Scaun[randuri][coloane];
        for (int r = 0; r < randuri; r++) {
            for (int c = 0; c < coloane; c++) {
                scaune[r][c] = new Scaun();
            }
        }
    }

    public String getNume() {
        return nume;
    }

    public int getRanduri() {
        return randuri;
    }

    public int getColoane() {
        return coloane;
    }

    public Scaun getScaun(int r, int c) {
        return scaune[r][c];
    }

    public Scaun[][] getScaune() {
        return scaune;
    }

    // Noua metodă pentru clonare
    public Sala cloneSala() {
        Sala copie = new Sala(this.nume, this.randuri, this.coloane);
        for (int r = 0; r < this.randuri; r++) {
            for (int c = 0; c < this.coloane; c++) {
                // copiează starea fiecărui scaun
                if (this.scaune[r][c].esteRezervat()) {
                    copie.getScaun(r, c).rezerva();
                }
            }
        }
        return copie;
    }
}
