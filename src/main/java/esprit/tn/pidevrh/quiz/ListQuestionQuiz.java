package esprit.tn.pidevrh.quiz;

import esprit.tn.pidevrh.connection.DatabaseConnection;
import esprit.tn.pidevrh.question.Question;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ListQuestionQuiz {


    private Long idQuiz;

    @FXML
    private TableView<Question> questionTableView;

    @FXML
    private TableColumn<Question, String> titleColumn, option1Column, option2Column, option3Column, option4Column,
            rightAnswerColumn, categoryColumn, difficultyColumn;

    @FXML
    private TableColumn<Question, Integer> scoreColumn;

    @FXML
    private TableColumn<Question, Void> actionColumn;

    public void setQuiz(Long idQuiz) {
        this.idQuiz = idQuiz;
        configureColumns();
        questionTableView.setItems(loadQuestions(idQuiz));
    }

    private ObservableList<Question> loadQuestions(Long idQuiz){
        ObservableList<Question> questions = FXCollections.observableArrayList();
        try(Connection conn= DatabaseConnection.getConnection()){
            String sql = "select * from question where id IN (select questions_id from quiz_questions where quiz_id = ?)";
            PreparedStatement ps= conn.prepareStatement(sql);
            ps.setLong(1, idQuiz);
            ResultSet resultSet= ps.executeQuery();
            while(resultSet.next()){
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
        }catch (SQLException e){
            e.printStackTrace();
        }
        return questions;
    }
    private void configureColumns() {
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        option1Column.setCellValueFactory(new PropertyValueFactory<>("option1"));
        option2Column.setCellValueFactory(new PropertyValueFactory<>("option2"));
        option3Column.setCellValueFactory(new PropertyValueFactory<>("option3"));
        option4Column.setCellValueFactory(new PropertyValueFactory<>("option4"));
        rightAnswerColumn.setCellValueFactory(new PropertyValueFactory<>("rightAnswer"));
        scoreColumn.setCellValueFactory(new PropertyValueFactory<>("score"));
        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("category"));
        difficultyColumn.setCellValueFactory(new PropertyValueFactory<>("difficulty"));
    }


}
