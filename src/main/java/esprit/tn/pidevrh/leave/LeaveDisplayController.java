package esprit.tn.pidevrh.leave;

import esprit.tn.pidevrh.connection.DatabaseConnection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Callback;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

public class LeaveDisplayController {
    @FXML private ListView<Leave> leaveListView;
    private ObservableList<Leave> leaveRequests = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        loadLeaveRequests();

        // Custom rendering for ListView items with action buttons
        leaveListView.setCellFactory(new Callback<>() {
            @Override
            public ListCell<Leave> call(ListView<Leave> param) {
                return new ListCell<>() {
                    @Override
                    protected void updateItem(Leave leave, boolean empty) {
                        super.updateItem(leave, empty);
                        if (empty || leave == null) {
                            setText(null);
                            setGraphic(null);
                        } else {
                            // Labels for leave details
                            Label typeLabel = new Label("Type: " + leave.getTypeConge());
                            Label autreLabel = new Label("Autre: " + leave.getAutre());
                            Label justificationLabel = new Label("Justification: " + leave.getJustification());
                            Label statusLabel = new Label("Status: " + leave.getStatus());
                            Label startDateLabel = new Label("Début: " + leave.getDateDebut());
                            Label endDateLabel = new Label("Fin: " + leave.getDateFin());

                            // Buttons for action
                            Button approveButton = new Button("✅ Approuver");
                            approveButton.setOnAction(e -> updateLeaveStatus(leave.getId(), "Approuvé"));

                            Button rejectButton = new Button("❌ Rejeter");
                            rejectButton.setOnAction(e -> updateLeaveStatus(leave.getId(), "Rejeté"));

                            // Layout for details and buttons
                            VBox detailsBox = new VBox(5, typeLabel, autreLabel, justificationLabel, statusLabel, startDateLabel, endDateLabel);
                            HBox actionBox = new HBox(10, approveButton, rejectButton);
                            VBox fullBox = new VBox(10, detailsBox, actionBox);

                            setGraphic(fullBox);
                        }
                    }
                };
            }
        });
    }

    private void loadLeaveRequests() {
        String query = "SELECT id, type_congé, autre, justification, status, date_debut, date_fin FROM demande_conge";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query);
             ResultSet resultSet = preparedStatement.executeQuery()) {

            while (resultSet.next()) {
                leaveRequests.add(new Leave(
                        resultSet.getInt("id"),
                        resultSet.getString("type_congé"),
                        resultSet.getString("autre"),
                        resultSet.getString("justification"),
                        resultSet.getString("status"),
                        resultSet.getDate("date_debut").toLocalDate(),
                        resultSet.getDate("date_fin").toLocalDate()
                ));
            }

            leaveListView.setItems(leaveRequests);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void updateLeaveStatus(int id, String newStatus) {
        String query = "UPDATE demande_conge SET status = ? WHERE id = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setString(1, newStatus);
            preparedStatement.setInt(2, id);
            preparedStatement.executeUpdate();

            // Refresh list view after update
            leaveRequests.clear();
            loadLeaveRequests();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
