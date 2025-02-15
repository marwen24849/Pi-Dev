package esprit.tn.pidevrh.appbar;

import esprit.tn.pidevrh.login.SessionManager;
import esprit.tn.pidevrh.login.User;
import javafx.animation.TranslateTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.util.Objects;

public class SidebarController {

    @FXML
    private Button logoutButton;

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
    private Label userNameLabel ;

    @FXML
    public void initialize() {
        displayLoggedInUser() ;

        handleToggleSidebar();


    }
    @FXML
    private void displayLoggedInUser() {
        User loggedInUser = SessionManager.getInstance().getUser();
        if (loggedInUser != null) {
            userNameLabel.setText(loggedInUser.getFirstName() + " " + loggedInUser.getLastName());
        } else {
            userNameLabel.setText("Unknown User");
        }
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
    public void handleUserList(){loadContent("/Fxml/Users_list/users_list.fxml");}

    @FXML
    public void  handleReclamation(){ loadContent("/Fxml/Reclamation/Reclamation.fxml");}

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
    public void handleLogout() {
        Alert confirmationAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmationAlert.setTitle("Confirm Logout");
        confirmationAlert.setHeaderText("Are you sure you want to log out?");
        confirmationAlert.setContentText("Click OK to log out or Cancel to stay logged in.");


        ButtonType result = confirmationAlert.showAndWait().orElse(ButtonType.CANCEL);


        if (result == ButtonType.OK) {
            SessionManager.getInstance().logout();
            System.out.println("User logged out!");

            try {

                FXMLLoader loader = new FXMLLoader(getClass().getResource("/Fxml/login/Login.fxml"));
                Stage stage = (Stage) logoutButton.getScene().getWindow();
                stage.setScene(new Scene(loader.load()));
                stage.show();
            } catch (IOException e) {
                e.printStackTrace();
            }
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
