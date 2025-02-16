package esprit.tn.pidevrh.question;

import esprit.tn.pidevrh.connection.DatabaseConnection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class QuestionListController {

    @FXML
    private ListView<Question> questionListView;

    @FXML
    private ComboBox<String> categoryFilter;

    @FXML
    private Button filterButton;

    @FXML
    private Pagination pagination;

    private final ObservableList<Question> questionObservableList = FXCollections.observableArrayList();

    private static final int ITEMS_PER_PAGE = 5;

    @FXML
    public void initialize() {
        loadQuestions();
        setupCategoryFilter();
        questionListView.setCellFactory(param -> new QuestionListCell());
        filterButton.setOnAction(event -> filterQuestions());

        pagination.setPageCount((int) Math.ceil((double) questionObservableList.size() / ITEMS_PER_PAGE));
        pagination.currentPageIndexProperty().addListener((obs, oldIndex, newIndex) -> updatePage(newIndex.intValue()));

        updatePage(0);
    }

    private void updatePage(int pageIndex) {
        int fromIndex = pageIndex * ITEMS_PER_PAGE;
        int toIndex = Math.min(fromIndex + ITEMS_PER_PAGE, questionObservableList.size());
        questionListView.setItems(FXCollections.observableArrayList(questionObservableList.subList(fromIndex, toIndex)));
    }

    private void loadQuestions() {
        questionObservableList.clear();
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
                questionObservableList.add(question);
            }
        } catch (SQLException e) {
            showAlert("Erreur de base de données", "Impossible de charger les questions : " + e.getMessage());
        }

        pagination.setPageCount((int) Math.ceil((double) questionObservableList.size() / ITEMS_PER_PAGE));
        updatePage(0);
    }

    private void filterQuestions() {
        String selectedCategory = categoryFilter.getValue();
        ObservableList<Question> filteredList;

        if ("Toutes les catégories".equals(selectedCategory) || selectedCategory == null) {
            filteredList = questionObservableList;
        } else {
            filteredList = questionObservableList.filtered(q -> q.getCategory().equals(selectedCategory));
        }

        pagination.setPageCount((int) Math.ceil((double) filteredList.size() / ITEMS_PER_PAGE));
        pagination.currentPageIndexProperty().addListener((obs, oldIndex, newIndex) -> updateFilteredPage(filteredList, newIndex.intValue()));
        updateFilteredPage(filteredList, 0);
    }

    private void updateFilteredPage(ObservableList<Question> filteredList, int pageIndex) {
        int fromIndex = pageIndex * ITEMS_PER_PAGE;
        int toIndex = Math.min(fromIndex + ITEMS_PER_PAGE, filteredList.size());
        if(fromIndex > toIndex)
            return;
        questionListView.setItems(FXCollections.observableArrayList(filteredList.subList(fromIndex, toIndex)));
    }

    private void setupCategoryFilter() {
        categoryFilter.getItems().add("Toutes les catégories");
        questionObservableList.stream()
                .map(Question::getCategory)
                .distinct()
                .forEach(categoryFilter.getItems()::add);
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
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


    class QuestionListCell extends ListCell<Question> {
        @Override
        protected void updateItem(Question question, boolean empty) {
            super.updateItem(question, empty);
            if (empty || question == null) {
                setGraphic(null);
                setText(null);
            } else {
                Label titleLabel = new Label(question.getTitle());
                titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
                Label categoryLabel = new Label("Catégorie : " + question.getCategory());
                categoryLabel.setStyle("-fx-text-fill: #8e44ad;");
                Label difficultyLabel = new Label("Difficulté : " + question.getDifficulty());
                difficultyLabel.setStyle("-fx-text-fill: #c0392b;");
                Label option1Label = new Label("A) " + question.getOption1());
                Label option2Label = new Label("B) " + question.getOption2());
                Label option3Label = new Label("C) " + question.getOption3());
                Label option4Label = new Label("D) " + question.getOption4());
                option1Label.setStyle("-fx-text-fill: #2c3e50;");
                option2Label.setStyle("-fx-text-fill: #2c3e50;");
                option3Label.setStyle("-fx-text-fill: #2c3e50;");
                option4Label.setStyle("-fx-text-fill: #2c3e50;");
                Label correctAnswerLabel = new Label("✅ Réponse correcte : " + question.getRightAnswer());
                correctAnswerLabel.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
                Button updateButton = new Button("✏️ Modifier");
                updateButton.setOnAction(event -> handleUpdate(question));
                Button deleteButton = new Button("🗑️ Supprimer");
                deleteButton.setOnAction(event -> handleDelete(question));
                updateButton.setStyle("-fx-background-color: #f1c40f; -fx-text-fill: white; -fx-padding: 5px 10px;");
                deleteButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-padding: 5px 10px;");
                HBox buttonBox = new HBox(10, updateButton, deleteButton);
                VBox vbox = new VBox(5, titleLabel, categoryLabel, difficultyLabel,
                        option1Label, option2Label, option3Label, option4Label,
                        correctAnswerLabel, buttonBox);
                vbox.setStyle("-fx-padding: 10px; -fx-background-color: #ecf0f1; -fx-border-color: #bdc3c7; -fx-border-radius: 5px;");

                setGraphic(vbox);
            }
        }
    }

}
