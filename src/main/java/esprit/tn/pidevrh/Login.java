package esprit.tn.pidevrh;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class Login extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/Fxml/Login/Login.fxml"));
        Parent root = loader.load();

        Scene scene = new Scene(root);
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/Fxml/Login/login.css")).toExternalForm());


        stage.setTitle("Login");
        stage.setScene(scene);
        stage.setResizable(true);

        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
