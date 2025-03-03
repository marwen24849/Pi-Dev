package esprit.tn.pidevrh.quiz;

import esprit.tn.pidevrh.connection.DatabaseConnection;
import esprit.tn.pidevrh.login.User;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AssignUserModalController {

    @FXML
    private ListView<User> usersListView;

    @FXML
    private TextField searchField;

    private Stage stage;
    private Long quizId;
    private boolean confirmed = false;
    private ObservableList<User> users = FXCollections.observableArrayList();
    private FilteredList<User> filteredUsers;

    public void initialize() {
        loadUsers();
        setupSearchFilter();
    }

    private void loadUsers() {
        String query = "SELECT id, first_name, last_name FROM user";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                User user = new User();
                user.setId(rs.getLong("id"));
                user.setFirstName(rs.getString("first_name"));
                user.setLastName(rs.getString("last_name"));
                users.add(user);
            }
        } catch (SQLException e) {
            showAlert("Erreur", "Impossible de charger les utilisateurs");
        }

        filteredUsers = new FilteredList<>(users, p -> true);
        usersListView.setItems(filteredUsers);

        usersListView.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(User user, boolean empty) {
                super.updateItem(user, empty);
                if (empty || user == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    // Création des labels pour prénom et nom
                    Label nameLabel = new Label(user.getFirstName() + " " + user.getLastName());
                    nameLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

                    // Icône utilisateur
                    Label icon = new Label("\uD83D\uDC64"); // Emoji utilisateur 👤
                    icon.setStyle("-fx-font-size: 16px; -fx-padding: 5px;");

                    // Conteneur HBox pour aligner l'icône et le texte
                    HBox hBox = new HBox(10, icon, nameLabel);
                    hBox.setStyle("-fx-padding: 5px; -fx-alignment: CENTER_LEFT;");

                    // Appliquer le HBox comme contenu de la cellule
                    setGraphic(hBox);
                }
            }
        });

    }

    private void setupSearchFilter() {
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredUsers.setPredicate(user -> {
                if (newValue == null || newValue.trim().isEmpty()) {
                    return true;
                }
                String lowerCaseFilter = newValue.toLowerCase();
                return user.getFirstName().toLowerCase().contains(lowerCaseFilter) ||
                        user.getLastName().toLowerCase().contains(lowerCaseFilter);
            });
        });
    }

    @FXML
    private void handleConfirm() {
        User selected = usersListView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            confirmed = true;
            assignQuizToUser(selected.getId());
            stage.close();
        } else {
            showAlert("Avertissement", "Veuillez sélectionner un utilisateur.");
        }
    }

    @FXML
    private void handleCancel() {
        stage.close();
    }

    private void assignQuizToUser(Long userId) {
        String checkSql = "SELECT COUNT(*) FROM user_quiz WHERE user_id = ? AND quiz_id = ?";
        String insertSql = "INSERT INTO user_quiz (user_id, quiz_id) VALUES (?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement checkStmt = conn.prepareStatement(checkSql);
             PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {

            checkStmt.setLong(1, userId);
            checkStmt.setLong(2, quizId);
            ResultSet rs = checkStmt.executeQuery();

            if (rs.next() && rs.getInt(1) > 0) {
                showAlert("Information", "Cet utilisateur a déjà ce quiz");
                return;
            }

            insertStmt.setLong(1, userId);
            insertStmt.setLong(2, quizId);
            int affectedRows = insertStmt.executeUpdate();

            if (affectedRows > 0) {
                showSuccessAlert("Succès", "Quiz affecté avec succès !");
            }

        } catch (SQLException e) {
            showAlert("Erreur", "Échec de l'affectation : " + e.getMessage());
        }
    }

    private void showSuccessAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public void setQuizId(Long quizId) {
        this.quizId = quizId;
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
