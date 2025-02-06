package esprit.tn.pidevrh.question;

import esprit.tn.pidevrh.connection.DatabaseConnection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class QuestionListController {

    @FXML
    private TableView<Question> questionTableView;

    @FXML
    private TableColumn<Question, String> titleColumn, option1Column, option2Column, option3Column, option4Column,
            rightAnswerColumn, categoryColumn, difficultyColumn;

    @FXML
    private TableColumn<Question, Integer> scoreColumn;

    @FXML
    private TableColumn<Question, Void> actionColumn;

    @FXML
    private ComboBox<String> categoryFilter;

    @FXML
    private Button filterButton;

    @FXML
    public void initialize() {
        configureColumns();
        ObservableList<Question> questionObservableList = loadQuestions();
        questionTableView.setItems(questionObservableList);
        actionColumn.setCellFactory(createActionColumnFactory());
        categoryFilter.setValue("Toutes les catégories");
        filterButton.setOnAction(event -> {
            String selectedCategory = categoryFilter.getValue();
            if ("Toutes les catégories".equals(selectedCategory)) {
                questionTableView.setItems(questionObservableList);
            }else{
                questionTableView.setItems(questionObservableList.filtered(q-> q.getCategory().equals(selectedCategory)));
            }
        });
        configureCategoryFilter(questionObservableList.stream().map(Question::getCategory).distinct());
    }
    private void configureCategoryFilter(Stream<String> stringStream) {
        categoryFilter.getItems().add("Toutes les catégories");
        stringStream.forEach(categorie -> categoryFilter.getItems().add(categorie));

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

    private Callback<TableColumn<Question, Void>, TableCell<Question, Void>> createActionColumnFactory() {
        return param -> new TableCell<Question, Void>() {
            private final Button updateButton = new Button("Modifier");
            private final Button deleteButton = new Button("Supprimer");

            {
                updateButton.setOnAction(event -> handleUpdate(getTableView().getItems().get(getIndex())));
                deleteButton.setOnAction(event -> handleDelete(getTableView().getItems().get(getIndex())));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(new HBox(10, updateButton, deleteButton));
                }
            }
        };
    }

    private void handleUpdate(Question question) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Fxml/Question/EditQuestionForm.fxml"));
            Parent root = loader.load();
            QuestionUpdateController controller = loader.getController();
            controller.setQuestion(question);
            Stage stage = new Stage();
            stage.setTitle("Modifier la Question");
            stage.setScene(new Scene(root));
            stage.showAndWait();
            initialize();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleDelete(Question question) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Suppression");
        alert.setHeaderText("Êtes-vous sûr de vouloir supprimer cette question ?");
        alert.setContentText("Question : " + question.getTitle());
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                deleteQuestion(question.getId());
            }
        });
    }

    private void deleteQuestion(Long id) {
        try (Connection connection = DatabaseConnection.getConnection()) {
            String sql = "DELETE FROM question WHERE id=?";
            PreparedStatement p = connection.prepareStatement(sql);
            p.setLong(1, id);
            p.executeUpdate();
            initialize();
        } catch (SQLException e) {
            showAlert("Erreur de base de données", "Impossible de supprimer la question : " + id);
        }
    }

    private ObservableList<Question> loadQuestions() {
        ObservableList<Question> questions = FXCollections.observableArrayList();
        String query = "SELECT * FROM question";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement statement = conn.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {

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
            showAlert("Erreur de base de données", "Impossible de charger les questions : " + e.getMessage());
        }

        return questions;
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
