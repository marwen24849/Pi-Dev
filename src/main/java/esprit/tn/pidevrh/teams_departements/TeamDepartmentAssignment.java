package esprit.tn.pidevrh.teams_departements;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class TeamDepartmentAssignment extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/Fxml/TeamDepartement/team-department-assignment.fxml"));
        VBox root = loader.load();
        Scene scene = new Scene(root, 600, 400);
        scene.getStylesheets().add(getClass().getResource("/Css/team-dep.css").toExternalForm());

        primaryStage.setTitle("Team Department Assignment");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}