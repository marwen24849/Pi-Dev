package esprit.tn.pidevrh.login;


import esprit.tn.pidevrh.connection.DatabaseConnection;
import esprit.tn.pidevrh.email.EmailService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
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


    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);


        DialogPane dialogPane = alert.getDialogPane();


        dialogPane.setStyle(
                "-fx-background-color: #FFFFFF;" +
                        "-fx-border-color: #E0E4E7;" +
                        "-fx-border-radius: 8px;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 2);" +
                        "-fx-font-family: 'Segoe UI';" +
                        "-fx-font-size: 14px;"
        );


        Label contentLabel = (Label) dialogPane.lookup(".content.label");
        if (contentLabel != null) {
            contentLabel.setStyle("-fx-text-fill: #2D3640;");
        }


        dialogPane.getButtonTypes().forEach(buttonType -> {
            Button button = (Button) dialogPane.lookupButton(buttonType);
            button.setStyle(
                    "-fx-background-color: #6FB2FF;" +
                            "-fx-text-fill: white;" +
                            "-fx-font-weight: 600;" +
                            "-fx-background-radius: 6px;" +
                            "-fx-padding: 8px 20px;" +
                            "-fx-cursor: hand;"
            );


            button.setOnMouseEntered(e -> button.setStyle(
                    "-fx-background-color: #5AA6FF;" +
                            "-fx-text-fill: white;"
            ));
            button.setOnMouseExited(e -> button.setStyle(
                    "-fx-background-color: #6FB2FF;" +
                            "-fx-text-fill: white;"
            ));
        });


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
        popUpStage.setTitle("Password Recovery");

        // Main container
        VBox layout = new VBox(15);
        layout.setPadding(new Insets(25, 30, 25, 30));
        layout.setStyle("-fx-background-color: #FFFFFF; " +
                "-fx-border-color: #E0E4E7; " +
                "-fx-border-radius: 8px; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 2);");

        // Title
        Label titleLabel = new Label("Password Recovery");
        titleLabel.setStyle("-fx-text-fill: #2D3640; " +
                "-fx-font-size: 20px; " +
                "-fx-font-weight: 600; " +
                "-fx-padding: 0 0 15px 0;");


        TextField emailField = createStyledField("Email");
        TextField firstNameField = createStyledField("First Name");
        TextField lastNameField = createStyledField("Last Name");


        Button submitButton = new Button("Verify Identity");
        submitButton.setStyle("-fx-background-color: #6FB2FF; " +
                "-fx-text-fill: white; " +
                "-fx-font-weight: 600; " +
                "-fx-font-size: 14px; " +
                "-fx-padding: 10px 25px; " +
                "-fx-background-radius: 6px; " +
                "-fx-cursor: hand;");

        submitButton.setOnMouseEntered(e -> submitButton.setStyle("-fx-background-color: #5AA6FF;"));
        submitButton.setOnMouseExited(e -> submitButton.setStyle("-fx-background-color: #6FB2FF;"));

        submitButton.setOnAction(e -> {
            String email = emailField.getText().trim();
            String firstName = firstNameField.getText().trim();
            String lastName = lastNameField.getText().trim();
            handleForgotPasswordSubmit(email, firstName, lastName);
            popUpStage.close();
        });


        layout.getChildren().addAll(titleLabel, emailField, firstNameField, lastNameField, submitButton);

        Scene scene = new Scene(layout);
        scene.setFill(Color.valueOf("#F8FAFC"));
        popUpStage.setScene(scene);
        popUpStage.setWidth(350);
        popUpStage.setHeight(350);
        popUpStage.show();
    }

    private TextField createStyledField(String prompt) {
        TextField field = new TextField();
        field.setPromptText(prompt);
        field.setStyle("-fx-background-color: #FFFFFF; " +
                "-fx-border-color: #D1D9E0; " +
                "-fx-border-radius: 6px; " +
                "-fx-padding: 10px 15px; " +
                "-fx-font-size: 14px; " +
                "-fx-text-fill: #2D3640;");
        field.setPrefHeight(40);
        return field;
    }

    private void handleForgotPasswordSubmit(String email, String firstName, String lastName) {
        SignupController signupController = new SignupController();
        String verificationCode = signupController.generateVerificationCode();
        boolean userExists = checkUserExists(email, firstName, lastName);

        if (userExists) {
            String emailBody = "Your verification code is: " + verificationCode;
            new Thread(() -> EmailService.sendEmail(email, "Password Reset Request", emailBody)).start();

            showVerificationPopUp(verificationCode, email);

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
        verificationStage.setTitle("Verification Required");


        VBox layout = new VBox(15);
        layout.setPadding(new Insets(25, 30, 25, 30));
        layout.setStyle("-fx-background-color: #FFFFFF; " +
                "-fx-border-color: #E0E4E7; " +
                "-fx-border-radius: 8px; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 2);");


        Label titleLabel = new Label("Verify Your Identity");
        titleLabel.setStyle("-fx-text-fill: #2D3640; " +
                "-fx-font-size: 18px; " +
                "-fx-font-weight: 600; " +
                "-fx-padding: 0 0 10px 0;");


        Label instructionLabel = new Label("Enter the verification code sent to:");
        Label emailLabel = new Label(email);
        emailLabel.setStyle("-fx-text-fill: #3A4045; -fx-font-weight: 500;");


        TextField codeField = new TextField();
        codeField.setPromptText("Verification Code");
        codeField.setStyle("-fx-background-color: #FFFFFF; " +
                "-fx-border-color: #D1D9E0; " +
                "-fx-border-radius: 6px; " +
                "-fx-padding: 10px 15px; " +
                "-fx-font-size: 14px;");
        codeField.setPrefHeight(40);


        Button verifyButton = new Button("Verify");
        verifyButton.setStyle("-fx-background-color: #6FB2FF; " +
                "-fx-text-fill: white; " +
                "-fx-font-weight: 600; " +
                "-fx-font-size: 14px; " +
                "-fx-padding: 10px 25px; " +
                "-fx-background-radius: 6px; " +
                "-fx-cursor: hand;");


        verifyButton.setOnMouseEntered(e -> verifyButton.setStyle("-fx-background-color: #5AA6FF;"));
        verifyButton.setOnMouseExited(e -> verifyButton.setStyle("-fx-background-color: #6FB2FF;"));


        VBox textContainer = new VBox(5, instructionLabel, emailLabel);
        layout.getChildren().addAll(titleLabel, textContainer, codeField, verifyButton);


        Scene scene = new Scene(layout);
        scene.setFill(Color.valueOf("#F8FAFC"));
        verificationStage.setScene(scene);
        verificationStage.setWidth(380);
        verificationStage.setHeight(280);
        verificationStage.show();

        verifyButton.setOnAction(e -> {
            String enteredCode = codeField.getText().trim();
            if (enteredCode.equals(verificationCode)) {
                showChangePasswordPopUp(email);
                verificationStage.close();
            } else {
                showAlert(Alert.AlertType.ERROR, "Verification Failed", "Incorrect verification code.");
            }
        });
    }

    private void showChangePasswordPopUp(String email) {
        Stage changePasswordStage = new Stage();
        changePasswordStage.setTitle("Change Password");


        VBox layout = new VBox(15);
        layout.setPadding(new Insets(25, 30, 25, 30));
        layout.setStyle("-fx-background-color: #FFFFFF; " +
                "-fx-border-color: #E0E4E7; " +
                "-fx-border-radius: 8px; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 2);");


        Label titleLabel = new Label("Set New Password");
        titleLabel.setStyle("-fx-text-fill: #2D3640; " +
                "-fx-font-size: 18px; " +
                "-fx-font-weight: 600; " +
                "-fx-padding: 0 0 15px 0;");


        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("New Password");
        passwordField.setStyle("-fx-background-color: #FFFFFF; " +
                "-fx-border-color: #D1D9E0; " +
                "-fx-border-radius: 6px; " +
                "-fx-padding: 10px 15px; " +
                "-fx-font-size: 14px;");
        passwordField.setPrefHeight(40);


        Button changeButton = new Button("Update Password");
        changeButton.setStyle("-fx-background-color: #6FB2FF; " +
                "-fx-text-fill: white; " +
                "-fx-font-weight: 600; " +
                "-fx-font-size: 14px; " +
                "-fx-padding: 10px 25px; " +
                "-fx-background-radius: 6px; " +
                "-fx-cursor: hand;");


        changeButton.setOnMouseEntered(e -> changeButton.setStyle("-fx-background-color: #5AA6FF;"));
        changeButton.setOnMouseExited(e -> changeButton.setStyle("-fx-background-color: #6FB2FF;"));


        layout.getChildren().addAll(titleLabel, passwordField, changeButton);


        Scene scene = new Scene(layout);
        scene.setFill(Color.valueOf("#F8FAFC"));
        changePasswordStage.setScene(scene);
        changePasswordStage.setWidth(350);
        changePasswordStage.setHeight(250);
        changePasswordStage.show();

        changeButton.setOnAction(e -> {
            String newPassword = passwordField.getText().trim();
            if (!newPassword.isEmpty()) {
                updatePasswordInDatabase(email, newPassword);
                showAlert(Alert.AlertType.INFORMATION, "Success", "Password updated successfully!");
                changePasswordStage.close();
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Password cannot be empty.");
            }
        });
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
