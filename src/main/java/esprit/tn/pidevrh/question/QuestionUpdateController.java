package esprit.tn.pidevrh.question;


import esprit.tn.pidevrh.connection.DatabaseConnection;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class QuestionUpdateController {

    @FXML
    private TextField titleField, option1Field, option2Field, option3Field, option4Field;
    @FXML
    private TextField rightAnswerField, scoreField, categoryField, difficultyField;

    private Question question;

    public void setQuestion(Question question) {
        this.question = question;


        titleField.setText(question.getTitle());
        option1Field.setText(question.getOption1());
        option2Field.setText(question.getOption2());
        option3Field.setText(question.getOption3());
        option4Field.setText(question.getOption4());
        rightAnswerField.setText(question.getRightAnswer());
        scoreField.setText(String.valueOf(question.getScore()));
        categoryField.setText(question.getCategory());
        difficultyField.setText(question.getDifficulty());
    }

    @FXML
    private void handleSave(){
        String sql = "UPDATE question SET question_title=?, option1=?, option2=?, option3=?, option4=?, right_answer=?, score=?, category=?, difficultylevel=? WHERE id=?";


        try (Connection connection = DatabaseConnection.getConnection()){

            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, titleField.getText());
            preparedStatement.setString(2, option1Field.getText());
            preparedStatement.setString(3, option2Field.getText());
            preparedStatement.setString(4, option3Field.getText());
            preparedStatement.setString(5, option4Field.getText());
            preparedStatement.setString(6, rightAnswerField.getText());
            preparedStatement.setInt(7, Integer.parseInt(scoreField.getText()));
            preparedStatement.setString(8, categoryField.getText());
            preparedStatement.setString(9, difficultyField.getText());
            preparedStatement.setLong(10, question.getId());

            preparedStatement.executeUpdate();

        }catch(SQLException e) {
            e.printStackTrace();
            showAlert("Erreur SQL", "Erreur lors de l'edit de la question dans la base de données.");
        }
        Stage stage = (Stage) titleField.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void handleCancel() {
        Stage stage = (Stage) titleField.getScene().getWindow();
        stage.close();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
