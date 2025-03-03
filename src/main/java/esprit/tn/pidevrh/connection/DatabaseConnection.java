package esprit.tn.pidevrh.connection;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DatabaseConnection {
    private static final String URL = "jdbc:mysql://localhost:3306/SGRH";
    private static final String USER = "root";
    private static final String PASSWORD = "pidev";

    private static DatabaseConnection instance;
    private Connection connection;

    private static final Logger logger = Logger.getLogger(DatabaseConnection.class.getName());

    private DatabaseConnection() {
        try {
            this.connection = DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Erreur de connexion à la base de données : " + e.getMessage(), e);
            throw new RuntimeException("Erreur de connexion à la base de données", e);
        }
    }

    public static synchronized DatabaseConnection getInstance() {
        if (instance == null || instance.connection == null || isConnectionClosed(instance.connection)) {
            instance = new DatabaseConnection();
        }
        return instance;
    }

    public static Connection getConnection() {
        return getInstance().connection;
    }

    private static boolean isConnectionClosed(Connection conn) {
        try {
            return conn == null || conn.isClosed();
        } catch (SQLException e) {
            logger.log(Level.WARNING, "Erreur lors de la vérification de l'état de la connexion : " + e.getMessage(), e);
            return true;
        }
    }

    // Nouvelle méthode pour fermer proprement la connexion
    public static void closeConnection() {
        try {
            if (instance != null && instance.connection != null && !instance.connection.isClosed()) {
                instance.connection.close();
                logger.info("Connexion fermée avec succès.");
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Erreur lors de la fermeture de la connexion à la base de données : " + e.getMessage(), e);
        }
    }
}