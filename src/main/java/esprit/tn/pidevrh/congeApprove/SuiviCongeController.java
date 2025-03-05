package esprit.tn.pidevrh.congeApprove;

import esprit.tn.pidevrh.connection.DatabaseConnection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class SuiviCongeController {

    @FXML private ListView<VBox> congeListView;
    @FXML private Button refreshButton;

    private ObservableList<VBox> congeList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        styleUI();
        loadSuiviConge();
    }

    @FXML
    private void loadSuiviConge() {
        congeList.clear();

        String query = "SELECT c.id, CONCAT(u.first_name, ' ', u.last_name) AS username, " +
                "c.start_date, c.end_date, d.type_congé " +
                "FROM conge c " +
                "JOIN user u ON c.user_id = u.id " +
                "JOIN demande_conge d ON c.conge_id = d.id " +
                "WHERE d.status = 'APPROVED'";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query);
             ResultSet resultSet = preparedStatement.executeQuery()) {

            while (resultSet.next()) {
                String username = resultSet.getString("username");
                LocalDate startDate = resultSet.getDate("start_date").toLocalDate();
                LocalDate endDate = resultSet.getDate("end_date").toLocalDate();
                String typeConge = resultSet.getString("type_congé");
                LocalDate today = LocalDate.now();

                String remainingDaysText;

                if (today.isBefore(startDate)) {
                    // Leave has not started yet
                    remainingDaysText = "📅 En attente";
                } else if ((today.isBefore(endDate) || today.equals(endDate)) && startDate.isBefore(today)) {
                    // Leave has started but not ended
                    long remainingDays = ChronoUnit.DAYS.between(today, endDate);
                    remainingDaysText = "⏳ " + remainingDays + " jours restants";
                } else {
                    // Leave has ended
                    remainingDaysText = "✅ Terminé";
                }

                // Creating labels
                Label userLabel = createStyledLabel("👤 Utilisateur: " + username, true);
                Label startDateLabel = createStyledLabel("📅 Début: " + startDate, false);
                Label endDateLabel = createStyledLabel("🏁 Fin: " + endDate, false);
                Label remainingDaysLabel = createStyledLabel(remainingDaysText, true);

                // Box Styling
                VBox congeBox = new VBox(5, userLabel, startDateLabel, endDateLabel, remainingDaysLabel);
                congeBox.setStyle("-fx-padding: 15px; -fx-background-color: #FFFFFF; " +
                        "-fx-border-radius: 10px; -fx-border-color: #E0E4E7; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.1), 8, 0, 0, 3);");

                congeList.add(congeBox);
            }

            congeListView.setItems(congeList);

        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("❌ SQL Error: " + e.getMessage());
        }
    }

    @FXML
    private void handleRefresh() {
        loadSuiviConge();
    }

    private Label createStyledLabel(String text, boolean bold) {
        Label label = new Label(text);
        label.setFont(Font.font("Arial", bold ? FontWeight.BOLD : FontWeight.NORMAL, 14));
        label.setStyle("-fx-text-fill: #2D3640;");
        return label;
    }

    private void styleUI() {
        congeListView.setStyle(
                "-fx-background-color: #F8FAFC; " +
                        "-fx-border-radius: 10px; " +
                        "-fx-border-color: #E0E4E7; " +
                        "-fx-padding: 10px; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 8, 0, 0, 3);"
        );

        refreshButton.setStyle(
                "-fx-background-color: #3498db; " +
                        "-fx-text-fill: white; " +
                        "-fx-font-size: 14px; " +
                        "-fx-padding: 10px 15px; " +
                        "-fx-background-radius: 6px; " +
                        "-fx-cursor: hand; " +
                        "-fx-effect: dropshadow(gaussian, rgba(52, 152, 219, 0.5), 8, 0.3, 0, 3);"
        );

        refreshButton.setOnMouseEntered(e -> refreshButton.setStyle(
                "-fx-background-color: #2980b9; -fx-text-fill: white; " +
                        "-fx-font-size: 14px; -fx-padding: 10px 15px; " +
                        "-fx-background-radius: 6px; -fx-cursor: hand;"
        ));

        refreshButton.setOnMouseExited(e -> refreshButton.setStyle(
                "-fx-background-color: #3498db; -fx-text-fill: white; " +
                        "-fx-font-size: 14px; -fx-padding: 10px 15px; " +
                        "-fx-background-radius: 6px; -fx-cursor: hand;"
        ));
    }
}
