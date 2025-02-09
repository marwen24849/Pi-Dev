package esprit.tn.pidevrh.session;

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

    @Setter
    private int currentFormationId;




    public void initialize() {

        salle.setText("");

    }


    @FXML
    void handleAddSession(ActionEvent event) {
        if (salle.getText().isEmpty() || date.getValue() == null) {
            showAlert("Erreur", "Tous les champs doivent être remplis.");
            return;
        }
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "insert into session (salle, date) VALUES (?, ?)";

            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, salle.getText());
            ps.setDate(2, Date.valueOf(date.getValue()));

            // Check the SQL and parameters before execution
            System.out.println("SQL: " + sql);
            System.out.println("Parameters: " + salle.getText() + ", " + Date.valueOf(date.getValue()));

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                addedAlert("Succès", "La Session a été ajoutée avec succès !");
                initialize();
            } else {
                showAlert("Erreur", "Aucune session ajoutée.");
            }
        } catch (SQLException e) {
            // Log the error message to help with troubleshooting
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
}
