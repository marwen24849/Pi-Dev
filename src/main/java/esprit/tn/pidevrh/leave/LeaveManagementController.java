package esprit.tn.pidevrh.leave;

import esprit.tn.pidevrh.email.EmailService;
import esprit.tn.pidevrh.connection.DatabaseConnection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.*;
import java.sql.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class LeaveManagementController {

    @FXML private ListView<Leave> leaveListView;
    private ObservableList<Leave> leaveRequests = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        loadAllLeaveRequests();

        leaveListView.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(Leave leave, boolean empty) {
                super.updateItem(leave, empty);
                if (empty || leave == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    Label userLabel = new Label("👤 Utilisateur: " + getUserName(leave.getUserId()));
                    Label typeLabel = new Label("📋 Type: " + leave.getTypeConge());
                    Label startDateLabel = new Label("📅 Début: " + leave.getDateDebut());
                    Label endDateLabel = new Label("🏁 Fin: " + leave.getDateFin());

                    long duration = ChronoUnit.DAYS.between(leave.getDateDebut(), leave.getDateFin());
                    Label durationLabel = new Label("🕒 Durée: " + duration + " jours");

                    // Apply styles
                    VBox detailsBox = new VBox(5, userLabel, typeLabel, startDateLabel, endDateLabel, durationLabel);
                    detailsBox.setStyle("-fx-padding: 10px; -fx-background-color: #FFFFFF; -fx-border-radius: 8px; " +
                            "-fx-border-color: #E0E4E7; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 8, 0, 0, 3);");

                    // Styled buttons
                    Button approveButton = createStyledButton("✅ Approuver", "#27AE60", "#219653");
                    approveButton.setOnAction(e -> updateLeaveStatus(leave.getId(), "APPROVED"));

                    Button rejectButton = createStyledButton("❌ Refuser", "#E74C3C", "#C0392B");
                    rejectButton.setOnAction(e -> updateLeaveStatus(leave.getId(), "REJECTED"));

                    HBox buttonBox = new HBox(10, approveButton, rejectButton);
                    buttonBox.setStyle("-fx-padding: 10px;");

                    VBox fullBox = new VBox(10, detailsBox, buttonBox);

                    // If leave type is "Maladie", show certificate
                    if ("Maladie".equalsIgnoreCase(leave.getTypeConge()) && leave.getCertificate() != null) {
                        ImageView certImageView = new ImageView(new Image(new ByteArrayInputStream(leave.getCertificate())));
                        certImageView.setFitWidth(100);
                        certImageView.setFitHeight(100);
                        certImageView.setStyle("-fx-border-radius: 6px; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 5, 0, 0, 2);");

                        certImageView.setOnMouseClicked(e -> showLargeCertificate(leave.getCertificate()));

                        Button downloadButton = createStyledButton("⬇ Télécharger Certificat", "#3498db", "#2980b9");
                        downloadButton.setOnAction(e -> downloadCertificate(leave.getCertificate()));

                        fullBox.getChildren().addAll(certImageView, downloadButton);
                    }

                    setGraphic(fullBox);
                }
            }
        });
    }

    private void loadAllLeaveRequests() {
        String query = "SELECT id, user_id, type_congé, COALESCE(status, 'PENDING') AS status, date_debut, date_fin, certificate FROM demande_conge";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query);
             ResultSet resultSet = preparedStatement.executeQuery()) {

            leaveRequests.clear();
            while (resultSet.next()) {
                leaveRequests.add(new Leave(
                        resultSet.getInt("id"),
                        resultSet.getInt("user_id"),
                        resultSet.getString("type_congé"),
                        null,
                        resultSet.getString("status"),
                        null,
                        resultSet.getDate("date_debut").toLocalDate(),
                        resultSet.getDate("date_fin").toLocalDate(),
                        resultSet.getBytes("certificate")
                ));
            }
            leaveListView.setItems(leaveRequests);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void updateLeaveStatus(int leaveId, String newStatus) {
        String updateQuery = "UPDATE demande_conge SET status = ? WHERE id = ?";
        String checkQuery = "SELECT COUNT(*) FROM conge WHERE conge_id = ?";
        String insertQuery = "INSERT INTO conge (start_date, end_date, conge_id, user_id) " +
                "SELECT date_debut, date_fin, id, user_id FROM demande_conge WHERE id = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement updateStmt = connection.prepareStatement(updateQuery);
             PreparedStatement checkStmt = connection.prepareStatement(checkQuery);
             PreparedStatement insertStmt = connection.prepareStatement(insertQuery)) {

            // 1️⃣ Update demande_conge table
            updateStmt.setString(1, newStatus);
            updateStmt.setInt(2, leaveId);
            updateStmt.executeUpdate();

            if ("APPROVED".equalsIgnoreCase(newStatus)) {
                // 2️⃣ Check if the leave already exists in conge table
                checkStmt.setInt(1, leaveId);
                ResultSet rs = checkStmt.executeQuery();
                if (rs.next() && rs.getInt(1) == 0) {
                    // 3️⃣ Insert into conge table if not already inserted
                    insertStmt.setInt(1, leaveId);
                    int rowsInserted = insertStmt.executeUpdate();

                    if (rowsInserted > 0) {
                        System.out.println("✅ Leave inserted into conge table.");
                        Leave leave = getLeaveDetails(leaveId);
                        sendLeaveApprovalEmail(leave);

                    } else {
                        System.out.println("⚠ Failed to insert leave into conge.");
                    }
                } else {
                    System.out.println("⚠ Leave already exists in conge, skipping insertion.");
                }
            }

            // Refresh the UI
            loadAllLeaveRequests();

        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("❌ SQL Error: " + e.getMessage());
        }
    }

    private Leave getLeaveDetails(int leaveId) {
        String query = "SELECT id, user_id, type_congé, autre, justification, status, date_debut, date_fin, certificate FROM demande_conge WHERE id = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            // Set the leaveId in the query
            statement.setInt(1, leaveId);
            ResultSet rs = statement.executeQuery();

            if (rs.next()) {
                // Create and return a Leave object using the data from the ResultSet
                return new Leave(
                        rs.getInt("id"),
                        rs.getInt("user_id"),
                        rs.getString("type_congé"),
                        rs.getString("autre"),
                        rs.getString("justification"),
                        rs.getString("status"),
                        rs.getDate("date_debut").toLocalDate(),
                        rs.getDate("date_fin").toLocalDate(),
                        rs.getBytes("certificate")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("❌ Error fetching leave details: " + e.getMessage());
        }
        return null;  // Return null if no leave found with the given leaveId
    }


    private String getUserName(int userId) {
        String userName = "Utilisateur " + userId;
        String query = "SELECT CONCAT(first_name, ' ', last_name) AS full_name FROM user WHERE id = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setInt(1, userId);
            ResultSet rs = statement.executeQuery();

            if (rs.next()) {
                userName = rs.getString("full_name");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("❌ Error retrieving user name: " + e.getMessage());
        }

        return userName;
    }

    private void showLargeCertificate(byte[] certificateData) {
        if (certificateData == null) {
            showAlert("Erreur", "Aucun certificat disponible.", Alert.AlertType.ERROR);
            return;
        }

        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("Certificat");

        ImageView imageView = new ImageView(new Image(new ByteArrayInputStream(certificateData)));
        imageView.setPreserveRatio(true);
        imageView.setFitWidth(600);

        StackPane pane = new StackPane(imageView);
        pane.setStyle("-fx-background-color: rgba(0,0,0,0.6);");
        pane.setOnMouseClicked(event -> stage.close());

        Scene scene = new Scene(pane, 650, 500);
        stage.setScene(scene);
        stage.show();
    }

    private void downloadCertificate(byte[] certificateData) {
        if (certificateData == null) {
            showAlert("Erreur", "Aucun certificat à télécharger.", Alert.AlertType.ERROR);
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Enregistrer le certificat");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Fichiers Image", "*.png", "*.jpg", "*.jpeg"));
        fileChooser.setInitialFileName("certificat.png");

        File file = fileChooser.showSaveDialog(null);
        if (file != null) {
            try (FileOutputStream fos = new FileOutputStream(file)) {
                fos.write(certificateData);
                showAlert("Succès", "Certificat téléchargé avec succès !", Alert.AlertType.INFORMATION);
            } catch (IOException e) {
                showAlert("Erreur", "Impossible d'enregistrer le certificat.", Alert.AlertType.ERROR);
            }
        }
    }
    private void sendLeaveApprovalEmail(Leave leave) {
        String userEmail = getUserEmail(leave.getUserId());
        if (userEmail == null) return;

        File pdfFile = PDFGenerator.generateLeavePDF(leave);
        if (pdfFile != null) {
            String subject = "Votre Demande de Congé a été Approuvée";
            String content = "Bonjour,\n\nVotre demande de congé a été approuvée. Vous trouverez les détails ci-joints.";
            EmailService.sendEmailWithAttachment(userEmail, subject, content, pdfFile);
        }
    }

    private String getUserEmail(int userId) {
        String query = "SELECT email FROM user WHERE id = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setInt(1, userId);
            ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                return rs.getString("email");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private Button createStyledButton(String text, String color, String hoverColor) {
        Button button = new Button(text);
        button.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white; -fx-font-size: 14px; " +
                "-fx-padding: 10px 15px; -fx-background-radius: 6px; -fx-cursor: hand;");
        button.setOnMouseEntered(e -> button.setStyle("-fx-background-color: " + hoverColor + ";"));
        return button;
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
