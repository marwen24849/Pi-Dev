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
import java.util.Objects;

public class SidebarController {

    @FXML
    private VBox sidebar;

    @FXML
    private Button toggleButton;

    @FXML
    private AnchorPane contentArea;

    private boolean isSidebarOpen = false;

    @FXML
    private AnchorPane content;

    @FXML
    public void initialize() {
        // Pas besoin de l'écouteur ici pour setResizable.
        // Cela peut être fait dans loadContent.
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

    private void loadContent(String fxmlPath) {
        try {
            // Charger le FXML et créer un Parent
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            // Effacer le contenu actuel de la zone de contenu et ajouter le nouveau
            contentArea.getChildren().clear();
            contentArea.getChildren().add(root);

            // Définir les contraintes d'ancrage pour que le contenu s'adapte à la taille de contentArea
            AnchorPane.setTopAnchor(root, 0.0);
            AnchorPane.setRightAnchor(root, 0.0);
            AnchorPane.setBottomAnchor(root, 0.0);
            AnchorPane.setLeftAnchor(root, 0.0);

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de charger la vue demandée.");
        }
    }

    @FXML
    public void handleAddPoste(ActionEvent event) {loadContent("/Fxml/Poste/PosteAdd.fxml");}

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
