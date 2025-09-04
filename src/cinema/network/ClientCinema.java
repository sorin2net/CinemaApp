package cinema.network;

import com.google.gson.Gson;

import java.io.*;
import java.net.Socket;
import java.util.Set;

public class ClientCinema {

    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private Gson gson = new Gson();

    public ClientCinema(String host, int port) throws IOException {
        socket = new Socket(host, port);
        out = new PrintWriter(socket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        new Thread(this::ascultaServer).start();
    }

    public void trimiteRezervare(String film, String ora, String data, Set<String> scaune, String email) {
        Mesaj msg = new Mesaj();
        msg.tip = "cerere_rezervare";
        msg.film = film;
        msg.ora = ora;
        msg.data = data;
        msg.scaune = scaune;
        msg.email = email;

        out.println(gson.toJson(msg));
    }

    private void ascultaServer() {
        try {
            String line;
            while ((line = in.readLine()) != null) {
                Mesaj msg = gson.fromJson(line, Mesaj.class);
                if ("raspuns".equals(msg.tip)) {
                    System.out.println("Server: " + msg.mesaj);
                } else if ("update_sali".equals(msg.tip)) {
                    System.out.println("Server: " + msg.mesaj);
                    // Aici vei actualiza GUI-ul scaunelor
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
