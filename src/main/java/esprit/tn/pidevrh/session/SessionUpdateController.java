package esprit.tn.pidevrh.session;

import esprit.tn.pidevrh.connection.DatabaseConnection;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;

public class SessionUpdateController {

    @FXML
    private TextField salleField;

    @FXML
    private TextField linkField;

    @FXML
    private DatePicker datePicker;

    @FXML
    private RadioButton presentielRadio;

    @FXML
    private RadioButton onlineRadio;

    @FXML
    private ToggleGroup sessionTypeGroup; // Ensure this matches the fx:id in FXML

    private Session session;

    public void initialize() {
        // Disable past dates in DatePicker
        datePicker.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                if (item.isBefore(LocalDate.now())) {
                    setDisable(true);
                    setStyle("-fx-background-color: #EEEEEE;");
                }
            }
        });

        // Initialize the ToggleGroup
        sessionTypeGroup = new ToggleGroup();

        // Set the ToggleGroup for both RadioButtons
        presentielRadio.setToggleGroup(sessionTypeGroup);
        onlineRadio.setToggleGroup(sessionTypeGroup);

        // Set default selection (optional)
        presentielRadio.setSelected(true);  // Makes sure "Présentiel" is selected by default

        // Ensure proper behavior when initialized
        handleSessionTypeChange(); // Call this to check the default state
    }

    public void setSession(Session session) {
        this.session = session;

        // Set the fields with the current session's values
        salleField.setText(session.getSalle());
        datePicker.setValue(session.getDate());

        // Set session type (online or présentiel)
        if (session.isOnline()) {
            onlineRadio.setSelected(true);
            linkField.setText(session.getRoomLink());
            salleField.setVisible(false);
            linkField.setVisible(true);
        } else {
            presentielRadio.setSelected(true);
            salleField.setVisible(true);
            linkField.setVisible(false);
        }
    }

    @FXML
    private void handleSessionTypeChange() {
        // Show/hide salle and link fields based on session type
        if (presentielRadio.isSelected()) {
            salleField.setVisible(true);
            linkField.setVisible(false);
        } else {
            salleField.setVisible(false);
            linkField.setVisible(true);
        }
    }

    @FXML
    private void handleSave() {
        // Check if all fields are filled out
        if ((presentielRadio.isSelected() && salleField.getText().isEmpty()) ||
                (onlineRadio.isSelected() && linkField.getText().isEmpty()) ||
                datePicker.getValue() == null) {
            showAlert("Erreur", "Tous les champs doivent être remplis.");
            return;
        }

        // Validate salle number if "Présentiel" is selected
        if (presentielRadio.isSelected()) {
            try {
                int salleNumber = Integer.parseInt(salleField.getText());

                if (salleNumber <= 0) {  // Check if the number is negative or zero
                    notValidNumberAlert("Erreur", "La salle doit être un nombre positif.");
                    return;
                }
            } catch (NumberFormatException e) {
                notNumberAlert("Erreur", "La salle doit être un nombre entier.");
                return;
            }
        }

        // Update session details
        session.setOnline(onlineRadio.isSelected());
        session.setDate(datePicker.getValue());
        session.setSalle(salleField.getText());
        session.setRoomLink(linkField.getText());

        // Save changes to the database
        String sql = "UPDATE session SET date=?, salle=?, is_online=?, link=? WHERE id=?";

        try (Connection connection = DatabaseConnection.getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setDate(1, java.sql.Date.valueOf(session.getDate()));
            preparedStatement.setString(2, session.isOnline() ? null : session.getSalle());
            preparedStatement.setBoolean(3, session.isOnline());
            preparedStatement.setString(4, session.isOnline() ? session.getRoomLink() : null);
            preparedStatement.setLong(5, session.getId());

            int rowsUpdated = preparedStatement.executeUpdate();

            if (rowsUpdated > 0) {
                showAlert("Succès", "Session mise à jour avec succès!");
            } else {
                showAlert("Erreur", "Aucune session n'a été modifiée.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Erreur SQL", "Erreur lors de la modification de la session dans la base de données.");
        }

        // Close the stage after update
        Stage stage = (Stage) salleField.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void handleCancel() {
        // Close the stage without saving
        Stage stage = (Stage) salleField.getScene().getWindow();
        stage.close();
    }

    private boolean validateForm() {
        // Validate Date
        if (datePicker.getValue() == null) {
            showAlert("Erreur", "La date ne peut pas être vide.");
            return false;
        }

        // Validate Salle (if présentiel)
        if (presentielRadio.isSelected() && salleField.getText().trim().isEmpty()) {
            showAlert("Erreur", "La salle ne peut pas être vide pour une session présentielle.");
            return false;
        }

        // Validate Link (if online)
        if (onlineRadio.isSelected() && linkField.getText().trim().isEmpty()) {
            showAlert("Erreur", "Le lien ne peut pas être vide pour une session en ligne.");
            return false;
        }

        // If all validations pass
        return true;
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void notNumberAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void notValidNumberAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}