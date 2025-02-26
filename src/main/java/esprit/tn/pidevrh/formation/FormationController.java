package esprit.tn.pidevrh.formation;

import esprit.tn.pidevrh.connection.DatabaseConnection;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.AnchorPane;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class FormationController {



    @FXML
    private TextField description;

    @FXML
    private TextField duration;

    @FXML
    private TextField titleField;

    @FXML
    private TextField capacity;

    public void initialize() {

        titleField.setText("");
        description.setText("");
        duration.setText("");
        capacity.setText("");
    }


    @FXML
    void handleAddFormation(ActionEvent event) {
        if(titleField.getText().isEmpty() ||description.getText().isEmpty() || duration.getText().isEmpty() || capacity.getText().isEmpty()) {
            showAlert("Erreur", "Tous les champs doivent être remplis.");
            return;
        }
        // Validate duration
        int durationValue;
        try {
            durationValue = Integer.parseInt(duration.getText());
            if (durationValue <= 0) { // Check if duration is a positive integer
                notValidNumberAlert("Erreur", "La durée doit être un nombre entier positif.");
                return;
            }
        } catch (NumberFormatException e) {
            notNumberAlert("Erreur", "La durée doit être un nombre entier.");
            return;
        }

        // Validate capacity
        int capacityValue;
        try {
            capacityValue = Integer.parseInt(capacity.getText());
            if (capacityValue <= 0) { // Check if capacity is a positive integer
                notValidNumberAlert("Erreur", "La capacité doit être un nombre entier positif.");
                return;
            }
        } catch (NumberFormatException e) {
            notNumberAlert("Erreur", "La capacité doit être un nombre entier.");
            return;
        }
        try(Connection conn = DatabaseConnection.getConnection()){
            String sql = "insert into formation (title, description, duration, capacity) VALUES (?, ?, ?, ?)";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, titleField.getText());
            ps.setString(2, description.getText());
            ps.setInt(3, Integer.parseInt(duration.getText()));
            ps.setInt(4, Integer.parseInt(capacity.getText()));
            ps.executeUpdate();
            addedAlert("Succès", "La Formation a été ajoutée avec succès !");
            initialize();
        }catch(SQLException e){
            showAlert("Erreur SQL", "Erreur lors de l'ajout de la formation dans la base de données.");
        }

    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
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
    private void addedAlert(String title, String message){
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }


}
