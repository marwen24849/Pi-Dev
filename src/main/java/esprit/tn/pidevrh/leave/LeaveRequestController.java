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
import java.sql.Date;
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

        String typeConge = congeComboBox.getValue();
        String justification = justificationField.getText().trim();
        String autre = autreCongeField.getText().trim();



        insertLeaveRequest(typeConge, autre, justification, startDate, endDate, selectedFile);
    }

    private void insertLeaveRequest(String typeConge, String autre, String justification, LocalDate startDate, LocalDate endDate, File certificateFile) {
        String sql = "INSERT INTO demande_conge (type_congé, autre, justification, status, date_debut, date_fin, certificate) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setString(1, typeConge);
            preparedStatement.setString(2, autre.isEmpty() ? null : autre);
            preparedStatement.setString(3, justification);
            preparedStatement.setString(4, "PENDING"); // Default status
            preparedStatement.setDate(5, Date.valueOf(startDate));
            preparedStatement.setDate(6, Date.valueOf(endDate));

            if (certificateFile != null && certificateFile.exists() && certificateFile.canRead()) {
                if (certificateFile.length() > 16 * 1024 * 1024) { // Vérifie si le fichier dépasse 16 Mo
                    showAlert("Erreur", "Le fichier est trop volumineux (max: 16 Mo)", Alert.AlertType.ERROR);
                    return;
                }
                try (FileInputStream fis = new FileInputStream(certificateFile)) {
                    preparedStatement.setBinaryStream(7, fis, (int) certificateFile.length());
                    preparedStatement.executeUpdate();
                    showAlert("Succès", "Demande de congé soumise avec succès !", Alert.AlertType.INFORMATION);
                }
            } else {
                preparedStatement.setNull(7, java.sql.Types.BLOB);
                preparedStatement.executeUpdate();
                showAlert("Succès", "Demande de congé soumise avec succès ! ", Alert.AlertType.INFORMATION);
            }

        } catch (SQLException | IOException e) {
            showAlert("Erreur", "Erreur lors de la soumission de la demande de congé : " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    @FXML
    public void handleUpload() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg"));
        File file = fileChooser.showOpenDialog(new Stage());

        if (file != null && file.canRead()) {
            selectedFile = file;
            showAlert("Fichier sélectionné", "Certificat médical sélectionné : " + file.getName(), Alert.AlertType.INFORMATION);
        } else {
            showAlert("Erreur", "Le fichier sélectionné est illisible ou introuvable.", Alert.AlertType.ERROR);
        }
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.show();
    }
}
