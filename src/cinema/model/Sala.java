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

        // Inițializare scaune
        for (int i = 0; i < randuri; i++) {
            for (int j = 0; j < coloane; j++) {
                scaune[i][j] = new Scaun(i, j);
            }
        }
    }

    public Scaun getScaun(int rand, int coloana) {
        return scaune[rand][coloana];
    }

    public String getNume() { return nume; }
    public int getRanduri() { return randuri; }
    public int getColoane() { return coloane; }
}
