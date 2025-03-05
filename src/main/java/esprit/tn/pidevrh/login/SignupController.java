package esprit.tn.pidevrh.login;

import esprit.tn.pidevrh.connection.DatabaseConnection;
import esprit.tn.pidevrh.email.EmailService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import java.io.IOException;
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Objects;

import org.mindrot.jbcrypt.BCrypt;

public class SignupController {

    @FXML
    private Button loginButton;
    @FXML
    private TextField firstNameField;
    @FXML
    private TextField lastNameField;
    @FXML
    private TextField emailField;
    @FXML
    private TextField passwordField;

    @FXML
    public void handleLoginClick() {
        try {
            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/Fxml/login/Login.fxml")));
            Stage stage = (Stage) loginButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setFullScreen(true);
            stage.show();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void handleSignupClick() {
        String firstName = firstNameField.getText().trim();
        String lastName = lastNameField.getText().trim();
        String email = emailField.getText().trim();
        String password = passwordField.getText().trim();

        if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() || password.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Form Error!", "Please fill all fields.");
            return;
        }

        User.Role defaultRole = User.Role.USER;

        String verificationCode = generateVerificationCode();
        String emailBody = "Your verification code is: " + verificationCode;

        new Thread(() -> EmailService.sendEmail(email, "Verify Your Account", emailBody)).start();


        showVerificationPopUp(verificationCode, firstName, lastName, email, password, defaultRole);

    }

    public  String generateVerificationCode() {
        SecureRandom random = new SecureRandom();
        int code = random.nextInt(999999);
        return String.format("%06d", code);
    }

    private void showVerificationPopUp(String verificationCode, String firstName, String lastName, String email, String password, User.Role role) {
        Stage verificationStage = new Stage();
        verificationStage.setTitle("Verify Your Account");

        javafx.scene.layout.VBox layout = new javafx.scene.layout.VBox(10);

        TextField codeField = new TextField();
        Button verifyButton = new Button("Verify");

        Label promptLabel = new Label("Enter the verification code sent to your email:");

        layout.getChildren().addAll(promptLabel, codeField, verifyButton);

        verifyButton.setOnAction(e -> {
            String enteredCode = codeField.getText().trim();
            if (enteredCode.equals(verificationCode)) {
                String hashedPassword = hashPassword(password);
                saveUserToDatabase(firstName, lastName, email, hashedPassword,  role);
                showAlert(Alert.AlertType.INFORMATION, "Account Verified", "Your account has been successfully verified!");
                verificationStage.close();
            } else {
                showAlert(Alert.AlertType.ERROR, "Verification Failed", "Incorrect verification code.");
            }
        });

        Scene scene = new Scene(layout, 300, 150);
        verificationStage.setScene(scene);
        verificationStage.show();
    }

    private void saveUserToDatabase(String firstName, String lastName, String email, String password,  User.Role role) {
        String sql = "INSERT INTO user (first_name, last_name, email, password,  role)"+" VALUES (?, ?, ?, ?, ?)";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setString(1, firstName);
            preparedStatement.setString(2, lastName);
            preparedStatement.setString(3, email);
            preparedStatement.setString(4, password);
            preparedStatement.setString(5, role.name());
            int rowsAffected = preparedStatement.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("User saved successfully!");
            } else {
                System.out.println("No rows affected.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public String hashPassword(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt());
    }

    private void showAlert(Alert.AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
