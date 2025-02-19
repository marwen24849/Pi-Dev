package esprit.tn.pidevrh.Poste;

import javafx.fxml.FXML;
import javafx.scene.control.*;

public class PosteController {

    @FXML
    private TextField userField;

    @FXML
    private TextField contentField;

    @FXML
    private TextField salaireField;

    @FXML
    private TextArea descriptionArea;

    @FXML
    private DatePicker datePostePicker;

    @FXML
    private TextField stateField;

    @FXML
    private Button addPosteButton;

    @FXML
    public void addPoste() {
        try {

            String content = contentField.getText();
            double salaire = Double.parseDouble(salaireField.getText());
            String description = descriptionArea.getText();
            java.sql.Date datePoste = java.sql.Date.valueOf(datePostePicker.getValue());
            String state = stateField.getText();

            // Create a new Poste object and save it (e.g., using a service)
            Poste poste = new Poste(content, salaire, description, datePoste, state);
            // posteService.addPoste(poste); // Uncomment and implement this in your service

            // Clear the form after submission
            clearForm();
        } catch (Exception e) {
            e.printStackTrace();
            // Show an error message
            showAlert("Erreur", "Veuillez remplir tous les champs correctement.");
        }
    }

    private void clearForm() {
        contentField.clear();
        salaireField.clear();
        descriptionArea.clear();
        datePostePicker.setValue(null);
        stateField.clear();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}