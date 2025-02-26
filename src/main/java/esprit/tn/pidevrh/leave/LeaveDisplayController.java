package esprit.tn.pidevrh.leave;

import esprit.tn.pidevrh.connection.DatabaseConnection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.IOException;
import java.sql.*;
import java.time.LocalDate;

public class LeaveDisplayController {
    @FXML private ListView<Leave> leaveListView;
    private ObservableList<Leave> leaveRequests = FXCollections.observableArrayList();

    private final long STATIC_USER_ID = 5;

    @FXML
    public void initialize() {
        loadLeaveRequests();

        leaveListView.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(Leave leave, boolean empty) {
                super.updateItem(leave, empty);
                if (empty || leave == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    Label typeLabel = new Label("Type: " + leave.getTypeConge());
                    Label autreLabel = new Label("Autre: " + (leave.getAutre() != null ? leave.getAutre() : "N/A"));
                    Label justificationLabel = new Label("Justification: " + (leave.getJustification() != null ? leave.getJustification() : "N/A"));
                    Label statusLabel = new Label("Statut: " + leave.getStatus());
                    Label startDateLabel = new Label("Début: " + leave.getDateDebut());
                    Label endDateLabel = new Label("Fin: " + leave.getDateFin());

                    Button editButton = new Button("📝 Modifier");
                    editButton.setOnAction(e -> editLeaveRequest(leave));

                    Button deleteButton = new Button("🗑️ Supprimer");
                    deleteButton.setOnAction(e -> deleteLeaveRequest(leave.getId()));

                    VBox detailsBox = new VBox(5, typeLabel, autreLabel, justificationLabel, statusLabel, startDateLabel, endDateLabel);
                    HBox actionBox = new HBox(10, editButton, deleteButton);
                    VBox fullBox = new VBox(10, detailsBox, actionBox);

                    setGraphic(fullBox);
                }
            }
        });
    }


    protected void loadLeaveRequests() {
        String query = "SELECT id, user_id, type_congé, autre, justification, status, date_debut, date_fin, certificate " +
                "FROM demande_conge WHERE user_id = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setLong(1, STATIC_USER_ID);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                leaveRequests.clear();
                while (resultSet.next()) {
                    leaveRequests.add(new Leave(
                            resultSet.getInt("id"),
                            resultSet.getInt("user_id"),
                            resultSet.getString("type_congé"),
                            resultSet.getString("autre"),
                            resultSet.getString("justification"),
                            resultSet.getString("status"),
                            resultSet.getDate("date_debut").toLocalDate(),
                            resultSet.getDate("date_fin").toLocalDate(),
                            resultSet.getBytes("certificate")
                    ));
                }
                leaveListView.setItems(leaveRequests);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Permet de modifier une demande de congé.
     */
    private void editLeaveRequest(Leave leave) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Leave/LeaveEdit.fxml"));
            Parent root = loader.load();

            LeaveEditController editController = loader.getController();
            editController.setLeaveData(leave, this);

            Stage stage = new Stage();
            stage.setTitle("Modifier la Demande de Congé");
            stage.setScene(new Scene(root));
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void deleteLeaveRequest(int id) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Voulez-vous vraiment supprimer cette demande ?", ButtonType.YES, ButtonType.NO);
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                String query = "DELETE FROM demande_conge WHERE id = ?";
                try (Connection connection = DatabaseConnection.getConnection();
                     PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                    preparedStatement.setInt(1, id);
                    preparedStatement.executeUpdate();
                    loadLeaveRequests();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
