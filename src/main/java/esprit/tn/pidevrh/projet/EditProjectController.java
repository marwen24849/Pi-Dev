package esprit.tn.pidevrh.projet;


import esprit.tn.pidevrh.connection.DatabaseConnection;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class EditProjectController {

    @FXML
    private TextField nomProjetField;
    @FXML
    private ComboBox<String> equipeComboBox;
    @FXML
    private TextField responsableField;
    @FXML
    private DatePicker dateDebutPicker;
    @FXML
    private DatePicker dateFinPicker;
    @FXML
    private Button saveButton;
    @FXML
    private Button cancelButton;

    private String originalProjectName;  // To store the project name for update

    public void initialize(String projectName, String equipe, String responsable, String startDate, String endDate) {
        originalProjectName = projectName;

        // Pre-populate fields with existing project data
        nomProjetField.setText(projectName);
        equipeComboBox.setValue(equipe);
        responsableField.setText(responsable);

        // Parse dates
        dateDebutPicker.setValue(java.time.LocalDate.parse(startDate));
        dateFinPicker.setValue(java.time.LocalDate.parse(endDate));
    }

    @FXML
    private void handleSaveChanges() {
        // Get updated values
        String newNomProjet = nomProjetField.getText();
        String newEquipe = equipeComboBox.getValue();
        String newResponsable = responsableField.getText();
        String newStartDate = dateDebutPicker.getValue().toString();
        String newEndDate = dateFinPicker.getValue().toString();

        // Call method to update project in database (use originalProjectName for identification)
        updateProjectInDatabase(originalProjectName, newNomProjet, newEquipe, newResponsable, newStartDate, newEndDate);

        // Close the modal
        Stage stage = (Stage) saveButton.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void handleCancel() {
        // Close the modal without saving changes
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }

    private void updateProjectInDatabase(String oldProjectName, String newProjectName, String equipe, String responsable, String startDate, String endDate) {
        // Use the original project name to identify the record and update it in the database
        try (Connection connection = DatabaseConnection.getConnection()) {
            String updateSQL = "UPDATE projet SET nom_projet = ?, equipe = ?, responsable = ?, date_debut = ?, date_fin = ? WHERE nom_projet = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(updateSQL);
            preparedStatement.setString(1, newProjectName);
            preparedStatement.setString(2, equipe);
            preparedStatement.setString(3, responsable);
            preparedStatement.setString(4, startDate);
            preparedStatement.setString(5, endDate);
            preparedStatement.setString(6, oldProjectName);
            preparedStatement.executeUpdate();

            showAlert("Succès", "Projet mis à jour avec succès!", Alert.AlertType.INFORMATION);
        } catch (SQLException e) {
            showAlert("Erreur", "Erreur lors de la mise à jour du projet: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
