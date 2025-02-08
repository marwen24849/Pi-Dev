package esprit.tn.pidevrh.formation;

import esprit.tn.pidevrh.connection.DatabaseConnection;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class FormationUpdateController {

    @FXML
    private TextField titleField, descriptionField, durationField;

    private Formation formation;

    public void setFormation(Formation formation) {
        this.formation = formation;

        titleField.setText(formation.getTitre());
        descriptionField.setText(formation.getDescription());
        durationField.setText(String.valueOf(formation.getDuree()));
    }

    @FXML
    private void handleSave() {
        // Validate the form fields
        if (!validateForm()) {
            return;  // Return early if validation fails
        }

        String sql = "UPDATE formation SET title=?, description=?, duration=? WHERE id=?";

        try (Connection connection = DatabaseConnection.getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, titleField.getText());
            preparedStatement.setString(2, descriptionField.getText());
            preparedStatement.setInt(3, Integer.parseInt(durationField.getText()));
            preparedStatement.setLong(4, formation.getId());

            int rowsUpdated = preparedStatement.executeUpdate();

            if (rowsUpdated > 0) {
                showAlert("Succès", "Formation mise à jour avec succès!");
            } else {
                showAlert("Erreur", "Aucune formation n'a été modifiée.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Erreur SQL", "Erreur lors de la modification de la formation dans la base de données.");
        }

        // Close the stage after update
        Stage stage = (Stage) titleField.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void handleCancel() {
        // Close the stage without saving
        Stage stage = (Stage) titleField.getScene().getWindow();
        stage.close();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private boolean validateForm() {
        // Validate Title
        if (titleField.getText().trim().isEmpty()) {
            showAlert("Erreur", "Le titre ne peut pas être vide.");
            return false;
        }

        // Validate Description
        if (descriptionField.getText().trim().isEmpty()) {
            showAlert("Erreur", "La description ne peut pas être vide.");
            return false;
        }

        // Validate Duration (must be a valid number)
        try {
            Integer.parseInt(durationField.getText());
        } catch (NumberFormatException e) {
            showAlert("Erreur", "La durée doit être un nombre valide.");
            return false;
        }

        // If all validations pass
        return true;
    }
}
