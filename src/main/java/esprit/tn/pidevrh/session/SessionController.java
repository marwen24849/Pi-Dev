package esprit.tn.pidevrh.session;

import com.mysql.cj.exceptions.NumberOutOfRange;
import esprit.tn.pidevrh.connection.DatabaseConnection;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import lombok.Setter;

import java.sql.*;
import java.time.LocalDate;


public class SessionController {

    public void setFormationId(long formationId) {
        this.formationId = formationId;
    }

    @FXML
    private TextField salle;
    @FXML
    private Label salleLabel;

    @FXML
    private DatePicker date;

    private long formationId;

    @FXML
    private RadioButton presentielRadio;

    @FXML
    private RadioButton onlineRadio;

    private ToggleGroup sessionTypeGroup;  // Define it as a private field

    public void initialize() {
        // Disable past dates in DatePicker
        date.setDayCellFactory(picker -> new DateCell() {
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
        handleSessionTypeChange(null); // Call this to check the default state
    }

    @FXML
    void handleSessionTypeChange(ActionEvent event) {
        if (presentielRadio.isSelected()) {
            salle.setVisible(true);
            salleLabel.setVisible(true);
        } else {
            salle.setVisible(false);
            salleLabel.setVisible(false);
        }
    }


    @FXML
    void handleAddSession() {
        // Check if all fields are filled out
        if ((presentielRadio.isSelected() && salle.getText().isEmpty()) || date.getValue() == null) {
            showAlert("Erreur", "Tous les champs doivent être remplis.");
            return;
        }

        // Validate salle number if "Présentiel" is selected
        if (presentielRadio.isSelected()) {
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
        }

        if (!canAddSession(formationId)) {
            showAlert("Erreur", "Le nombre maximum de sessions pour cette formation est atteint.");
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            // SQL query to insert the session into the database
            String sql = "INSERT INTO session (formation_id, salle, date, is_online) VALUES (?, ?, ?, ?)";

            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setLong(1, formationId);  // Use the current formationId here
            ps.setString(2, presentielRadio.isSelected() ? salle.getText() : null); // Set salle only for presentiel
            ps.setDate(3, Date.valueOf(date.getValue()));
            ps.setBoolean(4, onlineRadio.isSelected()); // Set is_online based on selection

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


    public boolean canAddSession(long formationId) {
        String countQuery = "SELECT COUNT(*) FROM session WHERE formation_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement countPs = conn.prepareStatement(countQuery)) {

            countPs.setLong(1, formationId); // Set formationId in query
            ResultSet countRs = countPs.executeQuery(); // Execute query

            if (countRs.next()) {
                int sessionCount = countRs.getInt(1); // Get the count from the result set

                // Fetch the duration (duree) of the formation
                String dureeQuery = "SELECT duration FROM formation WHERE id = ?";
                try (PreparedStatement dureePs = conn.prepareStatement(dureeQuery)) {
                    dureePs.setLong(1, formationId);
                    ResultSet dureeRs = dureePs.executeQuery();

                    if (dureeRs.next()) {
                        int duree = dureeRs.getInt(1); // Get duree from result set
                        return sessionCount < duree; // Return true if sessions < duree
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Erreur SQL", "Erreur lors de la vérification des sessions existantes: " + e.getMessage());
        }

        return false; // Default to false if an error occurs
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
