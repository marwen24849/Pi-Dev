package esprit.tn.pidevrh.Poste;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.util.StringConverter;

import java.util.List;

public class PosteController {

    @FXML
    private TextField contentField, salaireField;

    @FXML
    private TextArea descriptionField;

    @FXML
    private DatePicker datePicker;

    @FXML
    private ComboBox<User> userComboBox;

    @FXML
    private Button submitButton;

    @FXML
    private ComboBox<String> stateComboBox; // Correct ComboBox for Active/Not Active

    private final PosteService posteService = new PosteService();


    @FXML
    public void initialize() {

        loadUsers();
        // Populate the stateComboBox
        stateComboBox.getItems();
    }

    private void loadUsers() {
        List<User> users = posteService.getAvailableUsers();
        ObservableList<User> userObservableList = FXCollections.observableArrayList(users);
        userComboBox.setItems(userObservableList);

        // Ensure only name is displayed
        userComboBox.setConverter(new StringConverter<User>() {
            @Override
            public String toString(User user) {
                return (user != null) ? user.getFirstname() + " " + user.getLastname() : "";
            }

            @Override
            public User fromString(String string) {
                return null; // Not needed for selection
            }
        });



    }


    @FXML
    public void addPoste() {
        try {
            // Validate inputs
            if (contentField.getText().isEmpty() || salaireField.getText().isEmpty() ||
                    descriptionField.getText().isEmpty() || stateComboBox.getValue() == null ||
                    datePicker.getValue() == null) {
                showAlert("Erreur", "Veuillez remplir tous les champs.");
                return;
            }

            String content = contentField.getText();
            double salaire = Double.parseDouble(salaireField.getText());
            String description = descriptionField.getText();
            java.sql.Date datePoste = java.sql.Date.valueOf(datePicker.getValue());
            String state = stateComboBox.getValue(); // Corrected reference

            // Get selected user
            User selectedUser = userComboBox.getValue();
            if (selectedUser == null) {
                showAlert("Erreur", "Veuillez sélectionner un utilisateur.");
                return;
            }
            Long userId = selectedUser.getId(); // Extract user ID

            // Create Poste instance
            Poste poste = new Poste(userId,content, salaire, description, datePoste, state );


            posteService.addPoste(poste, userId);


            // Clear form after successful submission
            clearForm();
        } catch (NumberFormatException e) {
            showAlert("Erreur", "Veuillez entrer un salaire valide.");
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Une erreur est survenue.");
        }
    }

    private void clearForm() {
        contentField.clear();
        salaireField.clear();
        descriptionField.clear();
        datePicker.setValue(null);
        stateComboBox.setValue(null); // Clear state selection
        userComboBox.setValue(null);
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
