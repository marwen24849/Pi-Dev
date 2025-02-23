package esprit.tn.pidevrh.session;

import esprit.tn.pidevrh.connection.DatabaseConnection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SessionListController {

    private long currentFormationId;

    @FXML
    private ListView<Session> sessionListView;  // Use ListView instead of TableView

    private ObservableList<Session> sessionList = FXCollections.observableArrayList(); // List of sessions

    @FXML
    public void initialize() {
        sessionListView.setCellFactory(param -> new SessionListCell()); // Set custom cell for displaying session
        loadSessions(); // Load sessions from the database
    }

    // Set the selected formation for the session list view
    public void setFormationId(long formationId) {
        this.currentFormationId = formationId;
        System.out.println("Formation ID received: " + formationId);
        loadSessionsForFormation();
    }

    private void loadSessionsForFormation() {
        // Here, fetch sessions from the database using formationId
        System.out.println("Loading sessions for Formation ID: " + currentFormationId);
        loadSessions();
    }

    private void loadSessions() {
        sessionList.clear();
        String query = "SELECT * FROM session WHERE formation_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement statement = conn.prepareStatement(query)) {
            statement.setLong(1, currentFormationId);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                Session session = new Session();
                session.setId(resultSet.getLong("id"));
                session.setDate(resultSet.getDate("date").toLocalDate());
                session.setSalle(resultSet.getString("salle"));

                sessionList.add(session);
            }
        } catch (SQLException e) {
            showAlert("Database Error", "Could not load sessions.");
        }

        sessionListView.setItems(sessionList); // Bind sessions to ListView
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    private void showAlert1(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void openAddSession(ActionEvent event) {
        try {
            // Load the AddSessionForm.fxml
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Fxml/Session/AddSessionForm.fxml"));
            AnchorPane addSessionForm = loader.load();

            SessionController controller = loader.getController();
            controller.setFormationId(currentFormationId);
            // Create a new stage (window)
            Stage addSessionStage = new Stage();
            addSessionStage.setTitle("Add Session");
            addSessionStage.initModality(Modality.APPLICATION_MODAL); // Makes it modal (blocks interaction with other windows)

            // Set the scene with the loaded AddSessionForm.fxml content
            Scene scene = new Scene(addSessionForm);
            addSessionStage.setScene(scene);

            // Show the window
            addSessionStage.showAndWait();
            loadSessions();

        } catch (IOException e) {
            showAlert("Error", "Error opening the Add Session form: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Custom ListCell for Session objects
    private class SessionListCell extends ListCell<Session> {
        @Override
        protected void updateItem(Session session, boolean empty) {
            super.updateItem(session, empty);
            if (empty || session == null) {
                setGraphic(null);
                setText(null);
            } else {
                // Create a layout for each session
                Label dateLabel = new Label("Date: " + session.getDate());
                Label salleLabel = new Label("Salle: " + session.getSalle());
                Button updateButton = new Button("Modifier");
                Button deleteButton = new Button("Supprimer");

                updateButton.setOnAction(event -> handleUpdate(session));  // Handle update
                deleteButton.setOnAction(event -> handleDelete(session));  // Handle delete

                // Layout for session cell
                HBox actionBox = new HBox(10, updateButton, deleteButton);
                VBox sessionBox = new VBox(5, dateLabel, salleLabel, actionBox);
                sessionBox.setStyle("-fx-padding: 10px; -fx-background-color: #ecf0f1; -fx-border-color: #bdc3c7; -fx-border-radius: 5px;");

                setGraphic(sessionBox);
            }
        }
    }

    private void handleUpdate(Session session) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Fxml/Session/EditSessionForm.fxml"));
            Parent root = loader.load();
            SessionUpdateController controller = loader.getController();
            controller.setSession(session);
            Stage stage = new Stage();
            stage.setTitle("Modifier la Session");
            stage.setScene(new Scene(root));
            stage.showAndWait();
            loadSessions(); // Reload the sessions list after updating
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleDelete(Session session) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Suppression");
        alert.setHeaderText("Êtes-vous sûr de vouloir supprimer cette session ?");
        alert.setContentText("Salle : " + session.getSalle() + "\nDate : " + session.getDate());
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                deleteSession(session.getId());
            }
        });
    }

    private void deleteSession(Long id) {
        try (Connection connection = DatabaseConnection.getConnection()) {
            String sql = "DELETE FROM session WHERE id=?";
            PreparedStatement p = connection.prepareStatement(sql);
            p.setLong(1, id);
            int rowsAffected = p.executeUpdate();
            if (rowsAffected > 0) {
                showAlert1("Succès", "La session a été supprimée avec succès.");
                loadSessions(); // Reload sessions after successful deletion
            } else {
                showAlert("Erreur", "Impossible de supprimer la session.");
            }
        } catch (SQLException e) {
            showAlert("Erreur de base de données", "Impossible de supprimer la session : " + id);
        }
    }
}
