package esprit.tn.pidevrh.leave;

import esprit.tn.pidevrh.connection.DatabaseConnection;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;

public class LeaveRequestController {
    @FXML private ComboBox<String> congeComboBox;
    @FXML private TextField autreCongeField;
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    @FXML private TextArea justificationField;
    @FXML private Button submitButton;
    @FXML private Button uploadButton;
    private File selectedFile;

    @FXML
    public void initialize() {
        congeComboBox.getItems().addAll("Vacances", "Maladie", "Autre");
        congeComboBox.setOnAction(event -> handleCongeSelection());
        uploadButton.setDisable(true);
        autreCongeField.setDisable(true);
    }

    @FXML
    private void handleCongeSelection() {
        String selectedType = congeComboBox.getValue();
        if ("Maladie".equals(selectedType)) {
            uploadButton.setDisable(false);
            autreCongeField.setDisable(true);
            autreCongeField.clear();
        } else if ("Autre".equals(selectedType)) {
            autreCongeField.setDisable(false);
            uploadButton.setDisable(true);
            selectedFile = null;
        } else {
            uploadButton.setDisable(true);
            autreCongeField.setDisable(true);
            autreCongeField.clear();
            selectedFile = null;
        }
    }

    @FXML
    public void handleSubmit() {
        LocalDate startDate = startDatePicker.getValue();
        LocalDate endDate = endDatePicker.getValue();
        LocalDate today = LocalDate.now();

        if (startDate == null || endDate == null || startDate.isBefore(today) || endDate.isBefore(startDate)) {
            showAlert("Erreur", "Veuillez sélectionner des dates valides !", Alert.AlertType.ERROR);
            return;
        }

        String leaveType = congeComboBox.getValue();
        if (leaveType == null || ("Maladie".equals(leaveType) && selectedFile == null) ||
                ("Autre".equals(leaveType) && autreCongeField.getText().trim().isEmpty())) {
            showAlert("Erreur", "Veuillez remplir tous les champs obligatoires !", Alert.AlertType.ERROR);
            return;
        }

        showAlert("Succès", "Demande de congé soumise avec succès !", Alert.AlertType.INFORMATION);
    }
    @FXML
    public void handleUpload() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg"));
        File selectedFile = fileChooser.showOpenDialog(new Stage());

        if (selectedFile != null) {
            showAlert("Fichier sélectionné", "Certificat médical sélectionné : " + selectedFile.getName(), Alert.AlertType.INFORMATION);
        }
    }


    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.show();
    }
}
