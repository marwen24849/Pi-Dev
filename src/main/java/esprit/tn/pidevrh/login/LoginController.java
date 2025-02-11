package esprit.tn.pidevrh.login;

import esprit.tn.pidevrh.connection.DatabaseConnection;
import esprit.tn.pidevrh.email.EmailService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.mindrot.jbcrypt.BCrypt;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;

public class LoginController {

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Button loginButton;

    @FXML
    private Button signUpButton;





    @FXML
    private void handleLoginClick() {
        String email = emailField.getText();
        String password = passwordField.getText();

        if (email.isEmpty() || password.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "Please enter both email and password.");
            return;
        }

        User user = validateLogin(email, password);
        if (user != null) {

            SessionManager.getInstance().setUser(user);

            try {
                Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/Fxml/SideBar/sidebar.fxml")));
                Stage stage = (Stage) loginButton.getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            showAlert(Alert.AlertType.ERROR, "Login Failed", "Invalid email or password.");
        }
    }

    private User validateLogin(String email, String enteredPassword) {
        Connection conn = DatabaseConnection.getConnection();
        String query = "SELECT * FROM user WHERE email = ?";

        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String hashedPassword = rs.getString("password");


                if (BCrypt.checkpw(enteredPassword, hashedPassword)) {
                    return new User(
                            rs.getLong("id"),
                            rs.getString("first_name"),
                            rs.getString("last_name"),
                            rs.getString("email"),
                            hashedPassword,
                            User.Role.valueOf(rs.getString("role"))
                    );
                } else {
                    System.out.println("Password does not match.");
                }
            } else {
                System.out.println("No user found with this email.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }


    private void showAlert(Alert.AlertType error, String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void handleSignUpClick() {
        try {
            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/Fxml/login/Signup.fxml")));
            Stage stage = (Stage) signUpButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setFullScreen(true);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleForgotPasswordClick() {
        showForgotPasswordPopUp();
    }

    private void showForgotPasswordPopUp() {
        Stage popUpStage = new Stage();
        popUpStage.setTitle("Forgot Password");

        VBox layout = new VBox(10);
        TextField emailField = new TextField();
        emailField.setPromptText("Email");
        TextField firstNameField = new TextField();
        firstNameField.setPromptText("First Name");
        TextField lastNameField = new TextField();
        lastNameField.setPromptText("Last Name");
        Button submitButton = new Button("Submit");

        layout.getChildren().addAll(new Label("Enter your details:"), emailField, firstNameField, lastNameField, submitButton);

        submitButton.setOnAction(e -> {
            String email = emailField.getText().trim();
            String firstName = firstNameField.getText().trim();
            String lastName = lastNameField.getText().trim();
            handleForgotPasswordSubmit(email, firstName, lastName);
            popUpStage.close();
        });

        Scene scene = new Scene(layout, 300, 200);
        popUpStage.setScene(scene);
        popUpStage.show();
    }

    private void handleForgotPasswordSubmit(String email, String firstName, String lastName) {
        SignupController signupController = new SignupController();
        String verificationCode = signupController.generateVerificationCode();
        boolean userExists = checkUserExists(email, firstName, lastName);

        if (userExists) {
            String emailBody = "Your verification code is: " + verificationCode;
            boolean emailSent = EmailService.sendEmail(email, "Password Reset Request", emailBody);

            if (emailSent) {
                showVerificationPopUp(verificationCode, email);
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to send verification email.");
            }
        } else {
            showAlert(Alert.AlertType.ERROR, "User Not Found", "No matching user found with this email, first name, and last name.");
        }
    }

    private boolean checkUserExists(String email, String firstName, String lastName) {
        String sql = "SELECT * FROM user WHERE email = ? AND first_name = ? AND last_name = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, email);
            preparedStatement.setString(2, firstName);
            preparedStatement.setString(3, lastName);
            return preparedStatement.executeQuery().next();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void showVerificationPopUp(String verificationCode, String email) {
        Stage verificationStage = new Stage();
        verificationStage.setTitle("Enter Verification Code");

        VBox layout = new VBox(10);
        TextField codeField = new TextField();
        codeField.setPromptText("Verification Code");
        Button verifyButton = new Button("Verify");

        layout.getChildren().addAll(new Label("Enter the verification code sent to your email:"), codeField, verifyButton);

        verifyButton.setOnAction(e -> {
            String enteredCode = codeField.getText().trim();
            if (enteredCode.equals(verificationCode)) {
                showChangePasswordPopUp(email);
                verificationStage.close();
            } else {
                showAlert(Alert.AlertType.ERROR, "Verification Failed", "Incorrect verification code.");
            }
        });

        Scene scene = new Scene(layout, 300, 150);
        verificationStage.setScene(scene);
        verificationStage.show();
    }

    private void showChangePasswordPopUp(String email) {
        Stage changePasswordStage = new Stage();
        changePasswordStage.setTitle("Change Your Password");

        VBox layout = new VBox(10);
        TextField newPasswordField = new TextField();
        newPasswordField.setPromptText("New Password");
        Button changePasswordButton = new Button("Change Password");

        layout.getChildren().addAll(new Label("Enter your new password:"), newPasswordField, changePasswordButton);

        changePasswordButton.setOnAction(e -> {
            String newPassword = newPasswordField.getText().trim();
            if (!newPassword.isEmpty()) {
                updatePasswordInDatabase(email, newPassword);
                showAlert(Alert.AlertType.INFORMATION, "Password Changed", "Your password has been successfully changed!");
                changePasswordStage.close();
            } else {
                showAlert(Alert.AlertType.ERROR, "Invalid Password", "Password cannot be empty.");
            }
        });

        Scene scene = new Scene(layout, 300, 150);
        changePasswordStage.setScene(scene);
        changePasswordStage.show();
    }

    private void updatePasswordInDatabase(String email, String newPassword) {
        SignupController signupController = new SignupController();
        String hashedPassword = signupController.hashPassword(newPassword);

        String sql = "UPDATE user SET password = ? WHERE email = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, hashedPassword);
            preparedStatement.setString(2, email);
            int rowsAffected = preparedStatement.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("Password updated successfully!");
            } else {
                System.out.println("No rows affected.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }



}
