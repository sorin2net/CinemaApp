package cinema.network;

import java.util.Set;

public class Mesaj {
    public String tip; // "cerere_rezervare", "raspuns", "update_sali"
    public String film;
    public String ora;
    public String data;
    public Set<String> scaune;
    public String email;
    public String status;
    public String mesaj;
}