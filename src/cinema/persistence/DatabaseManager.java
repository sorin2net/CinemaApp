package cinema.persistence;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DatabaseManager {

    private static final String DB_URL = "jdbc:sqlite:cinema.db"; // fișierul .db în proiect
    private static final String URL = "jdbc:sqlite:cinema.db";
    // 1) Conexiune la baza de date
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }

    // 2) Creare tabel dacă nu există
    public static void createTableIfNotExists() {
        String sql = """
            CREATE TABLE IF NOT EXISTS rezervari (
                ora_rezervare TEXT,       -- ora la care s-a făcut rezervarea (prima coloană)
                titlu TEXT,
                gen TEXT,
                varsta INTEGER,
                data TEXT,
                ora_film TEXT,            -- ora filmului (ex: 15:30)
                rand INTEGER,
                coloana INTEGER,
                email TEXT
            )
        """;

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // 3) Inserare rezervare
    public static void insertRezervare(String titluFilm, String gen, int varsta,
                                       String data, String oraFilm, int rand, int coloana, String email) {
        // Ora curentă a rezervării (HH:mm) pentru prima coloană
        String oraRezervare = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));

        String sql = "INSERT INTO rezervari (ora_rezervare, titlu, gen, varsta, data, ora_film, rand, coloana, email) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, oraRezervare);   // prima coloană: ora rezervării
            stmt.setString(2, titluFilm);
            stmt.setString(3, gen);
            stmt.setInt(4, varsta);
            stmt.setString(5, data);
            stmt.setString(6, oraFilm);       // ora filmului (ex: 15:30)
            stmt.setInt(7, rand);
            stmt.setInt(8, coloana);
            stmt.setString(9, email);

            stmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // 4) Ștergere toate rezervările
    public static void clearAllRezervari() {
        String sql = "DELETE FROM rezervari";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.executeUpdate();
            System.out.println("Toate rezervările au fost șterse din baza de date.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public static void stergeRezervare(String email, String titlu, String data, String ora, int rand, int coloana) {
        String sql = "DELETE FROM rezervari WHERE email = ? AND titlu = ? AND data = ? AND ora_film = ? AND rand = ? AND coloana = ?";

        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, email);
            pstmt.setString(2, titlu);
            pstmt.setString(3, data);
            pstmt.setString(4, ora);
            pstmt.setInt(5, rand);
            pstmt.setInt(6, coloana);

            int affected = pstmt.executeUpdate();
            System.out.println("DatabaseManager: " + affected + " rezervare(s) șterse pentru email=" + email);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


}