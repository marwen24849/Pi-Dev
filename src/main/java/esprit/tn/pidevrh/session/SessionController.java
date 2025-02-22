package esprit.tn.pidevrh.session;

import com.mysql.cj.exceptions.NumberOutOfRange;
import esprit.tn.pidevrh.connection.DatabaseConnection;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import lombok.Setter;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;


public class SessionController {

    @FXML
    private TextField salle;

    @FXML
    private DatePicker date;

    private long formationId;






    public void initialize() {

        salle.setText("");

    }

    public void setFormationId(long formationId) {
        this.formationId = formationId;
    }

    @FXML
    void handleAddSession() {
        // Check if all fields are filled out
        if (salle.getText().isEmpty() || date.getValue() == null) {
            showAlert("Erreur", "Tous les champs doivent être remplis.");
            return;
        }

        try {
            int salleNumber = Integer.parseInt(salle.getText());

            if (salleNumber <= 0) {  // Check if the number is negative or zero
                notValidNumberAlert("Erreur", "La salle doit être un nombre positif.");
                return;
            }
        } catch (NumberFormatException e) {
            notNumberAlert("Erreur", "La salle doit être un nombre entier.");
            return;
        }



        try (Connection conn = DatabaseConnection.getConnection()) {
            // SQL query to insert the session into the database
            String sql = "INSERT INTO session (formation_id, salle, date) VALUES (?, ?, ?)";

            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setLong(1, formationId);  // Use the current formationId here
            ps.setString(2, salle.getText());
            ps.setDate(3, Date.valueOf(date.getValue()));

            // Check the SQL and parameters before execution (for debugging)
            System.out.println("SQL: " + sql);
            System.out.println("Parameters: " + formationId + ", " + salle.getText() + ", " + Date.valueOf(date.getValue()));

            // Execute the query
            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                addedAlert("Succès", "La Session a été ajoutée avec succès !");
                initialize();  // Reset the form after successful addition
            } else {
                showAlert("Erreur", "Aucune session ajoutée.");
            }
        } catch (SQLException e) {
            // Log the error and show alert if something goes wrong
            e.printStackTrace();
            showAlert("Erreur SQL", "Erreur lors de l'ajout de la session dans la base de données. Détails : " + e.getMessage());
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
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
