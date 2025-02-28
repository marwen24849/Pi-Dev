package esprit.tn.pidevrh.response;

import esprit.tn.pidevrh.connection.DatabaseConnection;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ResultController {

    @FXML
    private Label scoreLabel;
    @FXML
    private Label percentageLabel;
    @FXML
    private Label resultLabel;
    @FXML
    private ListView<String> responsesListView;

    private Long resultatId;
    public void initializeData(Long resultatId) {
        this.resultatId = resultatId;
        loadResult();
        loadResponses();
    }


    private void loadResult() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT score, percentage, resultat FROM resultat WHERE id = ?";
            PreparedStatement ps = conn.prepareStatement(query);
            ps.setLong(1, resultatId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                int score = rs.getInt("score");
                double percentage = rs.getDouble("percentage");
                boolean resultat = rs.getBoolean("resultat");

                // Afficher les résultats
                scoreLabel.setText("Score : " + score);
                percentageLabel.setText("Pourcentage : " + String.format("%.2f", percentage) + "%");
                resultLabel.setText("Résultat : " + (resultat ? "Réussi" : "Échoué"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    private void loadResponses() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT question, answer FROM response_responses WHERE response_id IN " +
                    "(SELECT id FROM response WHERE resultat_id = ?)";
            PreparedStatement ps = conn.prepareStatement(query);
            ps.setLong(1, resultatId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                String question = rs.getString("question");
                String answer = rs.getString("answer");


                responsesListView.getItems().add("Question : " + question + "\nRéponse : " + answer);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}