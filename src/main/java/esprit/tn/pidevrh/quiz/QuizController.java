package esprit.tn.pidevrh.quiz;

import esprit.tn.pidevrh.connection.DatabaseConnection;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;

import java.sql.*;
import java.util.HashSet;
import java.util.Set;

public class QuizController {


    @FXML
    private TextField titleField;

    @FXML
    private ComboBox<String> categoryComboBox;

    @FXML
    private ComboBox<String> percentageComboBox;

    @FXML
    private ComboBox<String> difficultyLevelField;

    @FXML
    private TextField questionCountField;
    @FXML
    public TextField quizTime;

    @FXML
    public void initialize() {


        listCategorie();
        listDifeculter();
        categoryComboBox.valueProperty().addListener((obs, oldValue, newValue) -> updateQuestionCount());
        difficultyLevelField.valueProperty().addListener((obs, oldValue, newValue) -> updateQuestionCount());
        for (int i = 50; i <= 100; i += 10) {
            percentageComboBox.getItems().add(String.valueOf(i));
        }
        if(!this.categoryComboBox.getItems().isEmpty())
            questionCountField.setText(String.valueOf(nbQuestion(categoryComboBox.getValue(),difficultyLevelField.getValue())));
        questionCountField.setText("0");
    }

    private void initChamps() {
        this.quizTime.setText("");
        this.questionCountField.setText("");
        this.titleField.setText("");
        this.categoryComboBox.setValue(null);
        this.percentageComboBox.setValue(null);
    }

    @FXML
    public void handleAddQuiz() {
        String title = titleField.getText();
        String category = categoryComboBox.getValue();
        String percentage = percentageComboBox.getValue();
        String selectedDifficulty = difficultyLevelField.getValue();
        String time = quizTime.getText();
        if (title.isEmpty() || category == null || percentage == null) {
            showAlert("Erreur", "Les champs doit non vide ");
            return;
        }
        Long idQuiz= addQuiz(title, category, selectedDifficulty, Integer.parseInt(percentage), Integer.parseInt(time));
        if(idQuiz == -1)
            showAlert("Erreur SQL", "Erreur lors de l'ajout de la quiz dans la base de données.");
        else{
            affecterQuestionQuiz(idQuiz, questionRandom(category,selectedDifficulty, questionCountField.getText()));
            showAlert("INFO", "Ajout Terminer");
            initChamps();
        }

    }

    private void affecterQuestionQuiz(Long idQuiz,Set<Long>idQuestion) {
        String sql = "INSERT INTO quiz_questions (quiz_id, questions_id) VALUES (?, ?)";
        try (Connection con = DatabaseConnection.getConnection()) {
            PreparedStatement ps = con.prepareStatement(sql);
            idQuestion.forEach(idQ->{
                try {
                    ps.setLong(1, idQuiz);
                    ps.setLong(2, idQ);
                    ps.executeUpdate();
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }

            });

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private Long addQuiz(String title, String category, String difficulty, int percentage, int time) {
        String sql = "INSERT INTO quiz (title, category, difficultylevel, minimum_success_percentage, passer, quizTime) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection con = DatabaseConnection.getConnection()) {
            PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, title);
            ps.setString(2, category);
            ps.setString(3, difficulty);
            ps.setInt(4, percentage);
            ps.setBoolean(5,false);
            ps.setInt(6,time);

            int affectedRows = ps.executeUpdate();
            if (affectedRows > 0) {
                ResultSet rs = ps.getGeneratedKeys();
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Erreur SQL", "Erreur lors de l'ajout de la quiz dans la base de données.");
        }
        return (long) -1;
    }


    private Set<Long> questionRandom(String cat, String level, String nbQ){
        String sql = "SELECT id FROM question q WHERE category =? AND difficultylevel =? ORDER BY RAND() LIMIT ?";
        Set<Long> idQs= new HashSet<>();
        try(Connection con= DatabaseConnection.getConnection()){
            PreparedStatement ps= con.prepareStatement(sql);
            ps.setString(1,cat);
            ps.setString(2,level);
            ps.setInt(3,Integer.parseInt(nbQ));
            ResultSet r= ps.executeQuery();
            while(r.next()){
                idQs.add(r.getLong(1));
            }
        }catch(SQLException e){
            e.printStackTrace();
        }
        return idQs;
    }

    private void listCategorie(){
        String sql = "SELECT DISTINCT category\n" +
                "FROM question";
        try(Connection con= DatabaseConnection.getConnection()){
            PreparedStatement ps= con.prepareStatement(sql);
            ResultSet r= ps.executeQuery();
            while(r.next()){
                categoryComboBox.getItems().add(r.getString("category"));
            }
        }catch(SQLException e){
            e.printStackTrace();
        }
    }
    private void listDifeculter(){
        String sql = "SELECT DISTINCT difficultylevel\n" +
                "FROM question";
        try(Connection con= DatabaseConnection.getConnection()){
            PreparedStatement ps= con.prepareStatement(sql);
            ResultSet r= ps.executeQuery();
            while(r.next()){
                difficultyLevelField.getItems().add(r.getString("difficultylevel"));
            }

        }catch(SQLException e){
            e.printStackTrace();
        }
    }
    private int nbQuestion(String categorie, String difeculter) {
        String sql = "SELECT COUNT(id) FROM question WHERE category=? AND difficultylevel=?";
        try (Connection con = DatabaseConnection.getConnection()) {
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, categorie);
            ps.setString(2, difeculter);
            ResultSet r = ps.executeQuery();
            if (r.next()) {
                return r.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
    private void updateQuestionCount() {
        String category = categoryComboBox.getValue();
        String difficulty = difficultyLevelField.getValue();

        if (category != null && difficulty != null) {
            int count = nbQuestion(category, difficulty);
            questionCountField.setText(String.valueOf(count));
        } else {
            questionCountField.setText("0");
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