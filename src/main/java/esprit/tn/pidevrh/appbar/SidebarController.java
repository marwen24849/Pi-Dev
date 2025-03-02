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

import javafx.scene.control.ScrollPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import java.io.IOException;

public class SidebarController {

    public Button gestionQuestionsButton;
    public Button formationUserButton;
    public VBox gestionQuestionsContainer;
    @FXML

    private Button logoutButton;

    @FXML
    private VBox sidebar;

    @FXML
    private VBox sidebarWrapper, sidebarMenuContainer, gestionQuestionsMenu, gestionQuizMenu, demandeCongeMenu , gestionFormationMenu ,inscriptionFormationMenu;

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
        toggleMenuVisibility(gestionFormationMenu, false);
        toggleMenuVisibility(inscriptionFormationMenu, false);
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
    private void toggleFormationMenu() {
        toggleMenuVisibility(gestionFormationMenu, !gestionFormationMenu.isVisible());
    }@FXML
    private void toggleInscriptionFormationMenu() {
        toggleMenuVisibility(inscriptionFormationMenu, !inscriptionFormationMenu.isVisible());
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
    public void handleUserList(){loadContent("/Fxml/Users_list/users_list.fxml");}

    @FXML
    public void  handleReclamation(){ loadContent("/Fxml/Reclamation/Reclamation.fxml");}
    @FXML
    public void handleReclamationList(){ loadContent("/Fxml/Reclamation/ListReclamations.fxml");}
    public void handleAssistant(ActionEvent actionEvent) {
        loadContent("/Fxml/chat/chat_view.fxml");
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
    public void handleListFormation(ActionEvent actionEvent) {
        loadContent("/Fxml/Formation/ListFormations.fxml");
    }
    public void handleFormationUser(ActionEvent actionEvent) {
        loadContent("/Fxml/Formation/formationUser.fxml");
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
    public void handleInscriptionFormation() {
        loadContent("/Fxml/Formation/inscriptionFormation.fxml");
    }
    public void handleAddDemande() {
        loadContent("/Fxml/Leave/LeaveRequest.fxml");

    }

}
