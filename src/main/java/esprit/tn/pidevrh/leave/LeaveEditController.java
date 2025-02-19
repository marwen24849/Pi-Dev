package esprit.tn.pidevrh.leave;

import esprit.tn.pidevrh.connection.DatabaseConnection;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;

public class LeaveEditController {

    @FXML private ComboBox<String> congeComboBox;
    @FXML private TextField autreField;
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    @FXML private TextArea justificationField;
    @FXML private TextField medicalCertificateField;
    @FXML private Button uploadCertificateButton;

    private Leave leave;
    private LeaveDisplayController parentController;
    private File selectedCertificate;
    private final long STATIC_USER_ID = 1; // ID utilisateur statique (à rendre dynamique si nécessaire)

    @FXML
    public void initialize() {
        congeComboBox.getItems().addAll("Vacances", "Maladie", "Autre");
        congeComboBox.setOnAction(event -> handleCongeSelection());
    }

    public void setLeaveData(Leave leave, LeaveDisplayController parentController) {
        this.leave = leave;
        this.parentController = parentController;

        congeComboBox.getSelectionModel().select(leave.getTypeConge());
        autreField.setText(leave.getTypeConge().equalsIgnoreCase("Autre") ? leave.getAutre() : "");
        startDatePicker.setValue(leave.getDateDebut());
        endDatePicker.setValue(leave.getDateFin());
        justificationField.setText(leave.getJustification());

        handleCongeSelection();
    }

    @FXML
    private void handleCongeSelection() {
        String selectedType = congeComboBox.getSelectionModel().getSelectedItem();
        boolean isMedicalLeave = "Maladie".equalsIgnoreCase(selectedType);
        boolean isOtherLeave = "Autre".equalsIgnoreCase(selectedType);

        medicalCertificateField.setDisable(!isMedicalLeave);
        uploadCertificateButton.setDisable(!isMedicalLeave);
        autreField.setDisable(!isOtherLeave);

        if (!isMedicalLeave) {
            selectedCertificate = null; // Supprime le fichier sélectionné si on quitte "Maladie"
            medicalCertificateField.clear();
        }
        if (!isOtherLeave) {
            autreField.clear();
        }
    }

    @FXML
    private void uploadCertificate() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg"));
        selectedCertificate = fileChooser.showOpenDialog(null);
        if (selectedCertificate != null) {
            medicalCertificateField.setText(selectedCertificate.getName());
        }
    }

    @FXML
    private void saveChanges() {
        String selectedType = congeComboBox.getSelectionModel().getSelectedItem();
        String previousType = leave.getTypeConge();
        boolean isNewTypeMedical = "Maladie".equalsIgnoreCase(selectedType);
        boolean wasOldTypeMedical = "Maladie".equalsIgnoreCase(previousType);
        boolean isNewTypeOther = "Autre".equalsIgnoreCase(selectedType);

        String autreValue = isNewTypeOther ? autreField.getText().trim() : null;
        boolean shouldRemoveCertificate = wasOldTypeMedical && !isNewTypeMedical;
        boolean shouldUpdateCertificate = isNewTypeMedical && selectedCertificate != null;

        String query;
        if (shouldRemoveCertificate) {
            query = "UPDATE demande_conge SET type_congé = ?, autre = ?, justification = ?, date_debut = ?, date_fin = ?, certificate = NULL, user_id = ? WHERE id = ?";
        } else if (shouldUpdateCertificate) {
            query = "UPDATE demande_conge SET type_congé = ?, autre = ?, justification = ?, date_debut = ?, date_fin = ?, certificate = ?, user_id = ? WHERE id = ?";
        } else {
            query = "UPDATE demande_conge SET type_congé = ?, autre = ?, justification = ?, date_debut = ?, date_fin = ?, user_id = ? WHERE id = ?";
        }

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setString(1, selectedType);
            preparedStatement.setString(2, autreValue); // Se remplit seulement si le type est "Autre", sinon NULL
            preparedStatement.setString(3, justificationField.getText());
            preparedStatement.setDate(4, java.sql.Date.valueOf(startDatePicker.getValue()));
            preparedStatement.setDate(5, java.sql.Date.valueOf(endDatePicker.getValue()));

            if (shouldUpdateCertificate) {
                if (selectedCertificate.length() > 16 * 1024 * 1024) { // Vérification taille max
                    showAlert("Erreur", "Le fichier est trop volumineux (max: 16 Mo)", Alert.AlertType.ERROR);
                    return;
                }
                try (FileInputStream fis = new FileInputStream(selectedCertificate)) {
                    preparedStatement.setBinaryStream(6, fis, (int) selectedCertificate.length());
                    preparedStatement.setLong(7, STATIC_USER_ID);
                    preparedStatement.setInt(8, leave.getId());
                    preparedStatement.executeUpdate();
                }
            } else {
                preparedStatement.setLong(6, STATIC_USER_ID);
                preparedStatement.setInt(7, leave.getId());
                preparedStatement.executeUpdate();
            }

            parentController.loadLeaveRequests();
            closeWindow();
            showAlert("Succès", "Demande mise à jour avec succès", Alert.AlertType.INFORMATION);

        } catch (SQLException | IOException e) {
            showAlert("Erreur", "Erreur lors de la mise à jour de la demande: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    private void showAlert(String title, String message, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void closeWindow() {
        Stage stage = (Stage) congeComboBox.getScene().getWindow();
        stage.close();
    }
}
