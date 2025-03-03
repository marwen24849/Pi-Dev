package esprit.tn.pidevrh.teams_departements;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Departments extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Load the FXML file
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/Fxml/TeamDepartement/DepartmentView.fxml"));
        Parent root = loader.load();

        // Set up the scene
        Scene scene = new Scene(root, 800, 600);

        // Apply the CSS stylesheet
        scene.getStylesheets().add(getClass().getResource("/Css/ActionStyle.css").toExternalForm());

        // Set up the stage
        primaryStage.setTitle("Gestion des Départements");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}