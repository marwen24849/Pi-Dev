package esprit.tn.pidevrh.leave;

import esprit.tn.pidevrh.connection.DatabaseConnection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

public class LeaveDisplayController {
    @FXML private TableView<Leave> leaveTable;
    @FXML private TableColumn<Leave, String> typeColumn;
    @FXML private TableColumn<Leave, String> autreColumn;
    @FXML private TableColumn<Leave, String> justificationColumn;
    @FXML private TableColumn<Leave, String> statusColumn;
    @FXML private TableColumn<Leave, LocalDate> startDateColumn;
    @FXML private TableColumn<Leave, LocalDate> endDateColumn;

    private ObservableList<Leave> leaveRequests = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("typeConge"));
        autreColumn.setCellValueFactory(new PropertyValueFactory<>("autre"));
        justificationColumn.setCellValueFactory(new PropertyValueFactory<>("justification"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        startDateColumn.setCellValueFactory(new PropertyValueFactory<>("dateDebut"));
        endDateColumn.setCellValueFactory(new PropertyValueFactory<>("dateFin"));

        loadLeaveRequests();
    }

    private void loadLeaveRequests() {
        String query = "SELECT id,  type_congé, autre, justification, status, date_debut, date_fin FROM demande_conge";
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

            leaveTable.setItems(leaveRequests);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
