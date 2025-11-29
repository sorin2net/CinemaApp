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
        this.scaune = new Scaun[randuri][coloane];

        for (int r = 0; r < randuri; r++) {
            for (int c = 0; c < coloane; c++) {
                scaune[r][c] = new Scaun(r + 1, c + 1); // rand și număr începe de la 1
            }
        }
    }


    public Scaun[][] getScaune() {
        return scaune;
    }

    public int getRanduri() {
        return randuri;
    }

    public int getColoane() {
        return coloane;
    }

    public String getNume() {
        return nume;
    }

    // Clonare sala pentru fiecare zi/ora ca fiecare sala sa fie independenta
    public Sala cloneSala() {
        Sala copie = new Sala(this.nume, this.randuri, this.coloane);
        return copie;
    }
    public Scaun getScaun(int rand, int coloana) {
        return scaune[rand][coloana];
    }

}