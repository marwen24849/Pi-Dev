package esprit.tn.pidevrh.appbar;

import javafx.animation.TranslateTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;

public class SidebarController {

    @FXML
    private VBox sidebar;

    @FXML
    private Button toggleButton;

    @FXML
    private AnchorPane contentArea;

    private boolean isSidebarOpen = false;

    @FXML
    public void initialize() {
        handleToggleSidebar();
    }

    @FXML
    private void handleToggleSidebar() {
        TranslateTransition transition = new TranslateTransition(Duration.millis(300), sidebar);
        if (isSidebarOpen) {
            transition.setToX(-sidebar.getPrefWidth());
        } else {
            transition.setToX(0);
        }
        transition.play();
        isSidebarOpen = !isSidebarOpen;
        adjustContentArea();
    }

    private void adjustContentArea() {
        if (isSidebarOpen) {
            AnchorPane.setLeftAnchor(contentArea, sidebar.getPrefWidth());
        } else {
            AnchorPane.setLeftAnchor(contentArea, 0.0);
        }
        Scene scene = contentArea.getScene();
        if (scene != null) {
            Stage stage = (Stage) scene.getWindow();
            stage.setResizable(true);
        }
    }

    @FXML
    public void handleListQuestions() {
        loadContent("/Fxml/Question/ListQuestions.fxml");
    }

    @FXML
    public void handleListQuiz() {
        loadContent("/Fxml/Quiz/ListQuiz.fxml");
    }

    @FXML
    public void handleAddQuestions(ActionEvent actionEvent) {
        loadContent("/Fxml/Question/Question.fxml");
    }

    @FXML
    public void handleAddQuiz(ActionEvent actionEvent) {
        loadContent("/Fxml/Quiz/Quiz.fxml");
    }

    @FXML
    public void handleDepartments(ActionEvent actionEvent) {
        loadContent("/Fxml/TeamDepartement/DepartmentView.fxml");
    }

    @FXML
    public void handleTeam(ActionEvent actionEvent) {
        loadContent("/Fxml/TeamDepartement/team-department-assignment.fxml");
    }

    //"Gestion de Projet"
    @FXML
    public void handleProjectManagement(ActionEvent actionEvent) {
        loadContent("/Fxml/Projet/projet.fxml");
    }

    private void loadContent(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            contentArea.getChildren().clear();
            contentArea.getChildren().add(root);

            AnchorPane.setTopAnchor(root, 0.0);
            AnchorPane.setRightAnchor(root, 0.0);
            AnchorPane.setBottomAnchor(root, 0.0);
            AnchorPane.setLeftAnchor(root, 0.0);

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de charger la vue demandée.");
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
