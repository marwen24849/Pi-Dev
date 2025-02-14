package esprit.tn.pidevrh.quiz;

import esprit.tn.pidevrh.connection.DatabaseConnection;
import esprit.tn.pidevrh.question.Question;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ListQuestionQuiz {

    private Long idQuiz;
    private static final String QUIZ_TITLE_STATIC = "Nom du Quiz"; // Valeur statique

    @FXML
    private Label quizTitleLabel; // Titre du quiz

    @FXML
    private ListView<Question> questionListView;

    public void setQuiz(Long idQuiz) {
        this.idQuiz = idQuiz;
        quizTitleLabel.setText("Quiz : " + QUIZ_TITLE_STATIC); // Mettre à jour le titre
        questionListView.setItems(loadQuestions(idQuiz));
        questionListView.setCellFactory(param -> new QuestionCell());
    }

    private ObservableList<Question> loadQuestions(Long idQuiz) {
        ObservableList<Question> questions = FXCollections.observableArrayList();
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT * FROM question WHERE id IN (SELECT questions_id FROM quiz_questions WHERE quiz_id = ?)";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setLong(1, idQuiz);
            ResultSet resultSet = ps.executeQuery();
            while (resultSet.next()) {
                Question question = new Question();
                question.setId(resultSet.getLong("id"));
                question.setTitle(resultSet.getString("question_title"));
                question.setOption1(resultSet.getString("option1"));
                question.setOption2(resultSet.getString("option2"));
                question.setOption3(resultSet.getString("option3"));
                question.setOption4(resultSet.getString("option4"));
                question.setRightAnswer(resultSet.getString("right_answer"));
                question.setScore(resultSet.getInt("score"));
                question.setCategory(resultSet.getString("category"));
                question.setDifficulty(resultSet.getString("difficultylevel"));
                questions.add(question);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return questions;
    }

    private void removeQuestionFromQuiz(Question question) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "DELETE FROM quiz_questions WHERE quiz_id = ? AND questions_id = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setLong(1, idQuiz);
            ps.setLong(2, question.getId());

            int affectedRows = ps.executeUpdate();
            if (affectedRows > 0) {
                questionListView.getItems().remove(question);
                showAlert("Succès", "La question a été supprimée du quiz.", Alert.AlertType.INFORMATION);
            } else {
                showAlert("Erreur", "Impossible de supprimer la question.", Alert.AlertType.ERROR);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Erreur", "Une erreur est survenue.", Alert.AlertType.ERROR);
        }
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    class QuestionCell extends ListCell<Question> {
        @Override
        protected void updateItem(Question question, boolean empty) {
            super.updateItem(question, empty);
            if (empty || question == null) {
                setGraphic(null);
            } else {
                VBox container = new VBox(10);
                container.setStyle("-fx-padding: 10px; -fx-background-color: #ecf0f1; -fx-border-color: #bdc3c7; -fx-border-radius: 5px;");

                Label titleLabel = new Label(question.getTitle());
                titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

                HBox optionsBox = new HBox(10);
                VBox leftOptions = new VBox(5);
                VBox rightOptions = new VBox(5);

                Label option1 = new Label("A) " + question.getOption1());
                Label option2 = new Label("B) " + question.getOption2());
                Label option3 = new Label("C) " + question.getOption3());
                Label option4 = new Label("D) " + question.getOption4());

                leftOptions.getChildren().addAll(option1, option2);
                rightOptions.getChildren().addAll(option3, option4);
                optionsBox.getChildren().addAll(leftOptions, rightOptions);

                Label answerLabel = new Label("✅ Réponse correcte : " + question.getRightAnswer());
                answerLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #27ae60;");

                Label scoreLabel = new Label("Score : " + question.getScore());
                Label categoryLabel = new Label("📂 Catégorie : " + question.getCategory());
                Label difficultyLabel = new Label("🎯 Difficulté : " + question.getDifficulty());

                // Bouton de suppression
                Button deleteButton = new Button("🗑️ Supprimer du Quiz");
                deleteButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-padding: 5px 10px;");
                deleteButton.setOnAction(event -> removeQuestionFromQuiz(question));

                HBox buttonBox = new HBox(10, deleteButton);
                container.getChildren().addAll(titleLabel, optionsBox, answerLabel, scoreLabel, categoryLabel, difficultyLabel, buttonBox);
                setGraphic(container);
            }
        }
    }
}
