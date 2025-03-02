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

public class inscriptionSessionController {

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
                session.setOnline(resultSet.getBoolean("is_online")); // Set online status
                session.setRoomLink(resultSet.getString("link")); // Set the link for online sessions

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
                dateLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #2c3e50; -fx-font-style: italic; -fx-wrap-text: true; -fx-max-width: 300px;");

                Label typeLabel = new Label("Type: " + (session.isOnline() ? "En ligne" : "Présentiel"));
                typeLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #2980b9; -fx-font-style: italic;");


                // Conditional display of salle or link
                Label salleLabel = new Label();
                Label linkLabel = new Label();
                if (session.isOnline()) {
                    linkLabel.setText("Link: " + session.getRoomLink());
                    linkLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #F08080; -fx-font-style: italic;");

                } else {
                    salleLabel.setText("Salle: " + session.getSalle());
                }


                VBox sessionBox = new VBox(5, dateLabel, typeLabel);

                // Add salle or link based on session type
                if (session.isOnline()) {
                    sessionBox.getChildren().add(linkLabel);
                } else {
                    sessionBox.getChildren().add(salleLabel);
                }

                sessionBox.setStyle("-fx-padding: 10px; -fx-background-color: #ecf0f1; -fx-border-color: #bdc3c7; -fx-border-radius: 5px;");

                setGraphic(sessionBox);
            }
        }
    }


}
