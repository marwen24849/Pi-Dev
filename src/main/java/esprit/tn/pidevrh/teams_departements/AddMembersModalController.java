package esprit.tn.pidevrh.teams_departements;

import esprit.tn.pidevrh.connection.DatabaseConnection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

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
        if (!selectedUsers.isEmpty()) {
            for (User selectedUser : selectedUsers) {
                assignUserToTeam(selectedUser, selectedTeam);
            }
            stage.close(); // Close the modal after assignment
        } else {
            showAlert("No Selection", "Please select at least one user to add to the team.");
        }
    }

    @FXML
    private void handleCancel() {
        stage.close(); // Close the modal without doing anything
    }

    private void assignUserToTeam(User user, Team team) {
        try (Connection connection = DatabaseConnection.getConnection();
             Statement statement = connection.createStatement()) {

            String query = String.format("UPDATE user SET id_equipe = %d WHERE id = %d", team.getId(), user.getId());
            statement.executeUpdate(query);
            System.out.println("User " + user.getName() + " assigned to team " + team.getName());
        } catch (SQLException e) {
            e.printStackTrace();
        }
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