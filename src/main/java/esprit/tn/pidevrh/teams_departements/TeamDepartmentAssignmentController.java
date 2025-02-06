package esprit.tn.pidevrh.teams_departements;

import esprit.tn.pidevrh.connection.DatabaseConnection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class TeamDepartmentAssignmentController {

    @FXML
    private TableView<Team> table;
    @FXML
    private TableColumn<Team, String> nameCol;
    @FXML
    private TableColumn<Team, Number> membersCol;
    @FXML
    private TableColumn<Team, String> departmentCol;
    @FXML
    private TableColumn<Team, Void> teamMembersCol;
    @FXML
    private TextField nameInput;
    @FXML
    private TextField membersInput;
    @FXML
    private TextField filterInput;

    private ObservableList<Team> teams = FXCollections.observableArrayList();
    private ObservableList<String> departments = FXCollections.observableArrayList();
    private int nextTeamId = 1; // To generate sequential IDs for new teams

    @FXML
    public void initialize() {
        // Initialize the columns
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        membersCol.setCellValueFactory(new PropertyValueFactory<>("members"));
        departmentCol.setCellValueFactory(new PropertyValueFactory<>("department"));
        departmentCol.setCellFactory(ComboBoxTableCell.forTableColumn(departments));

        // Set up the "Team Members" column with a button
        teamMembersCol.setCellFactory(new Callback<TableColumn<Team, Void>, TableCell<Team, Void>>() {
            @Override
            public TableCell<Team, Void> call(final TableColumn<Team, Void> param) {
                return new TableCell<Team, Void>() {
                    private final Button btn = new Button("Add Members");

                    {
                        btn.setOnAction(event -> {
                            Team team = getTableView().getItems().get(getIndex());

                            handleAddMembers(team);
                        });
                    }

                    @Override
                    protected void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            setGraphic(btn);
                        }
                    }
                };
            }
        });

        table.setItems(teams);
        table.setEditable(true);

        fetchDepartmentsFromDatabase();
        initializeSampleData();
    }

    private void fetchDepartmentsFromDatabase() {
        try (Connection connection = DatabaseConnection.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT name FROM department")) {

            while (resultSet.next()) {
                departments.add(resultSet.getString("name"));
            }
        } catch (SQLException e) {
            showAlert("Database Error", "Error fetching departments: " + e.getMessage());
        }
    }

    @FXML
    private void handleAddTeam() {
        try {
            teams.add(new Team(
                    nextTeamId++,  // Use and increment the ID counter
                    nameInput.getText(),
                    Integer.parseInt(membersInput.getText()),
                    "Unassigned"
            ));
            nameInput.clear();
            membersInput.clear();
        } catch (NumberFormatException ex) {
            showAlert("Invalid Input", "Please enter valid numbers for members");
        }
    }

    @FXML
    private void handleFilter() {
        try {
            int filterValue = Integer.parseInt(filterInput.getText());
            FilteredList<Team> filteredTeams = new FilteredList<>(teams, team -> team.getMembers() == filterValue);
            table.setItems(filteredTeams);
        } catch (NumberFormatException ex) {
            showAlert("Invalid Input", "Please enter a valid number for filtering");
        }
    }

    @FXML
    private void handleAddMembers(Team team) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Fxml/TeamDepartement/addMembersModal.fxml"));
            Stage modalStage = new Stage();
            modalStage.setScene(new Scene(loader.load()));
            modalStage.initModality(Modality.APPLICATION_MODAL);
            modalStage.setTitle("Add Members to " + team.getName());

            AddMembersModalController controller = loader.getController();
            controller.setSelectedTeam(team);
            controller.setStage(modalStage);

            modalStage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load the modal window: " + e.getMessage());
        }
    }

    private void initializeSampleData() {
        teams.add(new Team(nextTeamId++, "Alpha Team", 5, "IT"));
        teams.add(new Team(nextTeamId++, "Beta Team", 8, "HR"));
        teams.add(new Team(nextTeamId++, "Gamma Team", 6, "Sales"));
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}