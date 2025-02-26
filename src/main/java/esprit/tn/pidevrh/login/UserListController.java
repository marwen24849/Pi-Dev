package esprit.tn.pidevrh.login;

import esprit.tn.pidevrh.connection.DatabaseConnection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

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

    @FXML
    private VBox userContainer;

    private void loadUsers() {
        userContainer.getChildren().clear();

        // Add header
        HBox header = createHeader();
        userContainer.getChildren().add(header);

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
                HBox userCard = createUserCard(user);
                userContainer.getChildren().add(userCard);
            }

        } catch (SQLException e) {
            showAlert("Erreur", "Impossible de charger les utilisateurs: " + e.getMessage());
        }
    }
    private HBox createHeader() {
        HBox header = new HBox();
        header.getStyleClass().add("user-header");
        header.setAlignment(Pos.CENTER_LEFT);
        header.setSpacing(50);


        Label nameHeader = createHeaderLabel("Nom Complet", 250);
        Label emailHeader = createHeaderLabel("Email", 300);


        header.getChildren().addAll(nameHeader, emailHeader);
        return header;
    }

    private Label createHeaderLabel(String text, double width) {
        Label label = new Label(text);
        label.getStyleClass().add("column-header");
        label.setPrefWidth(width);
        label.setPadding(new Insets(0, 10, 0, 10));
        return label;
    }

    private HBox createUserCard(User user) {
        HBox card = new HBox();
        card.getStyleClass().add("user-card");
        card.setSpacing(20);
        card.setAlignment(Pos.CENTER_LEFT);


        Label nameLabel = new Label(user.getFirstName() + " " + user.getLastName());
        nameLabel.getStyleClass().add("user-info");
        nameLabel.setPrefWidth(200);


        Label emailLabel = new Label(user.getEmail());
        emailLabel.getStyleClass().add("user-info");
        emailLabel.setPrefWidth(250);


        ComboBox<User.Role> roleCombo = new ComboBox<>();
        roleCombo.getStyleClass().add("role-combo");
        roleCombo.getItems().addAll(User.Role.values());
        roleCombo.setValue(user.getRole());
        roleCombo.setPrefWidth(150);
        roleCombo.setOnAction(e -> handleRoleChange(user, roleCombo.getValue()));


        HBox actions = new HBox(10);
        Button deleteButton = new Button("Supprimer");
        deleteButton.getStyleClass().addAll("action-button", "delete-button");
        deleteButton.setOnAction(e -> handleDelete(user));

        actions.getChildren().addAll(deleteButton);
        actions.setPrefWidth(100);

        card.getChildren().addAll(nameLabel, emailLabel, roleCombo, actions);
        return card;
    }

    private void handleRoleChange(User user, User.Role newRole) {

        String query = "UPDATE user SET role = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, newRole.toString());
            stmt.setLong(2, user.getId());
            stmt.executeUpdate();
            user.setRole(newRole);
        } catch (SQLException e) {
            showAlert("Erreur", "Échec de la mise à jour du rôle: " + e.getMessage());
        }
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
