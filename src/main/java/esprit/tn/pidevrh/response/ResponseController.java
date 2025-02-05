package esprit.tn.pidevrh.response;

import esprit.tn.pidevrh.connection.DatabaseConnection;
import esprit.tn.pidevrh.question.Question;
import esprit.tn.pidevrh.quiz.Quiz;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class ResponseController implements Initializable {

    @FXML
    private Label questionLabel;
    @FXML
    private RadioButton option1, option2, option3, option4;
    @FXML
    private Button nextButton, submitButton;
    @FXML
    private Label questionNumberLabel;

    private ToggleGroup optionsGroup;
    private Quiz quiz;
    private List<Question> questions;
    private int currentQuestionIndex = 0;
    private Map<Long, String> userAnswers = new HashMap<>();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        optionsGroup = new ToggleGroup();
        option1.setToggleGroup(optionsGroup);
        option2.setToggleGroup(optionsGroup);
        option3.setToggleGroup(optionsGroup);
        option4.setToggleGroup(optionsGroup);
        submitButton.setDisable(true);
    }


    public void initializeQuiz(Long quizId) {
        this.quiz = getQuizById(quizId);
        this.questions = getQuestionsQuiz(quizId);

        if (questions.isEmpty()) {
            questionLabel.setText("Aucune question trouvée pour ce quiz.");
            nextButton.setDisable(true);
            submitButton.setDisable(true);
        } else {
            loadQuestion();
        }
    }


    private Quiz getQuizById(Long quizId) {
        Quiz quiz = new Quiz();
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT * FROM quiz WHERE id = ?";
            PreparedStatement ps = conn.prepareStatement(query);
            ps.setLong(1, quizId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                quiz.setId(rs.getLong("id"));
                quiz.setTitle(rs.getString("title"));
                quiz.setCategory(rs.getString("category"));
                quiz.setDifficultylevel(rs.getString("difficultylevel"));
                quiz.setMinimumSuccessPercentage(rs.getDouble("minimum_success_percentage"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return quiz;
    }


    private List<Question> getQuestionsQuiz(Long quizId) {
        List<Question> questions = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT * FROM question q, quiz_questions qq WHERE q.id = qq.questions_id AND qq.quiz_id = ?";
            PreparedStatement ps = conn.prepareStatement(query);
            ps.setLong(1, quizId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Question question = new Question();
                question.setId(rs.getLong("id"));
                question.setTitle(rs.getString("question_title"));
                question.setOption1(rs.getString("option1"));
                question.setOption2(rs.getString("option2"));
                question.setOption3(rs.getString("option3"));
                question.setOption4(rs.getString("option4"));
                question.setRightAnswer(rs.getString("right_answer"));
                question.setScore(rs.getInt("score"));
                questions.add(question);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return questions;
    }


    private void loadQuestion() {
        if (currentQuestionIndex < questions.size()) {
            Question question = questions.get(currentQuestionIndex);

            questionLabel.setText(question.getTitle());
            option1.setText(question.getOption1());
            option2.setText(question.getOption2());
            option3.setText(question.getOption3());
            option4.setText(question.getOption4());

            optionsGroup.selectToggle(null);
            questionNumberLabel.setText("Question " + (currentQuestionIndex + 1) + " sur " + questions.size());

            nextButton.setDisable(false);
            submitButton.setDisable(true);

            if (currentQuestionIndex == questions.size() - 1) {
                nextButton.setDisable(true);
                submitButton.setDisable(false);
            }
        }
    }


    @FXML
    private void handleNext() {
        saveAnswer();
        currentQuestionIndex++;
        loadQuestion();
    }


    @FXML
    private void handleSubmit() {
        saveAnswer();
        calculateResult();
    }


    private void saveAnswer() {
        Question question = questions.get(currentQuestionIndex);
        RadioButton selectedOption = (RadioButton) optionsGroup.getSelectedToggle();
        if (selectedOption != null) {
            userAnswers.put(question.getId(), selectedOption.getText());
        }
    }


    private void calculateResult() {
        int totalScore = 0;
        for (Question question : questions) {
            String userAnswer = userAnswers.get(question.getId());
            if (userAnswer != null && userAnswer.equals(question.getRightAnswer())) {
                totalScore += question.getScore();
            }
        }

        double percentage = (double) totalScore / questions.stream().mapToInt(Question::getScore).sum() * 100;
        boolean passed = percentage >= quiz.getMinimumSuccessPercentage();
        Resultat resultat = new Resultat();
        resultat.setScore(totalScore);
        resultat.setPercentage(percentage);
        resultat.setResultat(passed);
        saveResult(resultat);
        showResult(totalScore, percentage, passed);
    }

    private void saveResult(Resultat resultat) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "INSERT INTO resultat (score, percentage, resultat) VALUES (?, ?, ?)";
            PreparedStatement ps = conn.prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS);
            ps.setInt(1, resultat.getScore());
            ps.setDouble(2, resultat.getPercentage());
            ps.setBoolean(3, resultat.isResultat());
            ps.executeUpdate();

            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                long resultatId = rs.getLong(1);
                saveResponse(resultatId);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void saveResponse(long resultatId) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "INSERT INTO response (id, quiz_id, resultat_id) VALUES (?, ?, ?)";
            PreparedStatement ps = conn.prepareStatement(query);
            String key = UUID.randomUUID().toString();
            ps.setString(1, key);
            ps.setLong(2, quiz.getId());
            ps.setLong(3, resultatId);
            //ps.setLong(4, 1); // Remplacez par l'ID de l'utilisateur connecté
            ps.executeUpdate();
            for (Map.Entry<Long, String> entry : userAnswers.entrySet()) {
                System.out.println(key);
                saveResponseResponses(key, entry);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void saveResponseResponses(String responseId, Map.Entry<Long, String> entry) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "INSERT INTO response_responses (response_id, answer, question) VALUES (?, ?, ?)";
            PreparedStatement ps = conn.prepareStatement(query);
            ps.setString(1,responseId);
            ps.setString(2, entry.getValue());
            ps.setString(3, questions.stream().filter(q -> q.getId() == entry.getKey()).findFirst().get().getTitle());
            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    private void showResult(int totalScore, double percentage, boolean passed) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Résultat du quiz");
        alert.setHeaderText(null);
        alert.setContentText(
                "Score total : " + totalScore + "\n" +
                        "Pourcentage : " + String.format("%.2f", percentage) + "%\n" +
                        "Résultat : " + (passed ? "Réussi" : "Échoué")
        );
        alert.showAndWait();
    }
}