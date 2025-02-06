package esprit.tn.pidevrh.appbar;

import javafx.animation.TranslateTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import java.io.IOException;

public class SidebarController {

    @FXML
    private VBox sidebarWrapper, sidebarMenuContainer, gestionQuestionsMenu, gestionQuizMenu, demandeCongeMenu;

    @FXML
    private Button toggleButton;

    @FXML
    private ScrollPane sidebarScrollPane;

    @FXML
    private AnchorPane contentArea;

    private boolean isSidebarOpen = true; // Sidebar starts open

    @FXML
    public void initialize() {
        if (sidebarWrapper == null) {
            System.err.println("Error: Sidebar is null! Check FXML fx:id.");
            return;
        }

        sidebarWrapper.setTranslateX(0);
        toggleMenuVisibility(gestionQuestionsMenu, false);
        toggleMenuVisibility(gestionQuizMenu, false);
        toggleMenuVisibility(demandeCongeMenu, false);
        handleToggleSidebar();
    }

    @FXML
    private void handleToggleSidebar() {
        TranslateTransition transition = new TranslateTransition(Duration.millis(300), sidebarWrapper);
        double sidebarWidth = sidebarWrapper.getPrefWidth();

        if (isSidebarOpen) {
            transition.setToX(-sidebarWidth);
            toggleButton.setText("☰"); // Collapse icon
        } else {
            transition.setToX(0);
            toggleButton.setText("≡"); // Expand icon
        }
        transition.play();
        isSidebarOpen = !isSidebarOpen;
        adjustContentArea();
    }

    private void adjustContentArea() {
        double sidebarWidth = isSidebarOpen ? sidebarWrapper.getPrefWidth() : 0;
        AnchorPane.setLeftAnchor(contentArea, sidebarWidth);
    }

    private void toggleMenuVisibility(VBox menu, boolean isVisible) {
        menu.setVisible(isVisible);
        menu.setManaged(isVisible);
    }

    @FXML
    private void toggleGestionQuestionsMenu() {
        toggleMenuVisibility(gestionQuestionsMenu, !gestionQuestionsMenu.isVisible());
    }

    @FXML
    private void toggleGestionQuizMenu() {
        toggleMenuVisibility(gestionQuizMenu, !gestionQuizMenu.isVisible());
    }

    @FXML
    private void toggleDemandeCongeMenu() {
        toggleMenuVisibility(demandeCongeMenu, !demandeCongeMenu.isVisible());
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
    public void handleAddDemande(ActionEvent actionEvent) {
        loadContent("/Fxml/Leave/LeaveRequest.fxml");
    }
    @FXML
    public void handleALisDemande(ActionEvent actionEvent) {
        loadContent("/Fxml/Leave/ListRequest.fxml");
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
            showAlert("Erreur", "Impossible de charger la vue demandée: " + fxmlPath);
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void handleAddFormation() {
        loadContent("/Fxml/Formation/formation.fxml");
    }
    public void handleAddDemande() {
        loadContent("/Fxml/Leave/LeaveRequest.fxml");

    }

}
