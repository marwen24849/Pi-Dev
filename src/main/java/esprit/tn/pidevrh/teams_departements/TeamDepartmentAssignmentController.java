package esprit.tn.pidevrh.teams_departements;

import esprit.tn.pidevrh.connection.DatabaseConnection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.*;

public class TeamDepartmentAssignmentController {

    @FXML
    private ListView<HBox> teamListView;
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
        fetchDepartmentsFromDatabase();
        //initializeSampleData();
        fetchTeamsFromDatabase();
        updateListView();
    }

    private void updateListView() {
        teamListView.getItems().clear();
        for (Team team : teams) {
            HBox hBox = createTeamHBox(team);
            teamListView.getItems().add(hBox);
        }
    }

    private HBox createTeamHBox(Team team) {
        // Create a VBox to hold the team details (card layout)
        VBox cardLayout = new VBox();
        cardLayout.setSpacing(10);
        cardLayout.setPadding(new Insets(15));
        cardLayout.getStyleClass().add("team-card");

        // Team name label
        Label nameLabel = new Label("Team: " + team.getName());
        nameLabel.setStyle("-fx-font-size: 16; -fx-font-weight: bold;");

        // Team members label
        Label membersLabel = new Label("Members: " + team.getMembers());
        membersLabel.setStyle("-fx-font-size: 14; -fx-text-fill: #7f8c8d;");

        // Department combo box
        ComboBox<String> departmentComboBox = new ComboBox<>(departments);
        departmentComboBox.setValue(team.getDepartment());
        departmentComboBox.setOnAction(event -> {
            String selectedDepartment = departmentComboBox.getValue();
            team.setDepartment(selectedDepartment);
            updateTeamDepartmentInDatabase(team.getId(), selectedDepartment);
        });

        // Add Members button
        Button addMembersButton = new Button("Add Members");
        addMembersButton.getStyleClass().add("modern-button");
        addMembersButton.setOnAction(event -> handleAddMembers(team));

        // Delete Team button
        Button deleteButton = new Button("Delete Team");
        deleteButton.getStyleClass().add("modern-button");
        deleteButton.setStyle("-fx-background-color: #e74c3c;"); // Red color for delete button
        deleteButton.setOnAction(event -> handleDeleteTeam(team));

        // Add all elements to the card layout
        cardLayout.getChildren().addAll(nameLabel, membersLabel, departmentComboBox, addMembersButton, deleteButton);

        // Wrap the card layout in an HBox (optional, for additional styling)
        HBox hBox = new HBox(cardLayout);
        hBox.setSpacing(10);
        return hBox;
    }

    private void handleDeleteTeam(Team team) {
        // Show confirmation dialog
        Alert confirmationDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmationDialog.setTitle("Delete Team");
        confirmationDialog.setHeaderText("Are you sure you want to delete this team?");
        confirmationDialog.setContentText("This action cannot be undone.");

        // Wait for user response
        confirmationDialog.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                // Delete the team from the database
                if (deleteTeamFromDatabase(team.getId())) {
                    // Remove the team from the observable list and update the ListView
                    teams.remove(team);
                    updateListView();
                } else {
                    showAlert("Database Error", "Failed to delete the team.");
                }
            }
        });
    }

    private boolean deleteTeamFromDatabase(int teamId) {
        // Update users with id_equipe = teamId to set id_equipe = NULL
        String updateUsersQuery = "UPDATE user SET id_equipe = NULL WHERE id_equipe = ?";
        // Delete the team from the equipe table
        String deleteTeamQuery = "DELETE FROM equipe WHERE id = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement updateUsersStatement = connection.prepareStatement(updateUsersQuery);
             PreparedStatement deleteTeamStatement = connection.prepareStatement(deleteTeamQuery)) {

            // Update users
            updateUsersStatement.setInt(1, teamId);
            updateUsersStatement.executeUpdate();

            // Delete team
            deleteTeamStatement.setInt(1, teamId);
            deleteTeamStatement.executeUpdate();

            return true; // Success
        } catch (SQLException e) {
            e.printStackTrace();
            return false; // Failure
        }
    }

    private void updateTeamDepartmentInDatabase(int teamId, String departmentName) {
        String query = "UPDATE equipe SET department_id = (SELECT id FROM department WHERE name = ?) WHERE id = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setString(1, departmentName);
            preparedStatement.setInt(2, teamId);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            showAlert("Database Error", "Failed to update team department: " + e.getMessage());
        }
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
            String name = nameInput.getText().trim();
            String membersText = membersInput.getText().trim();

            // Validate name: Cannot be empty, only allows letters, numbers, and underscores (_)
            if (name.isEmpty() || !name.matches("^[A-Za-z0-9_]+$") || name.matches("^[0-9_]+$")) {
                showAlert("Invalid Team Name", "Team name must contain letters and can include numbers and underscores (_), but cannot be only numbers or Null.");
                return;
            }

            // Validate members: Must be an integer between 1 and 100
            int members = Integer.parseInt(membersText);
            if (members < 1 || members > 100) {
                showAlert("Invalid Members", "Number of members must be between 1 and 100.");
                return;
            }

            // Insert into database
            int generatedId = insertTeamIntoDatabase(name, members);
            if (generatedId != -1) {
                Team newTeam = new Team(generatedId, name, members, "Unassigned");
                teams.add(newTeam);
                teamListView.getItems().add(createTeamHBox(newTeam));
            }

            // Clear input fields
            nameInput.clear();
            membersInput.clear();
        } catch (NumberFormatException ex) {
            showAlert("Invalid Input", "Please enter a valid number for members.");
        }
    }

    private int insertTeamIntoDatabase(String name, int members) {
        String query = "INSERT INTO equipe (name, members, department_id) VALUES (?, ?, NULL)";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            preparedStatement.setString(1, name);
            preparedStatement.setInt(2, members);
            preparedStatement.executeUpdate();

            ResultSet rs = preparedStatement.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1); // Return generated team ID
            }
        } catch (SQLException e) {
            showAlert("Database Error", "Failed to add team: " + e.getMessage());
        }
        return -1; // Return -1 in case of failure
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

    private void fetchTeamsFromDatabase() {
        String query = "SELECT equipe.id, equipe.name, equipe.members, department.name AS department_name " +
                "FROM equipe LEFT JOIN department ON equipe.department_id = department.id";

        try (Connection connection = DatabaseConnection.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {

            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String name = resultSet.getString("name");
                int members = resultSet.getInt("members");
                String department = resultSet.getString("department_name");

                // Default to "Unassigned" if no department is linked
                if (department == null) {
                    department = "Unassigned";
                }

                teams.add(new Team(id, name, members, department));
            }
        } catch (SQLException e) {
            showAlert("Database Error", "Error fetching teams: " + e.getMessage());
        }
    }
}