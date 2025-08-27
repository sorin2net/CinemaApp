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
                scaune[r][c] = new Scaun();
            }
        }
    }

    public String getNume() {
        return nume;
    }

    public Scaun[][] getScaune() {
        return scaune;
    }

    // Deep copy pentru ziua diferită
    public Sala cloneSala() {
        Sala copie = new Sala(this.nume, this.randuri, this.coloane);
        for (int r = 0; r < this.randuri; r++) {
            for (int c = 0; c < this.coloane; c++) {
                copie.scaune[r][c] = new Scaun(); // scaune noi, neocupate
            }
        }
        return copie;
    }
    public Scaun getScaun(int rand, int coloana) {
        if (rand < 0 || rand >= randuri || coloana < 0 || coloana >= coloane)
            throw new IndexOutOfBoundsException("Rând sau coloană invalidă");
        return scaune[rand][coloana];
    }
    public int getRanduri() {
        return randuri;
    }

    public int getColoane() {
        return coloane;
    }

}
