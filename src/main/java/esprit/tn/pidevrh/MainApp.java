package esprit.tn.pidevrh;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/Fxml/SideBar/sidebar.fxml"));
        Scene scene = new Scene(loader.load());
        primaryStage.setScene(scene);
        primaryStage.setTitle("RH Leave Management");
        primaryStage.show();
    }
    //test

    public static void main(String[] args) {
        launch(args);
    }
}
