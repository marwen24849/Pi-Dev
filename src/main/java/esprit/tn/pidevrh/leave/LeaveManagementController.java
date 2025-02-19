package esprit.tn.pidevrh.leave;

import esprit.tn.pidevrh.connection.DatabaseConnection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
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
                    Label userLabel = new Label("Utilisateur: " + getUserName(leave.getUserId()));
                    Label typeLabel = new Label("Type: " + leave.getTypeConge());
                    Label startDateLabel = new Label("Début: " + leave.getDateDebut());
                    Label endDateLabel = new Label("Fin: " + leave.getDateFin());

                    long duration = ChronoUnit.DAYS.between(leave.getDateDebut(), leave.getDateFin());
                    Label durationLabel = new Label("Durée: " + duration + " jours");


                    String status = (leave.getStatus() != null) ? leave.getStatus() : "PENDING";
                    Label statusLabel = new Label("Statut: " + status);


                    Button approveButton = new Button("✅ Approuver");
                    approveButton.setOnAction(e -> updateLeaveStatus(leave.getId(), "APPROVED"));

                    Button rejectButton = new Button("❌ Refuser");
                    rejectButton.setOnAction(e -> updateLeaveStatus(leave.getId(), "REJECTED"));

                    VBox detailsBox = new VBox(5, userLabel, typeLabel, startDateLabel, endDateLabel, durationLabel, statusLabel);
                    VBox fullBox = new VBox(10, detailsBox, new HBox(10, approveButton, rejectButton));

                    // Si le congé est de type "Maladie", afficher le certificat
                    if ("Maladie".equalsIgnoreCase(leave.getTypeConge()) && leave.getCertificate() != null) {
                        ImageView certImageView = new ImageView(new Image(new ByteArrayInputStream(leave.getCertificate())));
                        certImageView.setFitWidth(100);
                        certImageView.setFitHeight(100);

                        certImageView.setOnMouseClicked(e -> showLargeCertificate(leave.getCertificate()));

                        Button downloadButton = new Button("⬇ Télécharger Certificat");
                        downloadButton.setOnAction(e -> downloadCertificate(leave.getCertificate()));

                        fullBox.getChildren().addAll(certImageView, downloadButton);
                    }

                    setGraphic(fullBox);
                }
            }
        });
    }

    protected void loadAllLeaveRequests() {
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
        String query = "UPDATE demande_conge SET status = ? WHERE id = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, newStatus);
            preparedStatement.setInt(2, leaveId);
            preparedStatement.executeUpdate();
            loadAllLeaveRequests();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private String getUserName(int userId) {
        return "Utilisateur " + userId;
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
                e.printStackTrace();
            }
        }
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }



}
