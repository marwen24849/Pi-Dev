package esprit.tn.pidevrh.login;

import esprit.tn.pidevrh.connection.DatabaseConnection;
import javafx.beans.property.ReadOnlyObjectWrapper;
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
    private TableView<User> userTableView;

    @FXML
    private TableColumn<User, Long> idColumn;

    @FXML
    private TableColumn<User, String> firstNameColumn;

    @FXML
    private TableColumn<User, String> lastNameColumn;

    @FXML
    private TableColumn<User, String> emailColumn;

    @FXML
    private TableColumn<User, String> roleColumn;

    @FXML
    private TableColumn<User, Void> actionColumn;

    @FXML
    private Pagination pagination;

    @FXML
    public void initialize() {
        loadUsers();
    }

    private void loadUsers() {
        ObservableList<User> userList = FXCollections.observableArrayList();

        String query = "SELECT * FROM user";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                long id = rs.getLong("id");
                String firstName = rs.getString("first_name");
                String lastName = rs.getString("last_name");
                String email = rs.getString("email");
                String password = rs.getString("password");  // Retrieve the password
                String roleString = rs.getString("role");

                User.Role role = User.Role.valueOf(roleString);

                User user = new User(id, firstName, lastName, email, password, role);

                userList.add(user);
            }

        } catch (SQLException e) {
            showAlert("Erreur", "Impossible de charger les utilisateurs: " + e.getMessage());
        }

        userTableView.setItems(userList);

        idColumn.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue().getId()));
        firstNameColumn.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue().getFirstName()));
        lastNameColumn.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue().getLastName()));
        emailColumn.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue().getEmail()));
        roleColumn.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue().getRole()).asString());

        actionColumn.setCellFactory(param -> new TableCell<User, Void>() {
            private final Button deleteButton = new Button("🗑️");

            {
                deleteButton.setOnAction(event -> {
                    User user = getTableView().getItems().get(getIndex());
                    handleDelete(user);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(new HBox(10, deleteButton));
                }
            }
        });
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
