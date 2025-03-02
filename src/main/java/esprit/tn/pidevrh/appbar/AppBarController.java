package esprit.tn.pidevrh.appbar;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class AppBarController {

    @FXML
    private Button list;

    @FXML
    private Button addQuestion;

    @FXML
    private Button addQuiz;


    @FXML
    private Button departmentButton; // New button for Departments



    public void handleListQuestions() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Fxml/Question/ListQuestions.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) list.getScene().getWindow();
            stage.getScene().getStylesheets().add(Objects.requireNonNull(getClass().getResource("/Css/style.css")).toExternalForm());

            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de charger la vue des questions.");
        }
    }
    public void handleAddQuestions(ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Fxml/Question/Question.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) addQuestion.getScene().getWindow();
            stage.getScene().getStylesheets().add(Objects.requireNonNull(getClass().getResource("/Css/style.css")).toExternalForm());
            stage.setScene(new Scene(root));
            stage.setResizable(true);
            stage.show();
        }catch (Exception e){
            showAlert("Erreur", "Impossible de charger la vue des questions.");
        }
    }

    public void handleAddQuiz(ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Fxml/Quiz/Quiz.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) addQuiz.getScene().getWindow();
            stage.getScene().getStylesheets().add(Objects.requireNonNull(getClass().getResource("/Css/style.css")).toExternalForm());
            stage.setScene(new Scene(root));
            stage.show();
        }catch (Exception e){
            e.printStackTrace();
            showAlert("Erreur", "Impossible de charger la vue de Quiz.");
        }
    }


    // New method to handle Departments button
    public void handleDepartments(ActionEvent actionEvent) {
        loadView("/Fxml/TeamDepartment/DepartmentView.fxml", departmentButton, "Erreur", "Impossible de charger la vue des départements.");
    }

    private void loadView(String fxmlPath, Button button, String errorTitle, String errorMessage) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            Stage stage = (Stage) button.getScene().getWindow();
            stage.getScene().getStylesheets().add(Objects.requireNonNull(getClass().getResource("/Css/style.css")).toExternalForm());

            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(errorTitle, errorMessage);
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
