package esprit.tn.pidevrh.leave;

import esprit.tn.pidevrh.connection.DatabaseConnection;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Arrays;

public class LeaveRequestController {
    @FXML private ComboBox<String> congeComboBox;
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    @FXML private TextArea justificationField;
    @FXML private Button submitButton;

    @FXML
    public void initialize() {
        // Populate leave types in ComboBox
        congeComboBox.getItems().addAll("Vacances", "Maladie", "Autre");
    }

    @FXML
    public void handleSubmit() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String leaveType = congeComboBox.getValue();
            if (leaveType == null) {
                showAlert("Error", "Please select a leave type!", Alert.AlertType.ERROR);
                return;
            }

            String sql = "INSERT INTO demande_conge (user_id, conge_id, justification, status, date_debut, date_fin) VALUES (?, ?, ?, 'PENDING', ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, 1); // Static user_id, replace with session user ID
            stmt.setInt(2, getCongeId(leaveType)); // Get conge_id from type
            stmt.setString(3, justificationField.getText());
            stmt.setDate(4, java.sql.Date.valueOf(startDatePicker.getValue()));
            stmt.setDate(5, java.sql.Date.valueOf(endDatePicker.getValue()));

            stmt.executeUpdate();
            showAlert("Success", "Leave request submitted successfully!", Alert.AlertType.INFORMATION);
        } catch (SQLException e) {
            showAlert("Error", "Database error: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    // Convert Leave Type to ID
    private int getCongeId(String type) {
        return switch (type) {
            case "Vacances" -> 1;
            case "Maladie" -> 2;
            default -> 3; // "Autre"
        };
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.show();
    }
}
