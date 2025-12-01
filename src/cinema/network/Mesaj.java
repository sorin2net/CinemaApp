package cinema.network;

import java.util.List;
import java.util.Set;

public class Mesaj {
    public String tip; // "cerere_rezervare", "raspuns", "update_sali", "cerere_anulare", "raspuns_anulare"
    public String film;
    public String ora;
    public String data;
    public Set<String> scaune;
    public String email;
    public String status; // "ok" sau "eroare"
    public String mesaj;
    public List<String> scauneOcupate; // NOU: pentru a trimite lista scaunelor ocupate
}