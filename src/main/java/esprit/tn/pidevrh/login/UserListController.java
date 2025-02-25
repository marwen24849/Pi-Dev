package esprit.tn.pidevrh.login;

import esprit.tn.pidevrh.connection.DatabaseConnection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserListController {

    @FXML
    private ListView<HBox> userListView;

    @FXML
    public void initialize() {
        loadUsers();
    }

    private void loadUsers() {
        ObservableList<HBox> userList = FXCollections.observableArrayList();

        String query = "SELECT * FROM user";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                long id = rs.getLong("id");
                String firstName = rs.getString("first_name");
                String lastName = rs.getString("last_name");
                String email = rs.getString("email");
                String password = rs.getString("password");
                String roleString = rs.getString("role");

                User.Role role = User.Role.valueOf(roleString);
                User user = new User(id, firstName, lastName, email, password, role);

                HBox userItem = createUserItem(user);
                userList.add(userItem);
            }

        } catch (SQLException e) {
            showAlert("Erreur", "Impossible de charger les utilisateurs: " + e.getMessage());
        }

        userListView.setItems(userList);
    }

    private HBox createUserItem(User user) {
        Label userInfo = new Label(user.getId()+" "  + user.getFirstName() + " " + user.getLastName() + " " + user.getEmail() + " "+ user.getRole());
        Button deleteButton = new Button("🗑️");

        deleteButton.setOnAction(event -> handleDelete(user));
        HBox userItem = new HBox(10, userInfo, deleteButton);
        return userItem;
    }

    private void handleDelete(User user) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Suppression");
        alert.setHeaderText("Voulez-vous supprimer cet utilisateur ?");
        alert.setContentText("Utilisateur: " + user.getFirstName());

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                deleteUser(user.getId());
            }
        });
    }

    private void deleteUser(long id) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "DELETE FROM user WHERE id = ?";
            PreparedStatement statement = conn.prepareStatement(query);
            statement.setLong(1, id);
            statement.executeUpdate();
            loadUsers();
        } catch (SQLException e) {
            showAlert("Erreur", "Impossible de supprimer l'utilisateur: " + e.getMessage());
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
