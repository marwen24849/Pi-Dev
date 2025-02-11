package esprit.tn.pidevrh.quiz;

import esprit.tn.pidevrh.connection.DatabaseConnection;
import esprit.tn.pidevrh.question.Question;
import esprit.tn.pidevrh.question.QuestionUpdateController;
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
import java.util.stream.Stream;

public class QuizListController {

    @FXML
    private TableColumn<Quiz, Void> actionColumn;

    @FXML
    private TableColumn<Quiz, String> categoryColumn;

    @FXML
    private ComboBox<String> categoryFilter;

    @FXML
    private TableColumn<Quiz, String> difficultylevel;

    @FXML
    private Button filterButton;

    @FXML
    private Pagination pagination;

    @FXML
    private TableColumn<String, Double> pourcentage;

    @FXML
    private TableView<Quiz> quizTableView;

    @FXML
    private TableColumn<Quiz, Void> questions;

    @FXML
    private TableColumn<Quiz, String> titleColumn;



    @FXML
    public void initialize() {
        configureColumns();
        ObservableList<Quiz> quizObservableList = loadQuiz();

        quizTableView.setItems(quizObservableList);
       //actionColumn.setCellFactory(createActionColumnFactory());
        categoryFilter.setValue("Toutes les catégories");
        filterButton.setOnAction(event -> {
            String selectedCategory = categoryFilter.getValue();
            if ("Toutes les catégories".equals(selectedCategory)) {
                quizTableView.setItems(quizObservableList);
            }else{
                quizTableView.setItems(quizObservableList.filtered(q-> q.getCategory().equals(selectedCategory)));
            }
        });
        actionColumn.setCellFactory(createActionColumnFactory());
        questions.setCellFactory(createActionColumListQuestions());

        configureCategoryFilter(quizObservableList.stream().map(Quiz::getCategory).distinct());
    }
    private void configureCategoryFilter(Stream<String> stringStream) {
        categoryFilter.getItems().add("Toutes les catégories");
        stringStream.forEach(categorie -> categoryFilter.getItems().add(categorie));

    }
    private void configureColumns() {
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        pourcentage.setCellValueFactory(new PropertyValueFactory<>("minimumSuccessPercentage"));
        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("category"));
        difficultylevel.setCellValueFactory(new PropertyValueFactory<>("difficultylevel"));

    }


    private Callback<TableColumn<Quiz, Void>, TableCell<Quiz, Void>> createActionColumnFactory() {
        return param -> new TableCell<Quiz, Void>() {
            private final Button updateButton = new Button("✏️"); // Icône de modification
            private final Button deleteButton = new Button("🗑️"); // Icône de suppression

            {
                updateButton.getStyleClass().add("button-modifier");
                deleteButton.getStyleClass().add("button-supprimer");

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

    private Callback<TableColumn<Quiz, Void>, TableCell<Quiz, Void>> createActionColumListQuestions() {
        return param -> new TableCell<Quiz, Void>() {
            private final Button listQuestions = new Button("Questions"); // Icône de modification

            {
                //listQuestions.getStyleClass().add("button-modifier");

                listQuestions.setOnAction(event -> handleListQuestion(getTableView().getItems().get(getIndex())));

            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(new HBox(10, listQuestions));
                }
            }
        };
    }

    private void handleListQuestion(Quiz quiz) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Fxml/Quiz/ListQuestions.fxml"));
            Parent root = loader.load();
            ListQuestionQuiz controller = loader.getController();
            controller.setQuiz(quiz.getId());
            Stage stage = new Stage();
            stage.setTitle("List Question");
            stage.setScene(new Scene(root));
            stage.showAndWait();
            initialize();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void handleUpdate(Quiz quiz) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Fxml/Quiz/EditQuizForm.fxml"));
            Parent root = loader.load();
            QuizUpdateController controller = loader.getController();
            controller.setQuiz(quiz);
            Stage stage = new Stage();
            stage.setTitle("Modifier la Quiz");
            stage.setScene(new Scene(root));
            stage.showAndWait();
            initialize();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private ObservableList<Quiz> loadQuiz() {
        ObservableList<Quiz> quizs = FXCollections.observableArrayList();
        String query = "SELECT * FROM quiz";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement statement = conn.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                Quiz quiz= new Quiz();
                quiz.setId(resultSet.getLong("id"));
                quiz.setTitle(resultSet.getString("title"));
                quiz.setDifficultylevel(resultSet.getString("difficultylevel"));
                quiz.setCategory(resultSet.getString("category"));
                quiz.setMinimumSuccessPercentage(resultSet.getDouble("minimum_success_percentage"));
                quizs.add(quiz);
            }
        } catch (SQLException e) {
            showAlert("Erreur de base de données", "Impossible de charger les quiz : " + e.getMessage());
        }

        return quizs;
    }




    private void handleDelete(Quiz quiz) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Suppression");
        alert.setHeaderText("Êtes-vous sûr de vouloir supprimer cette question ?");
        alert.setContentText("Question : " + quiz.getTitle());
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
            initialize();
        } catch (SQLException e) {
            showAlert("Erreur de base de données", "Impossible de supprimer la quiz : " + id);
        }
    }
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

}
