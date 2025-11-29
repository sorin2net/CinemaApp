package cinema.network;

import com.google.gson.Gson;

import java.io.*;
import java.net.Socket;
import java.util.Properties;
import java.util.Set;

public class ClientCinema {

    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private Gson gson = new Gson();
    private String host;
    private int port;

    public ClientCinema() throws IOException {
        // Citim IP și port din config.properties
        Properties props = new Properties();
        try (FileInputStream fis = new FileInputStream("config.properties")) {
            props.load(fis);
        } catch (IOException e) {
            throw new IOException("Nu am putut citi fișierul config.properties!", e);
        }

        String host = props.getProperty("server.ip", "127.0.0.1"); // implicit localhost
        int port = Integer.parseInt(props.getProperty("server.port", "12345")); // implicit 12345

        socket = new Socket(host, port);
        out = new PrintWriter(socket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        System.out.println("Conectat la server: " + host + ":" + port);

        new Thread(this::ascultaServer).start();
    }

    public ClientCinema(String host, int port) throws IOException {
        this.host = host;
        this.port = port;
        socket = new Socket(host, port);
        out = new PrintWriter(socket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        System.out.println("Conectat la server: " + host + ":" + port);

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
                    // TODO: aici actualizez GUI-ul scaunelor
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Socket getSocket() {
        return socket;
    }
    public void trimiteAnulare(String email, String film, String ora, String data, Set<String> scaune) {
        Mesaj msg = new Mesaj();
        msg.tip = "cerere_anulare"; // tipul mesajului pentru server
        msg.email = email;
        msg.film = film;
        msg.ora = ora;
        msg.data = data;
        msg.scaune = scaune;

        out.println(gson.toJson(msg));
    }
}
