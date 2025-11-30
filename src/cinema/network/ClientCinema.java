package cinema.network;

import com.google.gson.Gson;

import java.io.*;
import java.net.Socket;
import java.util.Properties;
import java.util.Set;
import java.util.function.Consumer;

public class ClientCinema {

    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private Gson gson = new Gson();
    private String host;
    private int port;
    private Consumer<Mesaj> onMessageReceived; // Callback pentru mesaje

    public ClientCinema(String host, int port) throws IOException {
        this.host = host;
        this.port = port;
        socket = new Socket(host, port);
        out = new PrintWriter(socket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        System.out.println("Conectat la server: " + host + ":" + port);

        new Thread(this::ascultaServer).start();
    }

    // Setează callback-ul pentru mesaje primite
    public void setOnMessageReceived(Consumer<Mesaj> callback) {
        this.onMessageReceived = callback;
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

    public void trimiteAnulare(String email, String film, String ora, String data, Set<String> scaune) {
        Mesaj msg = new Mesaj();
        msg.tip = "cerere_anulare";
        msg.email = email;
        msg.film = film;
        msg.ora = ora;
        msg.data = data;
        msg.scaune = scaune;

        out.println(gson.toJson(msg));
    }

    private void ascultaServer() {
        try {
            String line;
            while ((line = in.readLine()) != null) {
                Mesaj msg = gson.fromJson(line, Mesaj.class);

                System.out.println("Mesaj primit de la server: " + msg.tip + " - " + msg.mesaj);

                // Notifică GUI-ul prin callback
                if (onMessageReceived != null) {
                    onMessageReceived.accept(msg);
                }

                // Nu mai afișăm pop-up-uri aici - le gestionează GUI-ul
            }
        } catch (IOException e) {
            System.out.println("Conexiune închisă cu serverul.");
        }
    }

    public Socket getSocket() {
        return socket;
    }
}