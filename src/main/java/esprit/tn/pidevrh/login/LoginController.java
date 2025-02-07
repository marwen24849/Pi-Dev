package esprit.tn.pidevrh.login;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import java.io.IOException;

public class LoginController {

    @FXML
    private Button signUpButton;

    @FXML
    private void handleSignUpClick(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/Fxml/login/Signup.fxml"));

            Stage stage = (Stage) signUpButton.getScene().getWindow();

            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
