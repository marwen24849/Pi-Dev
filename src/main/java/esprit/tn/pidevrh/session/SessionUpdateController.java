package esprit.tn.pidevrh.session;

import esprit.tn.pidevrh.connection.DatabaseConnection;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

public class SessionUpdateController {

    @FXML
    private TextField salleField;

    @FXML
    private DatePicker datePicker;

    @FXML
    private RadioButton presentielRadio;
    @FXML
    private HBox salleHBox;
    @FXML
    private HBox dateHBox;
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
            salleHBox.setVisible(false);  // Hide the "Salle" HBox for Online
        } else {
            presentielRadio.setSelected(true);
            salleHBox.setVisible(true);  // Show the "Salle" HBox for Présentiel
        }

        // Always ensure that the dateHBox is visible
        dateHBox.setVisible(true);  // Date is always visible
    }


    @FXML
    private void handleSessionTypeChange() {
        // Always show the "Date" label and DatePicker
        dateHBox.setVisible(true);

        // Show the "Salle" label and TextField only if "Présentiel" is selected
        if (presentielRadio.isSelected()) {
            salleHBox.setVisible(true);  // Show the "Salle" label and TextField
        } else {
            salleHBox.setVisible(false); // Hide the "Salle" label and TextField
        }
    }



    @FXML
    private void handleSave() {
        // Check if all fields are filled out
        if ((presentielRadio.isSelected() && salleField.getText().isEmpty()) || datePicker.getValue() == null) {
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

        // Set the date regardless of whether the session is online or not
        if (datePicker.getValue() != null) {
            session.setDate(datePicker.getValue());  // Set the date from the DatePicker
        }

        // Set salle only if "Présentiel" is selected, else set it to null
        if (presentielRadio.isSelected()) {
            session.setSalle(salleField.getText());
        } else {
            session.setSalle(null);  // Set salle to null for online sessions
        }

        // Generate Zoom link if session is online
        if (onlineRadio.isSelected()) {
            String zoomLink = ZoomMeetingCreatorServerOAuth.createZoomMeeting();
            if (zoomLink == null) {
                showAlert("Erreur", "Erreur lors de la création de la réunion Zoom.");
                return;
            }
            session.setRoomLink(zoomLink);
        } else {
            session.setRoomLink(null); // Set Zoom link to null for présentiel sessions
        }

        // Save changes to the database
        String sql = "UPDATE session SET date=?, salle=?, is_online=?, link=? WHERE id=?";

        try (Connection connection = DatabaseConnection.getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setDate(1, session.getDate() != null ? java.sql.Date.valueOf(session.getDate()) : null);
            preparedStatement.setString(2, session.getSalle());
            preparedStatement.setBoolean(3, session.isOnline());
            preparedStatement.setString(4, session.getRoomLink());
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

    // Check if a session with the same date and salle already exists
    private boolean isSessionConflict(LocalDate date, String salle) {
        String query = "SELECT COUNT(*) FROM session WHERE date = ? AND salle = ? AND id != ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement statement = conn.prepareStatement(query)) {
            statement.setDate(1, java.sql.Date.valueOf(date));
            statement.setString(2, salle);
            statement.setLong(3, session.getId()); // Exclude the current session by checking against its ID

            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                int count = resultSet.getInt(1);
                return count > 0; // If count > 0, a conflict exists
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Erreur SQL", "Erreur lors de la vérification des conflits de session.");
        }

        return false; // Default to false if an error occurs
    }

    // Ensure the number of sessions does not exceed the allowed limit for the formation
    public boolean canUpdateSession(long formationId) {
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
