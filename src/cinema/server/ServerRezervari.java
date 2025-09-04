package cinema.server;

import cinema.model.Film;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ServerRezervari {
    private static List<String> rezervari = new ArrayList<>();
    private static List<Film> filme = new ArrayList<>();

    public static void main(String[] args) {
        // 1) Încarcă filmele din JSON
        try {
            Gson gson = new Gson();
            FileReader reader = new FileReader("resources/filme.json"); // asigură-te că JSON-ul e aici
            filme = gson.fromJson(reader, new TypeToken<List<Film>>(){}.getType());
            reader.close();
            System.out.println("Server: Filme încărcate corect! " + filme.size() + " filme disponibile.");
        } catch (Exception e) {
            System.out.println("Server: Nu am putut încărca filmele!");
            e.printStackTrace();
            return; // oprim serverul dacă nu avem filme
        }

        int port = 1234; // portul trebuie să fie același cu cel din getSocket()

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server pornit la portul " + port);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client conectat: " + clientSocket.getInetAddress());

                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);

                String mesaj = in.readLine();
                if (mesaj != null) {
                    // Format mesaj: titluFilm;ora;zi;email
                    String[] parts = mesaj.split(";");
                    if (parts.length < 4) {
                        out.println("Format mesaj invalid!");
                        clientSocket.close();
                        continue;
                    }

                    String titluFilm = parts[0];
                    String ora = parts[1];
                    String zi = parts[2];
                    String email = parts[3];

                    Film filmGasit = null;
                    for (Film f : filme) {
                        if (f.getTitlu().equalsIgnoreCase(titluFilm)) {
                            filmGasit = f;
                            break;
                        }
                    }

                    if (filmGasit == null) {
                        out.println("Film inexistent!");
                    } else {
                        rezervari.add(mesaj);
                        out.println("Rezervare confirmata pentru filmul " + titluFilm + " la ora " + ora + "!");
                        System.out.println("Rezervare pentru film: " + titluFilm + " la ora " + ora + " ziua " + zi + " email: " + email);
                    }
                }

                clientSocket.close();
            }

        } catch (IOException e) {
            System.out.println("Eroare la server: " + e.getMessage());
        }
    }
}
