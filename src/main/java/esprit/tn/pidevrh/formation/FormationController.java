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

    public void initialize() {

        titleField.setText("");
        description.setText("");
        duration.setText("");
    }


    @FXML
    void handleAddFormation(ActionEvent event) {
        if(titleField.getText().isEmpty() ||description.getText().isEmpty() || duration.getText().isEmpty()){
            showAlert("Erreur", "Tous les champs doivent être remplis.");
            return;
        }
        try(Connection conn = DatabaseConnection.getConnection()){
            String sql = "insert into formation (title, description, duration) VALUES (?, ?, ?)";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, titleField.getText());
            ps.setString(2, description.getText());
            ps.setInt(3, Integer.parseInt(duration.getText()));
            ps.executeUpdate();
            showAlert("Succès", "La Formation a été ajoutée avec succès !");
            initialize();
        }catch(SQLException e){
            showAlert("Erreur SQL", "Erreur lors de l'ajout de la formation dans la base de données.");
        }

    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

}
