package esprit.tn.pidevrh.quiz;

import esprit.tn.pidevrh.connection.DatabaseConnection;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class QuizUpdateController {

    @FXML
    public TextField quizTime;

    @FXML
    private ComboBox<Double> percentageComboBox;

    @FXML
    private TextField titleField;


    private Quiz quiz;





    public void setQuiz(Quiz quiz) {
        this.quiz = quiz;
        for (int i = 10; i <= 100; i += 10) {
            percentageComboBox.getItems().add((double) i);
        }
        titleField.setText(quiz.getTitle());
        percentageComboBox.setValue(quiz.getMinimumSuccessPercentage());
        quizTime.setText(String.valueOf(quiz.getTime()));

    }

    @FXML
    private void handleSave(){
        String sql = "UPDATE quiz SET title=?, minimum_success_percentage=?, quizTime=? WHERE id=?";


        try (Connection connection = DatabaseConnection.getConnection()){

            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, titleField.getText());
            preparedStatement.setDouble(2, percentageComboBox.getValue());
            preparedStatement.setInt(3, Integer.parseInt(quizTime.getText()));
            preparedStatement.setLong(4,quiz.getId());
            preparedStatement.executeUpdate();
        }catch(SQLException e) {
            e.printStackTrace();
            showAlert("Erreur SQL", "Erreur lors de l'edit del quiz dans la base de données.");
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