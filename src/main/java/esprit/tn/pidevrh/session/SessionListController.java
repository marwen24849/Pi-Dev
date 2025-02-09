package esprit.tn.pidevrh.session;

import esprit.tn.pidevrh.connection.DatabaseConnection;
import esprit.tn.pidevrh.formation.Formation;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SessionListController {
    private Formation selectedFormation;

    @FXML
    private TableView<Session> sessionTableView;
    @FXML
    private TableColumn<Session, String> dateColumn, timeColumn, locationColumn;

    // Set the selected formation for the session list view
    public void setFormation(Formation formation) {
        this.selectedFormation = formation;
        loadSessions();
    }

    private void loadSessions() {
        ObservableList<Session> sessions = FXCollections.observableArrayList();
        String query = "SELECT * FROM session WHERE formation_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement statement = conn.prepareStatement(query)) {

            statement.setLong(1, selectedFormation.getId());
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                Session session = new Session();
                session.setId(resultSet.getLong("id"));
                session.setDate(resultSet.getDate("date").toLocalDate());
                session.setSalle(resultSet.getInt("salle"));

                sessions.add(session);

            }

        } catch (SQLException e) {
            showAlert("Database Error", "Could not load sessions.");
        }

        sessionTableView.setItems(sessions);
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
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

            // Create a new stage (window)
            Stage addSessionStage = new Stage();
            addSessionStage.setTitle("Add Session");
            addSessionStage.initModality(Modality.APPLICATION_MODAL); // Makes it modal (blocks interaction with other windows)

            // Set the scene with the loaded AddSessionForm.fxml content
            Scene scene = new Scene(addSessionForm);
            addSessionStage.setScene(scene);

            // Show the window
            addSessionStage.showAndWait();
        } catch (IOException e) {
            showAlert("Error", "Error opening the Add Session form: " + e.getMessage());
            e.printStackTrace();
        }
    }

}
