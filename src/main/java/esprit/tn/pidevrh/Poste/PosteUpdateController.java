package esprit.tn.pidevrh.Poste;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

public class PosteUpdateController {

    @FXML
    private TextField contentField;
    @FXML
    private TextField salaireField;
    @FXML
    private TextArea descriptionField;
    @FXML
    private DatePicker datePicker;
    @FXML
    private ComboBox<String> stateComboBox;
    @FXML
    private Button saveButton;
    @FXML
    private Button cancelButton;

    private PosteService posteService = new PosteService();
    private Poste selectedPoste;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    public void setPoste(Poste poste) {
        this.selectedPoste = poste;

        // Load existing data into the form
        contentField.setText(poste.getContent());
        salaireField.setText(String.valueOf(poste.getSalaire()));
        descriptionField.setText(poste.getDescription());

        // Convert Date to LocalDate for DatePicker
        LocalDate localDate = new java.sql.Date(poste.getDatePoste().getTime()).toLocalDate();
        datePicker.setValue(localDate);

        // Populate state dropdown
        stateComboBox.getItems().addAll("Active", "Inactive");
        stateComboBox.setValue(poste.getState());
    }

    @FXML
    private void handleSave() {
        if (validateInput()) {
            selectedPoste.setContent(contentField.getText());
            selectedPoste.setSalaire(Double.parseDouble(salaireField.getText()));
            selectedPoste.setDescription(descriptionField.getText());

            // Convert LocalDate to Date
            LocalDate localDate = datePicker.getValue();
            java.sql.Date sqlDate = java.sql.Date.valueOf(localDate);
            selectedPoste.setDatePoste(sqlDate); // ✅ Correct

            selectedPoste.setState(stateComboBox.getValue());

            // Update in database
            posteService.updatePoste(selectedPoste);

            showAlert("Success", "Poste updated successfully!", Alert.AlertType.INFORMATION);

            // Close the dialog
            ((Stage) saveButton.getScene().getWindow()).close();
        }
    }

    @FXML
    private void handleCancel() {
        ((Stage) cancelButton.getScene().getWindow()).close();
    }

    private boolean validateInput() {
        String errorMessage = "";

        if (contentField.getText() == null || contentField.getText().isEmpty()) {
            errorMessage += "Content cannot be empty!\n";
        }
        if (salaireField.getText() == null || salaireField.getText().isEmpty()) {
            errorMessage += "Salaire cannot be empty!\n";
        } else {
            try {
                Double.parseDouble(salaireField.getText());
            } catch (NumberFormatException e) {
                errorMessage += "Salaire must be a valid number!\n";
            }
        }
        if (datePicker.getValue() == null) {
            errorMessage += "Please select a date!\n";
        }

        if (!errorMessage.isEmpty()) {
            showAlert("Validation Error", errorMessage, Alert.AlertType.ERROR);
            return false;
        }
        return true;
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
