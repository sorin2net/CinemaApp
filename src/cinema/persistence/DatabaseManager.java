package cinema.persistence;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseManager {
    private static final String DB_URL = "jdbc:sqlite:data/cinema.db";

    // Initializează baza de date și tabelul rezervări
    public static void initDatabase() {
        // Creează folderul data dacă nu există
        java.io.File folder = new java.io.File("data");
        if (!folder.exists()) {
            folder.mkdirs();
        }

        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            if (conn != null) {
                System.out.println("Conexiune SQLite realizată!");
                createRezervariTable(conn);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Creează tabelul rezervări dacă nu există
    private static void createRezervariTable(Connection conn) {
        String sql = "CREATE TABLE IF NOT EXISTS rezervari (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "film TEXT NOT NULL," +
                "sala TEXT NOT NULL," +
                "data DATE NOT NULL," +
                "ora TEXT NOT NULL," +
                "email TEXT NOT NULL," +
                "scaune TEXT NOT NULL" + // lista de scaune rezervate, ca text
                ");";

        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            System.out.println("Tabelul rezervari este gata!");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Obține conexiunea la baza de date
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }
}
