package esprit.tn.pidevrh.congeApprove;

import esprit.tn.pidevrh.connection.DatabaseConnection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SuiviCongeController {

    @FXML private ListView<String> congeListView;
    @FXML private Button refreshButton;

    private ObservableList<String> congeList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        loadSuiviConge();
    }

    @FXML
    private void loadSuiviConge() {
        congeList.clear();

        String query = "SELECT c.id, CONCAT(u.first_name, ' ', u.last_name) AS username, " +
                "c.start_date, c.end_date " +
                "FROM conge c " +
                "JOIN user u ON c.user_id = u.id " +
                "JOIN demande_conge d ON c.conge_id = d.id " +
                "WHERE d.status = 'APPROVED'";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query);
             ResultSet resultSet = preparedStatement.executeQuery()) {

            System.out.println("🔍 Exécution de la requête...");

            while (resultSet.next()) {
                String congeInfo = " | Utilisateur: " + resultSet.getString("username") +
                        " | Début: " + resultSet.getDate("start_date").toLocalDate() +
                        " | Fin: " + resultSet.getDate("end_date").toLocalDate();

                System.out.println("✅ Congé trouvé : " + congeInfo);
                congeList.add(congeInfo);
            }

            congeListView.setItems(congeList);

            if (congeList.isEmpty()) {
                System.out.println("⚠ Aucun congé approuvé trouvé !");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("❌ Erreur SQL : " + e.getMessage());
        }
    }


    @FXML
    private void handleRefresh() {
        loadSuiviConge();
    }
}
