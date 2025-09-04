package cinema.network;

import java.util.Set;

public class Mesaj {
    public String tip; // "cerere_rezervare", "raspuns", "update_sali"
    public String film;
    public String ora;
    public String data; // yyyy-MM-dd
    public Set<String> scaune; // R1-C1, etc.
    public String email;
    public String status; // "ok" sau "eroare"
    public String mesaj;  // text pentru client
}
