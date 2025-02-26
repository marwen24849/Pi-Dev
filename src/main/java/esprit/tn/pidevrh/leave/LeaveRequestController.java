package esprit.tn.pidevrh.leave;

import esprit.tn.pidevrh.connection.DatabaseConnection;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.*;
import java.time.LocalDate;
import java.util.Set;

public class LeaveRequestController {
    @FXML private ComboBox<String> congeComboBox;
    @FXML private TextField autreCongeField;
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    @FXML private TextArea justificationField;
    @FXML private Button submitButton;
    @FXML private Button uploadButton;
    @FXML private Button checkHolidayButton;

    private File selectedFile;
    private final long STATIC_USER_ID = 5;
    private final int MAX_LEAVE_DAYS = 30;

    @FXML
    public void initialize() {
        congeComboBox.getItems().addAll("Vacances", "Maladie", "Autre");
        congeComboBox.setOnAction(event -> handleCongeSelection());
        uploadButton.setDisable(true);
        autreCongeField.setDisable(true);
        checkHolidayButton.setOnAction(event -> handleCheckHolidays());
    }
    @FXML
    public void handleCheckHolidays() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Vérification des jours fériés");
        dialog.setHeaderText("Entrer le code du pays et l'année");
        dialog.setContentText("Code Pays:");

        String countryCode = dialog.showAndWait().orElse("");
        dialog.setContentText("Année:");
        String yearString = dialog.showAndWait().orElse("");

        try {
            int year = Integer.parseInt(yearString);
            Set<String> publicHolidays = PublicHolidayService.getPublicHolidays(year, countryCode);
            showAlert("Jours Fériés", "Jours fériés récupérés: \n" + String.join("\n", publicHolidays), Alert.AlertType.INFORMATION);
        } catch (NumberFormatException e) {
            showAlert("Erreur", "Année invalide !", Alert.AlertType.ERROR);
        }
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

        int requestedDays = (int) (endDate.toEpochDay() - startDate.toEpochDay()) + 1;
        int remainingDays = getRemainingLeaveDays(STATIC_USER_ID);

        if (requestedDays > remainingDays) {
            showAlert("Erreur", "Vous avez seulement " + remainingDays + " jours de congé restants.", Alert.AlertType.ERROR);
            return;
        }

        String typeConge = congeComboBox.getValue();
        String justification = justificationField.getText().trim();
        String autre = autreCongeField.getText().trim();

        if ("Maladie".equals(typeConge) && (selectedFile == null || !selectedFile.exists() || !selectedFile.canRead())) {
            showAlert("Erreur", "Veuillez téléverser un certificat médical valide.", Alert.AlertType.ERROR);
            return;
        }

        if ("Autre".equals(typeConge) && autre.isEmpty()) {
            showAlert("Erreur", "Veuillez spécifier le type de congé dans le champ 'Autre'.", Alert.AlertType.ERROR);
            return;
        }

        insertLeaveRequest(typeConge, autre, justification, startDate, endDate, selectedFile, requestedDays);
    }

    private int getRemainingLeaveDays(long userId) {
        String query = "SELECT solde_conge FROM user WHERE id = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setLong(1, userId);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt("solde_conge");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private void insertLeaveRequest(String typeConge, String autre, String justification, LocalDate startDate, LocalDate endDate, File certificateFile, int requestedDays) {
        String sql = "INSERT INTO demande_conge (type_congé, autre, justification, status, date_debut, date_fin, certificate, user_id) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        String updateSoldeQuery = "UPDATE user SET solde_conge = solde_conge - ? WHERE id = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql);
             PreparedStatement updateSoldeStatement = connection.prepareStatement(updateSoldeQuery)) {

            connection.setAutoCommit(false);

            preparedStatement.setString(1, typeConge);
            preparedStatement.setString(2, autre.isEmpty() ? null : autre);
            preparedStatement.setString(3, justification);
            preparedStatement.setString(4, "PENDING");
            preparedStatement.setDate(5, Date.valueOf(startDate));
            preparedStatement.setDate(6, Date.valueOf(endDate));
            preparedStatement.setLong(8, STATIC_USER_ID);

            if (certificateFile != null && certificateFile.exists() && certificateFile.canRead()) {
                try (FileInputStream fis = new FileInputStream(certificateFile)) {
                    preparedStatement.setBinaryStream(7, fis, (int) certificateFile.length());
                }
            } else {
                preparedStatement.setNull(7, java.sql.Types.BLOB);
            }

            preparedStatement.executeUpdate();

            updateSoldeStatement.setInt(1, requestedDays);
            updateSoldeStatement.setLong(2, STATIC_USER_ID);
            updateSoldeStatement.executeUpdate();

            connection.commit();
            showAlert("Succès", "Demande de congé soumise avec succès !", Alert.AlertType.INFORMATION);
        } catch (SQLException | IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Erreur lors de la soumission de la demande de congé : " + e.getMessage(), Alert.AlertType.ERROR);
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
