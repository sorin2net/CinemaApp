package cinema.persistence;

import java.sql.*;

public class DatabaseManager {

    private static final String DB_URL = "jdbc:sqlite:cinema.db"; // fișierul .db în proiect

    // 1) Conexiune la baza de date
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }

    // 2) Creare tabel dacă nu există
    public static void createTableIfNotExists() {
        String sql = """
            CREATE TABLE IF NOT EXISTS rezervari (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                titlu TEXT,
                gen TEXT,
                varsta INTEGER,
                data TEXT,
                ora TEXT,
                rand INTEGER,
                coloana INTEGER,
                email TEXT
            )
        """;

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.execute();
            System.out.println("Tabelul rezervari a fost creat sau există deja.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // 3) Inserare rezervare
    public static void insertRezervare(String titluFilm, String gen, int varsta, String data, String ora, int rand, int coloana, String email) {
        String sql = "INSERT INTO rezervari (titlu, gen, varsta, data, ora, rand, coloana, email) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, titluFilm);
            stmt.setString(2, gen);
            stmt.setInt(3, varsta);
            stmt.setString(4, data);
            stmt.setString(5, ora);
            stmt.setInt(6, rand);
            stmt.setInt(7, coloana);
            stmt.setString(8, email);

            stmt.executeUpdate();
            System.out.println("Rezervare inserată cu succes în baza de date.");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
