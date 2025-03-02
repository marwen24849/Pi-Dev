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

    @FXML
    public Button gestionQuestionsButton, logoutButton, toggleButton;
    public VBox sidebarMenuContainer;
    public VBox gestionQuizContainer;
    public VBox userQuizContainer;
    public VBox demandeCongeContainer;
    public VBox userListContainer;
    public VBox reclamation;
    public VBox Listreclamation;
    public VBox assistant;
    public Button assistant1;
    public VBox SGRHAssistant;
    public Button sgrhIa;
    public Button RecalmationtList;
    public Button RecalmationtButton;
    public Button userListButton;
    public Button demandeCongeButton;
    public Button userQuizButton;
    public Button gestionQuizButton;
    public VBox gestionQuestionsContainer;
    @FXML
    private VBox sidebarWrapper, gestionQuestionsMenu, gestionQuizMenu, demandeCongeMenu, userQuizMenu;
    @FXML
    private AnchorPane contentArea;
    private boolean isSidebarOpen = true;
    private boolean roleTest=false;

    @FXML
    public void initialize() {
        if(SessionManager.getInstance().getUser() != null)
            roleTest = SessionManager.getInstance().getUser().getRole() == User.Role.ADMIN;
        if (sidebarWrapper == null) {
            System.err.println("Error: Sidebar is null! Check FXML fx:id.");
            return;
        }
        if(roleTest)
            loadContent("/Fxml/Dashboard/admin_dashboard.fxml");

        sidebarWrapper.setTranslateX(0);
        toggleMenuVisibility(gestionQuestionsMenu, false);
        toggleMenuVisibility(gestionQuizMenu, false);
        toggleMenuVisibility(demandeCongeMenu, false);
        manageVisibilityBasedOnRole();
        handleToggleSidebar();
    }

    private void manageVisibilityBasedOnRole() {

        gestionQuestionsContainer.setVisible(roleTest);
        gestionQuestionsContainer.setManaged(roleTest);

        gestionQuizContainer.setVisible(roleTest);
        gestionQuizContainer.setManaged(roleTest);

        userListContainer.setVisible(roleTest);
        userListContainer.setManaged(roleTest);

        Listreclamation.setVisible(roleTest);
        Listreclamation.setManaged(roleTest);

        SGRHAssistant.setVisible(roleTest);
        SGRHAssistant.setManaged(roleTest);


        userQuizContainer.setVisible(!roleTest);
        userQuizContainer.setManaged(!roleTest);

        demandeCongeContainer.setVisible(!roleTest);
        demandeCongeContainer.setManaged(!roleTest);

        reclamation.setVisible(!roleTest);
        reclamation.setManaged(!roleTest);

        assistant.setVisible(!roleTest);
        assistant.setManaged(!roleTest);
    }


    @FXML
    private void handleToggleSidebar() {
        TranslateTransition transition = new TranslateTransition(Duration.millis(300), sidebarWrapper);
        double sidebarWidth = sidebarWrapper.getPrefWidth();

        if (isSidebarOpen) {
            transition.setToX(-sidebarWidth);
            toggleButton.setText("☰");
        } else {
            transition.setToX(0);
            toggleButton.setText("≡");
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
    private void toggleGestionQuestionsMenu() {
        toggleMenuVisibility(gestionQuestionsMenu, !gestionQuestionsMenu.isVisible());
    }

    @FXML
    private void toggleGestionQuizMenu() {
        toggleMenuVisibility(gestionQuizMenu, !gestionQuizMenu.isVisible());
    }
    @FXML
    private void toggleUserQuizMenu() {
        toggleMenuVisibility(userQuizMenu, !userQuizMenu.isVisible());
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

    public void handleUserList(){loadContent("/Fxml/Users_list/users_list.fxml");}

    @FXML
    public void  handleReclamation(){ loadContent("/Fxml/Reclamation/Reclamation.fxml");}

    @FXML
    public void handleReclamationList(){ loadContent("/Fxml/Reclamation/ListReclamations.fxml");}

    @FXML
    public void handleAddDemande(ActionEvent actionEvent) {
        loadContent("/Fxml/Leave/LeaveRequest.fxml");
    }
    @FXML
    public void handleAddFormation() {
        loadContent("/Fxml/Formation/formation.fxml");
    }
    @FXML
    public void handleAddDemande() {
        loadContent("/Fxml/Leave/LeaveRequest.fxml");

    }
    @FXML
    public void handleAssistant(ActionEvent actionEvent) {
        loadContent("/Fxml/chat/chat_view.fxml");
    }
    @FXML
    public void handleAssistantSgrh(ActionEvent actionEvent) {
        loadContent("/Fxml/chat/Chat_SGRH.fxml");
    }
    @FXML
    public void handleListUserQuiz(ActionEvent actionEvent) {
        loadContent("/Fxml/Quiz/UserQuizzesView.fxml");
    }
    @FXML
    public void handleQuizResults(ActionEvent actionEvent) {
        loadContent("/Fxml/Quiz/UserResultatView.fxml");
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
