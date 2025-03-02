package esprit.tn.pidevrh;

import esprit.tn.pidevrh.response.ResponseController;
import esprit.tn.pidevrh.response.ResultController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class App extends Application {
    @Override
    public void start(Stage stage) throws IOException {
       FXMLLoader loader = new FXMLLoader(getClass().getResource("/Fxml/Resultat/result.fxml"));
        //FXMLLoader loader = new FXMLLoader(getClass().getResource("/Fxml/Response/quiz.fxml"));
        Parent root = loader.load();
        ResultController controller = loader.getController();
        controller.initializeData(22L);

        Scene scene = new Scene(root);
        //scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/Css/style.css")).toExternalForm());

        stage.setTitle("Gestion des Questions");
        stage.setScene(scene);

        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
