package esprit.tn.pidevrh.Reclamation;

import esprit.tn.pidevrh.connection.DatabaseConnection;
import esprit.tn.pidevrh.email.EmailService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.paint.Color;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ListReclamationController {

    @FXML
    private VBox reclamationContainer;

    @FXML
    public void initialize() {
        loadReclamations();
    }

    private void loadReclamations() {
        reclamationContainer.getChildren().clear();

        String query = "SELECT r.id, r.sujet, r.description, r.date_creation, r.statut, u.first_name, u.last_name " +
                "FROM reclamation r JOIN user u ON r.user_id = u.id";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                int reclamationId = rs.getInt("id");
                String sujet = rs.getString("sujet");
                String description = rs.getString("description");
                String dateCreation = rs.getString("date_creation");
                String statut = rs.getString("statut");
                String userFullName = rs.getString("first_name") + " " + rs.getString("last_name");

                VBox postBox = new VBox(8);
                postBox.setStyle("-fx-padding: 15px; -fx-background-color: #ffffff; " +
                        "-fx-border-color: #ddd; -fx-border-radius: 10px; -fx-background-radius: 10px; " +
                        "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 0, 2, 2);");

                Label userLabel = new Label(userFullName);
                userLabel.setFont(new Font("Arial", 14));
                userLabel.setStyle("-fx-font-weight: bold;");

                Label dateLabel = new Label(dateCreation);
                dateLabel.setStyle("-fx-text-fill: #777; -fx-font-size: 12px;");

                Label sujetLabel = new Label(sujet);
                sujetLabel.setFont(new Font("Arial", 16));
                sujetLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #333;");

                Text descriptionText = new Text(description);
                descriptionText.setWrappingWidth(400);
                descriptionText.setStyle("-fx-font-size: 14px; -fx-fill: #555;");

                Label statutLabel = new Label("Statut: " + statut);
                statutLabel.setFont(new Font("Arial", 13));
                if ("Résolue".equals(statut)) {
                    statutLabel.setTextFill(Color.GREEN);
                } else {
                    statutLabel.setTextFill(Color.RED);
                }

                Button resolveButton = new Button("✅ Marquer comme Résolue");
                resolveButton.setStyle("-fx-background-color: #3A4045; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 5px 10px;");
                resolveButton.setOnAction(e -> markAsResolved(reclamationId));

                HBox topBox = new HBox(10, userLabel, dateLabel);
                topBox.setStyle("-fx-padding: 0 0 10px 0;");

                postBox.getChildren().addAll(topBox, sujetLabel, descriptionText, statutLabel, resolveButton);
                reclamationContainer.getChildren().add(postBox);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void markAsResolved(int reclamationId) {
        String updateQuery = "UPDATE reclamation SET statut = 'Résolue' WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(updateQuery)) {
            pstmt.setInt(1, reclamationId);
            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                loadReclamations();
                showAlert("Succès", "Réclamation marquée comme résolue.");


                new Thread(() -> {
                    String selectQuery = "SELECT  r.description, r.date_creation, u.email " +
                            "FROM reclamation r JOIN user u ON r.user_id = u.id WHERE r.id = ?";
                    try (Connection conn2 = DatabaseConnection.getConnection();
                         PreparedStatement pstmt2 = conn2.prepareStatement(selectQuery)) {
                        pstmt2.setInt(1, reclamationId);
                        try (ResultSet rs = pstmt2.executeQuery()) {
                            if (rs.next()) {
                                String description = rs.getString("description");
                                String dateCreation = rs.getString("date_creation");
                                String recipientEmail = rs.getString("email");

                                String emailSubject = "Réclamation résolue";
                                String emailContent = "Votre réclamation envoyée le " + dateCreation +
                                        " : " + description +
                                        "' est maintenant résolue.";


                                EmailService.sendEmail(recipientEmail, emailSubject, emailContent);
                            }
                        }
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }).start();
            } else {
                showAlert("Erreur", "Impossible de mettre à jour la réclamation.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Erreur", "Une erreur est survenue lors de la mise à jour.");
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
