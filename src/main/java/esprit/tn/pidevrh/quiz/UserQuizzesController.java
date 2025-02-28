package esprit.tn.pidevrh.quiz;


import esprit.tn.pidevrh.connection.DatabaseConnection;
import esprit.tn.pidevrh.login.User;
import esprit.tn.pidevrh.response.ResponseController;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserQuizzesController {

    @FXML
    private ListView<Quiz> quizzesListView;

    @FXML
    private Button passQuizButton;

    private User loggedInUser;
    private Stage stage;

    public void initialize() {
        loadUserQuizzes();
    }

    public void setUser(User user) {
        this.loggedInUser = user;
        loadUserQuizzes();
    }

    private void loadUserQuizzes() {
        ObservableList<Quiz> quizzes = FXCollections.observableArrayList();
        String query = """
        SELECT q.id, q.title, q.category, q.difficultylevel, q.quizTime, q.minimum_success_percentage
        FROM quiz q
        JOIN user_quiz uq ON q.id = uq.quiz_id
        LEFT JOIN response r ON q.id = r.quiz_id AND r.user_id = uq.user_id
        WHERE uq.user_id = ? AND r.id IS NULL
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setLong(1, 1); // Remplace par loggedInUser.getId() si disponible
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Quiz quiz = new Quiz();
                quiz.setId(rs.getLong("id"));
                quiz.setTitle(rs.getString("title"));
                quiz.setCategory(rs.getString("category"));
                quiz.setDifficultylevel(rs.getString("difficultylevel"));
                quiz.setTime(rs.getInt("quizTime"));
                quiz.setMinimumSuccessPercentage(rs.getDouble("minimum_success_percentage"));
                quizzes.add(quiz);
            }
        } catch (SQLException e) {
            showAlert("Erreur", "Impossible de charger les quiz assignés.");
        }

        quizzesListView.setItems(quizzes);
        quizzesListView.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Quiz quiz, boolean empty) {
                super.updateItem(quiz, empty);
                if (empty || quiz == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    VBox container = new VBox();
                    container.setSpacing(5);

                    Label titleLabel = new Label("📌 " + quiz.getTitle());
                    titleLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #2c3e50;");

                    Label categoryLabel = new Label("📂 Catégorie : " + quiz.getCategory());
                    categoryLabel.setStyle("-fx-text-fill: #2980b9;");

                    Label difficultyLabel = new Label("🔥 Difficulté : " + quiz.getDifficultylevel());
                    difficultyLabel.setStyle("-fx-text-fill: " + getDifficultyColor(quiz.getDifficultylevel()) + ";");

                    Label timeLabel = new Label("⏳ Temps : " + quiz.getTime() + " min");
                    timeLabel.setStyle("-fx-text-fill: #e67e22;");

                    Label successLabel = new Label("✅ Succès min : " + quiz.getMinimumSuccessPercentage() + "%");
                    successLabel.setStyle("-fx-text-fill: #27ae60;");

                    container.getChildren().addAll(titleLabel, categoryLabel, difficultyLabel, timeLabel, successLabel);
                    setGraphic(container);
                }
            }
        });

        // Désactiver le bouton si aucun quiz n'est sélectionné
        passQuizButton.setDisable(true);
        quizzesListView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            passQuizButton.setDisable(newSelection == null);
        });
    }

    /**
     * Retourne une couleur en fonction du niveau de difficulté
     */
    private String getDifficultyColor(String difficulty) {
        return switch (difficulty.toLowerCase()) {
            case "facile" -> "#2ecc71"; // Vert
            case "moyen" -> "#f1c40f"; // Jaune
            case "difficile" -> "#e74c3c"; // Rouge
            default -> "#7f8c8d"; // Gris
        };
    }


    @FXML
    private void handlePassQuiz() {
        Quiz selectedQuiz = quizzesListView.getSelectionModel().getSelectedItem();
        if (selectedQuiz != null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/Fxml/Response/quiz.fxml"));
                Parent root = loader.load();
                ResponseController controller = loader.getController();
                controller.initializeQuiz(selectedQuiz.getId());
                Stage stage = new Stage();
                stage.initModality(Modality.APPLICATION_MODAL);
                stage.setTitle("Passer Quiz : "+ selectedQuiz.getTitle());
                stage.setScene(new Scene(root));
                controller.setStage(stage);
                stage.showAndWait();
                initialize();
            } catch (IOException e) {
                e.printStackTrace();
            }

            showAlert("Quiz", "Vous allez passer le quiz : " + selectedQuiz.getTitle());

        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }
}
