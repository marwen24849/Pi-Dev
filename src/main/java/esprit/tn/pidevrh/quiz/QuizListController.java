package esprit.tn.pidevrh.quiz;

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
public class QuizListController {

    @FXML
    private ListView<Quiz> quizListView;

    @FXML
    private ComboBox<String> categoryFilter;

    @FXML
    private Button filterButton;

    @FXML
    private Pagination pagination;

    private final ObservableList<Quiz> quizObservableList = FXCollections.observableArrayList();

    private static final int ITEMS_PER_PAGE = 5;

    @FXML
    public void initialize() {
        loadQuiz();
        setupCategoryFilter();
        quizListView.setCellFactory(param -> new QuizListCell());
        filterButton.setOnAction(event -> filterQuiz());

        pagination.setPageCount((int) Math.ceil((double) quizObservableList.size() / ITEMS_PER_PAGE));
        pagination.currentPageIndexProperty().addListener((obs, oldIndex, newIndex) -> updatePage(newIndex.intValue()));

        updatePage(0);
    }

    private void updatePage(int pageIndex) {
        int fromIndex = pageIndex * ITEMS_PER_PAGE;
        int toIndex = Math.min(fromIndex + ITEMS_PER_PAGE, quizObservableList.size());
        quizListView.setItems(FXCollections.observableArrayList(quizObservableList.subList(fromIndex, toIndex)));
    }

    private void loadQuiz() {
        quizObservableList.clear();
        String query = "SELECT * FROM quiz";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement statement = conn.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                Quiz quiz = new Quiz();
                quiz.setId(resultSet.getLong("id"));
                quiz.setTitle(resultSet.getString("title"));
                quiz.setDifficultylevel(resultSet.getString("difficultylevel"));
                quiz.setCategory(resultSet.getString("category"));
                quiz.setTime(resultSet.getInt("quizTime"));
                quiz.setMinimumSuccessPercentage(resultSet.getDouble("minimum_success_percentage"));
                quizObservableList.add(quiz);
            }
        } catch (SQLException e) {
            showAlert("Erreur de base de données", "Impossible de charger les quiz : " + e.getMessage());
        }

        pagination.setPageCount((int) Math.ceil((double) quizObservableList.size() / ITEMS_PER_PAGE));
        updatePage(0);
    }

    private void filterQuiz() {
        String selectedCategory = categoryFilter.getValue();
        ObservableList<Quiz> filteredList;

        if ("Toutes les catégories".equals(selectedCategory) || selectedCategory == null) {
            filteredList = quizObservableList;
        } else {
            filteredList = quizObservableList.filtered(q -> q.getCategory().equals(selectedCategory));
        }

        pagination.setPageCount((int) Math.ceil((double) filteredList.size() / ITEMS_PER_PAGE));
        pagination.currentPageIndexProperty().addListener((obs, oldIndex, newIndex) -> updateFilteredPage(filteredList, newIndex.intValue()));
        updateFilteredPage(filteredList, 0);
    }

    private void updateFilteredPage(ObservableList<Quiz> filteredList, int pageIndex) {
        int fromIndex = pageIndex * ITEMS_PER_PAGE;
        int toIndex = Math.min(fromIndex + ITEMS_PER_PAGE, filteredList.size());
        quizListView.setItems(FXCollections.observableArrayList(filteredList.subList(fromIndex, toIndex)));
    }


    private void setupCategoryFilter() {
        categoryFilter.getItems().add("Toutes les catégories");
        quizObservableList.stream()
                .map(Quiz::getCategory)
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



    class QuizListCell extends ListCell<Quiz> {
        @Override
        protected void updateItem(Quiz quiz, boolean empty) {
            super.updateItem(quiz, empty);

            if (empty || quiz == null) {
                setGraphic(null);
                setText(null);
            } else {
                Label titleLabel = new Label(quiz.getTitle());
                titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

                Label categoryLabel = new Label("Catégorie : " + quiz.getCategory());
                categoryLabel.setStyle("-fx-text-fill: #8e44ad;");

                Label percentageLabel = new Label("Validation : " + quiz.getMinimumSuccessPercentage() + "%");
                percentageLabel.setStyle("-fx-text-fill: #2980b9;");

                Label quizTime = new Label("Duréé Quiz : " + quiz.getTime() + "%");
                quizTime.setStyle("-fx-text-fill: #2980b9;");

                Label difficultyLabel = new Label("Difficulté : " + quiz.getDifficultylevel());
                difficultyLabel.setStyle("-fx-text-fill: #c0392b;");


                Button updateButton = new Button("✏️ Modifier");
                Button deleteButton = new Button("🗑️ Supprimer");
                updateButton.setStyle("-fx-background-color: #f1c40f; -fx-text-fill: white; -fx-padding: 5px 10px; -fx-background-radius: 5px;");
                deleteButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-padding: 5px 10px; -fx-background-radius: 5px;");
                updateButton.setOnAction(event -> handleUpdate(quiz));
                deleteButton.setOnAction(event -> handleDelete(quiz));
                Hyperlink viewQuestionsLink = new Hyperlink("📜 Voir Questions");
                viewQuestionsLink.setStyle("-fx-text-fill: #3498db; -fx-font-size: 14px;");
                viewQuestionsLink.setOnAction(event -> handleViewQuestions(quiz));
                HBox buttonBox = new HBox(10, updateButton, deleteButton, viewQuestionsLink);
                VBox vbox = new VBox(6, titleLabel, categoryLabel, percentageLabel,quizTime,difficultyLabel, buttonBox);
                vbox.setStyle("-fx-padding: 10px; -fx-background-color: #ecf0f1; -fx-border-color: #bdc3c7; -fx-border-radius: 5px;");
                setGraphic(vbox);
            }
        }

        private void handleViewQuestions(Quiz quiz) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/Fxml/Quiz/ListQuestions.fxml"));
                Parent root = loader.load();
                ListQuestionQuiz controller = loader.getController();
                controller.setQuiz(quiz.getId(), quiz.getTitle());

                Stage stage = new Stage();
                stage.setTitle("Questions du Quiz : " + quiz.getTitle());
                stage.setScene(new Scene(root));
                stage.show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    private void handleUpdate(Quiz quiz) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Fxml/Quiz/EditQuizForm.fxml"));
            Parent root = loader.load();
            QuizUpdateController controller = loader.getController();
            controller.setQuiz(quiz);
            Stage stage = new Stage();
            stage.setTitle("Modifier le Quiz");
            stage.setScene(new Scene(root));
            stage.showAndWait();
            loadQuiz();
            initialize();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleDelete(Quiz quiz) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Suppression");
        alert.setHeaderText("Êtes-vous sûr de vouloir supprimer ce quiz ?");
        alert.setContentText("Quiz : " + quiz.getTitle());
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                deleteQuiz(quiz.getId());
            }
        });
    }

    private void deleteQuiz(Long id) {
        try (Connection connection = DatabaseConnection.getConnection()) {
            String sql = "DELETE FROM quiz WHERE id=?";
            PreparedStatement p = connection.prepareStatement(sql);
            p.setLong(1, id);
            p.executeUpdate();
            loadQuiz();
        } catch (SQLException e) {
            showAlert("Erreur", "Impossible de supprimer le quiz : " + id);
        }
    }
}
