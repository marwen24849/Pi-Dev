package esprit.tn.pidevrh.leave;

import esprit.tn.pidevrh.email.EmailService;

import esprit.tn.pidevrh.connection.DatabaseConnection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import java.io.*;
import java.sql.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class LeaveManagementController {

    @FXML private ListView<Leave> leaveListView;
    private ObservableList<Leave> leaveRequests = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        loadAllLeaveRequests();
        leaveListView.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(Leave leave, boolean empty) {
                super.updateItem(leave, empty);
                if (empty || leave == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    Label userLabel = new Label("Utilisateur: " + getUserName(leave.getUserId()));
                    Label typeLabel = new Label("Type: " + leave.getTypeConge());
                    Label startDateLabel = new Label("Début: " + leave.getDateDebut());
                    Label endDateLabel = new Label("Fin: " + leave.getDateFin());

                    Button approveButton = new Button("✅ Approuver");
                    approveButton.setOnAction(e -> updateLeaveStatus(leave.getId(), "APPROVED"));

                    Button rejectButton = new Button("❌ Refuser");
                    rejectButton.setOnAction(e -> updateLeaveStatus(leave.getId(), "REJECTED"));

                    VBox detailsBox = new VBox(5, userLabel, typeLabel, startDateLabel, endDateLabel);
                    setGraphic(new VBox(10, detailsBox, new HBox(10, approveButton, rejectButton)));
                }
            }
        });
    }

    protected void loadAllLeaveRequests() {
        String query = "SELECT id, user_id, type_congé, COALESCE(status, 'PENDING') AS status, date_debut, date_fin FROM demande_conge";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query);
             ResultSet resultSet = preparedStatement.executeQuery()) {
            leaveRequests.clear();
            while (resultSet.next()) {
                leaveRequests.add(new Leave(
                        resultSet.getInt("id"),
                        resultSet.getInt("user_id"),
                        resultSet.getString("type_congé"),
                        getUserEmail(resultSet.getInt("user_id")),
                        resultSet.getString("status"),
                        null,
                        resultSet.getDate("date_debut").toLocalDate(),
                        resultSet.getDate("date_fin").toLocalDate(),
                        null
                ));
            }
            leaveListView.setItems(leaveRequests);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void updateLeaveStatus(int leaveId, String newStatus) {
        String updateQuery = "UPDATE demande_conge SET status = ? WHERE id = ?";
        String soldeQuery = "UPDATE user SET solde_conge = solde_conge - ? WHERE id = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement updateStmt = connection.prepareStatement(updateQuery);
             PreparedStatement soldeStmt = connection.prepareStatement(soldeQuery)) {

            updateStmt.setString(1, newStatus);
            updateStmt.setInt(2, leaveId);
            updateStmt.executeUpdate();

            if ("APPROVED".equalsIgnoreCase(newStatus)) {
                Leave leave = getLeaveById(leaveId);
                if (leave != null) {
                    long days = ChronoUnit.DAYS.between(leave.getDateDebut(), leave.getDateFin());

                    if (!hasEnoughLeaveBalance(leave.getUserId(), days)) {
                        showAlert("Erreur", "Solde de congé insuffisant.", Alert.AlertType.ERROR);
                        return;
                    }

                    soldeStmt.setLong(1, days);
                    soldeStmt.setInt(2, leave.getUserId());
                    soldeStmt.executeUpdate();

                    File pdfFile = PDFGenerator.generateLeavePDF(leave);
                    String userEmail = getUserEmail(leaveId);

                }
            }

            loadAllLeaveRequests();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private boolean hasEnoughLeaveBalance(int userId, long requestedDays) {
        String query = "SELECT solde_conge FROM user WHERE id = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement stmt = connection.prepareStatement(query)) {

            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("solde_conge") >= requestedDays;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private Leave getLeaveById(int leaveId) {
        String query = "SELECT user_id, type_congé, date_debut, date_fin FROM demande_conge WHERE id = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement stmt = connection.prepareStatement(query)) {

            stmt.setInt(1, leaveId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new Leave(
                        leaveId,
                        rs.getInt("user_id"),
                        rs.getString("type_congé"),
                        getUserEmail(rs.getInt("user_id")),
                        "APPROVED",
                        null,
                        rs.getDate("date_debut").toLocalDate(),
                        rs.getDate("date_fin").toLocalDate(),
                        null
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private String getUserName(int userId) {
        String query = "SELECT first_name, last_name FROM user WHERE id = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement stmt = connection.prepareStatement(query)) {

            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("first_name") + " " + rs.getString("last_name");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "Utilisateur inconnu";
    }

    private String getUserEmail(int userId) {
        String query = "SELECT email FROM user WHERE id = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement stmt = connection.prepareStatement(query)) {

            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("email");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "unknown@example.com";
    }

    private void showAlert(String title, String message, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
