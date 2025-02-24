package esprit.tn.pidevrh.question;

import esprit.tn.pidevrh.connection.DatabaseConnection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class QuestionController {
    @FXML
    private TextField titleField;
    @FXML
    private TextField option1Field;
    @FXML
    private TextField option2Field;
    @FXML
    private TextField option3Field;
    @FXML
    private TextField option4Field;
    @FXML
    private TextField rightAnswerField;
    @FXML
    private TextField scoreField;
    @FXML
    private TextField categoryField;
    @FXML
    private TextField difficultyField;

    @FXML
    private Button addQuestionButton;




    public void initialize() {

        addQuestionButton.setOnAction(event -> handleAddQuestion());
        addQuestionButton.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                handleAddQuestion();
            }
        });
    }



    @FXML
    private void handleAddQuestion() {
        // Récupérer les informations du formulaire
        String title = titleField.getText().trim();
        String option1 = option1Field.getText().trim();
        String option2 = option2Field.getText().trim();
        String option3 = option3Field.getText().trim();
        String option4 = option4Field.getText().trim();
        String rightAnswer = rightAnswerField.getText().trim();
        String scoreText = scoreField.getText().trim();
        String category = categoryField.getText().trim();
        String difficulty = difficultyField.getText().trim();

        if (isValidInput(title, option1, option2, option3, option4, rightAnswer, scoreText, category, difficulty)) {
            int score = Integer.parseInt(scoreText);
            insertQuestion(title, option1, option2, option3, option4, rightAnswer, score, category, difficulty);
        }
    }

    private boolean isValidInput(String title, String option1, String option2, String option3, String option4,
                                 String rightAnswer, String scoreText, String category, String difficulty) {
        if (title.isEmpty() || option1.isEmpty() || option2.isEmpty() || option3.isEmpty() || option4.isEmpty() ||
                rightAnswer.isEmpty() || scoreText.isEmpty() || category.isEmpty() || difficulty.isEmpty()) {
            showAlert("Erreur", "Tous les champs doivent être remplis.");
            return false;
        }

        try {
            Integer.parseInt(scoreText); // Vérifier si le score est un entier
        } catch (NumberFormatException e) {
            showAlert("Erreur de format", "Le score doit être un nombre entier.");
            return false;
        }

        return true;
    }

    private void initChamp(){
        this.categoryField.clear();
        this.difficultyField.clear();
        this.option1Field.clear();
        this.option2Field.clear();
        this.option3Field.clear();
        this.option4Field.clear();
        this.rightAnswerField.clear();
        this.scoreField.clear();
        this.titleField.clear();
    }

    private void insertQuestion(String title, String option1, String option2, String option3, String option4,
                                String rightAnswer, int score, String category, String difficulty) {
        String sql = "INSERT INTO question (question_title, option1, option2, option3, option4, right_answer, score, category, difficultylevel) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setString(1, title);
            preparedStatement.setString(2, option1);
            preparedStatement.setString(3, option2);
            preparedStatement.setString(4, option3);
            preparedStatement.setString(5, option4);
            preparedStatement.setString(6, rightAnswer);
            preparedStatement.setInt(7, score);
            preparedStatement.setString(8, category);
            preparedStatement.setString(9, difficulty);

            int rowsAffected = preparedStatement.executeUpdate();
            if (rowsAffected > 0) {

                showAlert("Succès", "La question a été ajoutée avec succès !");
                initChamp();
            }

        } catch (SQLException e) {
            showAlert("Erreur SQL", "Erreur lors de l'ajout de la question dans la base de données.");
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
