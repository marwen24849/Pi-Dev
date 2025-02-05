package esprit.tn.pidevrh;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.http.*;
import java.util.Objects;

public class MainApp extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/Fxml/SideBar/sidebar.fxml"));
        Parent root = loader.load();

        Scene scene = new Scene(root);
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/Fxml/SideBar/sidebar.css")).toExternalForm());

        stage.setTitle("Gestion des Questions");
        stage.setScene(scene);
        stage.setResizable(true);

        stage.show();
    }
    //test

    public static void main(String[] args) {
        launch(args);
    }
}
