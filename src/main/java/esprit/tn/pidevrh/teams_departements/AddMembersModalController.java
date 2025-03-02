package esprit.tn.pidevrh.teams_departements;

import esprit.tn.pidevrh.connection.DatabaseConnection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.stage.Stage;

import java.sql.*;
import java.util.regex.Pattern;

public class AddMembersModalController {

    @FXML
    private ListView<User> userListView;

    private ObservableList<User> users = FXCollections.observableArrayList();
    private Team selectedTeam; // The team to which users will be added
    private Stage stage;

    public void setSelectedTeam(Team team) {
        this.selectedTeam = team;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    @FXML
    public void initialize() {
        fetchUnassignedUsers();
        userListView.setItems(users);
        userListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE); // Enable multiple selection
    }

    private void fetchUnassignedUsers() {
        try (Connection connection = DatabaseConnection.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT id, first_name, last_name, email FROM user WHERE id_equipe IS NULL")) {

            while (resultSet.next()) {
                users.add(new User(
                        resultSet.getInt("id"),
                        resultSet.getString("first_name"),
                        resultSet.getString("last_name"),
                        resultSet.getString("email")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
    private void handleAddSelected() {
        ObservableList<User> selectedUsers = userListView.getSelectionModel().getSelectedItems();

        if (selectedUsers.isEmpty()) {
            showAlert("No Selection", "Please select at least one user to add to the team.");
            return;
        }

        int maxMembers = getMaxMembersForTeam(selectedTeam);
        int currentTeamSize = getCurrentTeamSize(selectedTeam);
        int remainingSlots = maxMembers - currentTeamSize;

        if (selectedUsers.size() > remainingSlots) {
            showAlert("Limit Reached", "You can only add " + remainingSlots + " more member(s) to this team.");
            return;
        }

        boolean success = assignUsersToTeam(selectedUsers, selectedTeam);
        if (success) {
            showSuccessAlert("Users Added", "Successfully added " + selectedUsers.size() + " user(s) to " + selectedTeam.getName());
            stage.close(); // Close modal only after confirmation
        } else {
            showAlert("Database Error", "Failed to assign users to the team.");
        }
    }

    private int getCurrentTeamSize(Team team) {
        String query = "SELECT COUNT(*) AS count FROM user WHERE id_equipe = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setInt(1, team.getId());
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt("count");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0; // Default if something goes wrong
    }

    private int getMaxMembersForTeam(Team team) {
        String query = "SELECT members FROM equipe WHERE id = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setInt(1, team.getId());
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt("members"); // Use 'members' instead of 'max_members'
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 100; // Default if something goes wrong
    }


    private void showSuccessAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
    private void handleCancel() {
        stage.close(); // Close the modal without doing anything
    }

    private boolean assignUsersToTeam(ObservableList<User> users, Team team) {
        String query = "UPDATE user SET id_equipe = ? WHERE id = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            for (User user : users) {
                preparedStatement.setInt(1, team.getId());
                preparedStatement.setInt(2, user.getId());
                preparedStatement.addBatch(); // Batch processing
            }

            preparedStatement.executeBatch();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean isValidTeamName(String name) {
        return name != null && !name.trim().isEmpty() && Pattern.matches("^[a-zA-Z0-9 ]+$", name);
    }


    public static class User {
        private final int id;
        private final String firstName;
        private final String lastName;
        private final String email;
        private final String name;

        public User(int id, String firstName, String lastName, String email) {
            this.id = id;
            this.firstName = firstName;
            this.lastName = lastName;
            this.email = email;
            this.name = firstName + " " + lastName;
        }

        public int getId() { return id; }
        public String getFirstName() { return firstName; }
        public String getLastName() { return lastName; }
        public String getEmail() { return email; }
        public String getName() { return name; }

        @Override
        public String toString() {
            return id + "  |  " + name + "  |  " + email;
        }
    }
}
