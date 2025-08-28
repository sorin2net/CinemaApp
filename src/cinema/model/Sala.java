package cinema.model;

public class Sala implements Cloneable {
    private String nume;
    private int randuri;
    private int coloane;
    private Scaun[][] scaune;

    public Sala(String nume, int randuri, int coloane) {
        this.nume = nume;
        this.randuri = randuri;
        this.coloane = coloane;
        this.scaune = new Scaun[randuri][coloane];

        // inițializăm scaunele
        for (int i = 0; i < randuri; i++) {
            for (int j = 0; j < coloane; j++) {
                scaune[i][j] = new Scaun();
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

    public Scaun[][] getScaune() {
        return scaune;
    }

    public Scaun getScaun(int rand, int coloana) {
        return scaune[rand][coloana];
    }

    /**
     * Creează o copie completă a sălii cu toate scaunele libere
     * (fiecare zi/ora primește o sală separată, dar identică ca structură).
     */
    public Sala cloneSala() {
        Sala copie = new Sala(this.nume, this.randuri, this.coloane);
        // scaunele noi sunt deja inițializate ca libere, deci nu copiem rezervările
        return copie;
    }
}
