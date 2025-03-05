package esprit.tn.pidevrh.quiz;

import esprit.tn.pidevrh.connection.DatabaseConnection;
import esprit.tn.pidevrh.login.SessionManager;
import esprit.tn.pidevrh.response.ResponseController;
import esprit.tn.pidevrh.response.ResultController;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
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

public class UserResultatController {

    public Button resultQuizButton;
    @FXML
    private ListView<Quiz> quizzesListView;

    private Long loggedInUserId;


    public void initialize() {
        setUserId();
        loadUserQuizzes();
    }

    private void setUserId() {
        if(SessionManager.getInstance().getUser() != null)
            this.loggedInUserId = SessionManager.getInstance().getUser().getId();

    }

    private void loadUserQuizzes() {
        ObservableList<Quiz> quizzes = FXCollections.observableArrayList();
        String query = """
        
                SELECT q.id, q.title, q.category, q.difficultylevel, q.quizTime, q.minimum_success_percentage, r.resultat_id
        FROM quiz q
        JOIN user_quiz uq ON q.id = uq.quiz_id
        LEFT JOIN response r ON q.id = r.quiz_id AND r.user_id = uq.user_id
        WHERE uq.user_id = ? AND r.id IS Not NULL
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setLong(1, loggedInUserId);
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
        resultQuizButton.setDisable(true);
        quizzesListView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            resultQuizButton.setDisable(newSelection == null);
        });
    }
    private String getDifficultyColor(String difficulty) {
        return switch (difficulty.toLowerCase()) {
            case "facile" -> "#2ecc71";
            case "moyen" -> "#f1c40f";
            case "difficile" -> "#e74c3c";
            default -> "#7f8c8d";
        };
    }





    public void handleResultatQuiz() {
        Quiz selectedQuiz = quizzesListView.getSelectionModel().getSelectedItem();

        if (selectedQuiz != null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/Fxml/Resultat/result.fxml"));
                Parent root = loader.load();
                ResultController controller = loader.getController();
                controller.initializeData(getIdResultatFromQuizUserId(selectedQuiz.getId()));
                Stage stage = new Stage();
                stage.initModality(Modality.APPLICATION_MODAL);
                stage.setTitle("Resultat Quiz " + selectedQuiz.getTitle());
                stage.setScene(new Scene(root));
                stage.showAndWait();
                initialize();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private Long getIdResultatFromQuizUserId(Long quizId){
        try(Connection con= DatabaseConnection.getConnection()) {
            String sql = """
                    select resultat_id
                    from response
                    where user_id=? AND quiz_id=?
                 """;
            PreparedStatement ps= con.prepareStatement(sql);
            ps.setLong(1,loggedInUserId);
            ps.setLong(2,quizId);
            ResultSet r = ps.executeQuery();
            while(r.next()){
                return r.getLong("resultat_id");
            }

        }catch(SQLException e){
            showAlert("Erreur", "Impossible de charger les quiz assignés.");
        }
        return null;
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
