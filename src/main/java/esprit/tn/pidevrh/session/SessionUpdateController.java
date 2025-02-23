package esprit.tn.pidevrh.session;

import esprit.tn.pidevrh.connection.DatabaseConnection;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.DateCell;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;

public class SessionUpdateController {

    @FXML
    private TextField salleField;

    @FXML
    private DatePicker datePicker;

    private Session session;

    public void setSession(Session session) {
        this.session = session;
        // Set the fields with the current session's values
        salleField.setText(session.getSalle());
        datePicker.setValue(session.getDate());
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
    }

    @FXML
    private void handleSave() {
        // Validate the form fields
        if (!validateForm()) {
            return;  // Return early if validation fails
        }
        if (salleField.getText().isEmpty() || datePicker.getValue() == null) {
            showAlert("Erreur", "Tous les champs doivent être remplis.");
            return;
        }

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

        String sql = "UPDATE session SET salle=?, date=? WHERE id=?";

        try (Connection connection = DatabaseConnection.getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, salleField.getText());
            preparedStatement.setDate(2, java.sql.Date.valueOf(datePicker.getValue()));
            preparedStatement.setLong(3, session.getId());

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

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private boolean validateForm() {
        // Validate Salle (it should not be empty)
        if (salleField.getText().trim().isEmpty()) {
            showAlert("Erreur", "Le champ 'Salle' ne peut pas être vide.");
            return false;
        }

        // Validate Date (it should be selected)
        if (datePicker.getValue() == null) {
            showAlert("Erreur", "La date doit être sélectionnée.");
            return false;
        }

        // If all validations pass
        return true;
    }
    private void notNumberAlert(String title, String message){
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    private void notValidNumberAlert(String title, String message){
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
