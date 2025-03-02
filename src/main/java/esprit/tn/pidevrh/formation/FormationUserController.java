package esprit.tn.pidevrh.formation;

import esprit.tn.pidevrh.connection.DatabaseConnection;
import esprit.tn.pidevrh.login.User;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FormationUserController {

    @FXML
    private VBox formationUserContainer;

    @FXML
    public void initialize() {
        loadFormationsWithUsers();
    }

    private void loadFormationsWithUsers() {
        formationUserContainer.getChildren().clear();

        // Add header
        HBox header = createHeader();
        formationUserContainer.getChildren().add(header);

        // Updated query to fetch formations and all users enrolled in each formation
        String query = "SELECT f.id AS formation_id, f.title AS formation_name, u.id AS user_id, u.first_name, u.last_name, u.email\n" +
                "FROM formation f\n" +
                "LEFT JOIN formation_user fu ON f.id = fu.formation_id\n" +
                "LEFT JOIN user u ON fu.user_id = u.id\n" +
                "ORDER BY f.id;";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            // Process the result set and group users by formation
            long currentFormationId = -1;
            String currentFormationName = null;
            List<User> users = new ArrayList<>();

            while (rs.next()) {
                long formationId = rs.getLong("formation_id");
                String formationName = rs.getString("formation_name");
                long userId = rs.getLong("user_id");
                String firstName = rs.getString("first_name");
                String lastName = rs.getString("last_name");
                String email = rs.getString("email");

                // If we encounter a new formation, display the previous formation and reset the list
                if (formationId != currentFormationId) {
                    if (currentFormationName != null) {
                        // Create a formation card with its users
                        HBox formationCard = createFormationCard(currentFormationId, currentFormationName, users); // Pass formationId
                        formationUserContainer.getChildren().add(formationCard);
                    }

                    // Reset for the new formation
                    currentFormationId = formationId;
                    currentFormationName = formationName;
                    users = new ArrayList<>();
                }

                // Add the user to the list of users for the current formation
                if (userId != 0) {
                    users.add(new User(userId, firstName, lastName, email, "", User.Role.USER));
                }
            }

            // After the loop, add the last formation to the container
            if (currentFormationName != null) {
                HBox formationCard = createFormationCard(currentFormationId, currentFormationName, users); // Pass formationId
                formationUserContainer.getChildren().add(formationCard);
            }

        } catch (SQLException e) {
            showAlert("Erreur", "Impossible de charger les formations et leurs utilisateurs: " + e.getMessage());
        }
    }


    private HBox createHeader() {
        HBox header = new HBox();
        header.getStyleClass().add("user-formation-header");
        header.setAlignment(Pos.CENTER_LEFT);
        header.setSpacing(50);

        Label formationHeader = createHeaderLabel("Formation", 250);
        Label userHeader = createHeaderLabel("Utilisateurs", 500);

        header.getChildren().addAll(formationHeader, userHeader);
        return header;
    }

    private Label createHeaderLabel(String text, double width) {
        Label label = new Label(text);
        label.getStyleClass().add("column-header");
        label.setPrefWidth(width);
        label.setPadding(new Insets(0, 10, 0, 10));
        return label;
    }

    private HBox createFormationCard(long formationId, String formationName, List<User> users) {
        HBox card = new HBox();
        card.getStyleClass().add("formation-user-card");
        card.setSpacing(20);
        card.setAlignment(Pos.CENTER_LEFT);

        Label formationLabel = new Label(formationName);
        formationLabel.getStyleClass().add("formation-info");
        formationLabel.setPrefWidth(250);

        // Create a VBox to hold the user list for the formation
        VBox usersContainer = new VBox();
        usersContainer.setSpacing(5);

        // Add users and a Ban button for each user in the formation
        for (User user : users) {
            HBox userBox = new HBox(10);
            userBox.setAlignment(Pos.CENTER_LEFT);

            Label userLabel = new Label(user.getFirstName() + " " + user.getLastName());
            userLabel.getStyleClass().add("user-info");

            // Create the Ban button for each user
            Button banButton = new Button("Retirer");
            banButton.getStyleClass().add("action-button");
            banButton.setOnAction(e -> handleBanUserFromFormation(user, formationId, formationName));

            userBox.getChildren().addAll(userLabel, banButton);
            usersContainer.getChildren().add(userBox);
        }

        // If no users exist, display a default message
        if (users.isEmpty()) {
            Label noUsersLabel = new Label("Aucun utilisateur");
            usersContainer.getChildren().add(noUsersLabel);
        }

        card.getChildren().addAll(formationLabel, usersContainer);
        return card;
    }

    private void handleBanUserFromFormation(User user, long formationId, String formationName) {
        // Show a confirmation dialog to confirm banning
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("retirerutilisateur");
        alert.setHeaderText("Êtes-vous sûr de vouloir bannir cet utilisateur de la formation ?");
        alert.setContentText("User: " + user.getFirstName() + " " + user.getLastName() + " - Formation: " + formationName);

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                banUserFromFormation(user, formationId);  // Pass the formationId here
            }
        });
    }

    private void banUserFromFormation(User user, long formationId) {
        String query = "DELETE FROM formation_user WHERE user_id = ? AND formation_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setLong(1, user.getId());
            stmt.setLong(2, formationId);  // Use formationId to uniquely identify the formation
            int affectedRows = stmt.executeUpdate();

            if (affectedRows > 0) {
                showAlert("Success", "L'utilisateur a été banni de la formation..");
                loadFormationsWithUsers(); // Reload the formation list to reflect the change
            } else {
                showAlert("Error", "Impossible de bannir l'utilisateur de la formation.");
            }
        } catch (SQLException e) {
            showAlert("Error", "An error occurred while banning the user: " + e.getMessage());
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
