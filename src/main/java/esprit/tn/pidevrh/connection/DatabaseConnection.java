

package esprit.tn.pidevrh.connection;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    private static final String URL = "jdbc:mysql://172.31.33.177:3307/SGRH";
    private static final String USER = "root";
    private static final String PASSWORD = "root";

    public static Connection getConnection() {
        try {
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (SQLException e) {
            System.out.println("Erreur de connexion à la base de données : " + e.getMessage());
            throw new RuntimeException(e);
        }
    }
}