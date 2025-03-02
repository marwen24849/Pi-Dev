package esprit.tn.pidevrh.formation;

import esprit.tn.pidevrh.connection.DatabaseConnection;
import esprit.tn.pidevrh.email.EmailService;
import esprit.tn.pidevrh.login.User;
import esprit.tn.pidevrh.session.SessionController;
import esprit.tn.pidevrh.session.inscriptionSessionController;
import esprit.tn.pidevrh.login.SessionManager;
import esprit.tn.pidevrh.login.User;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.IOException;
import java.sql.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class InscriptionFormationController {

    @FXML
    private ListView<Formation> formationListView;

    @FXML
    private TextField searchField;  // Replace ComboBox with a search bar

    private ObservableList<Formation> formationsObservableList;

    private final User loggedInUser = SessionManager.getInstance().getUser();

    @FXML
    public void initialize() {
        // Load formations
        formationsObservableList = loadFormations();
        formationListView.setItems(formationsObservableList);
        formationListView.setCellFactory(createFormationCellFactory());

        // Add a listener to filter the list based on the search query
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filterFormations(newValue);
        });
    }

    private void filterFormations(String query) {
        if (query == null || query.trim().isEmpty()) {
            formationListView.setItems(formationsObservableList); // Show all if search is empty
        } else {
            String lowerCaseQuery = query.toLowerCase();
            ObservableList<Formation> filteredList = FXCollections.observableArrayList(
                    formationsObservableList.stream()
                            .filter(f -> f.getTitre().toLowerCase().contains(lowerCaseQuery))
                            .collect(Collectors.toList())
            );
            formationListView.setItems(filteredList);
        }
    }

    private ObservableList<Formation> loadFormations() {
        ObservableList<Formation> formations = FXCollections.observableArrayList();
        String query = "SELECT * FROM formation";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement statement = conn.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                Formation formation = new Formation();
                formation.setId(resultSet.getLong("id"));
                formation.setTitre(resultSet.getString("title"));
                formation.setDescription(resultSet.getString("description"));
                formation.setDuree(resultSet.getInt("duration"));
                formation.setCapacity(resultSet.getInt("capacity"));
                formations.add(formation);
            }
        } catch (SQLException e) {
            showAlert("Erreur de base de données", "Impossible de charger les formations : " + e.getMessage());
        }
        return formations;
    }

    private Callback<ListView<Formation>, ListCell<Formation>> createFormationCellFactory() {
        return param -> new ListCell<Formation>() {

            private final Button manageButton = new Button("Consulter les sessions");
            private final Button inscriptionButton = new Button("S'inscrire");

            {
                manageButton.getStyleClass().add("manage-button");
                inscriptionButton.getStyleClass().add("inscription-button");
                manageButton.setOnAction(event -> {
                    Formation selectedFormation = getItem();
                    if (selectedFormation != null) {
                        long formationId = selectedFormation.getId();
                        handleManageSessions(formationId);
                    }
                });
                inscriptionButton.getStyleClass().add("inscription-button");
                inscriptionButton.setOnAction(event -> {
                    Formation selectedFormation = getItem();
                    if (selectedFormation != null) {
                        handleInscriptionFormation(selectedFormation);
                    }
                });
            }

            @Override
            protected void updateItem(Formation formation, boolean empty) {
                super.updateItem(formation, empty);
                if (empty || formation == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    // Create a VBox to structure the content
                    VBox vbox = new VBox(10);
                    vbox.setStyle("-fx-padding: 10px; -fx-border-color: #ddd; -fx-border-radius: 5px; -fx-background-color: #f7f7f7;");

                    // Title label
                    Label titleLabel = new Label("Titre: " + formation.getTitre());
                    titleLabel.setStyle("-fx-text-fill: #c0392b ;-fx-font-weight: bold; -fx-font-size: 18px;");

                    // Description label
                    Label descriptionLabel = new Label("Description: " + formation.getDescription());
                    descriptionLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #2c3e50; -fx-font-style: italic; -fx-wrap-text: true; -fx-max-width: 300px;");

                    // Duration label
                    Label durationLabel = new Label("Durée: " + formation.getDuree() + " jours");
                    durationLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #2980b9; -fx-font-style: italic;");

                    // Capacity label
                    Label capacityLabel = new Label("Capacité: " + formation.getCapacity());
                    capacityLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #27ae60; -fx-font-style: italic;");

                    // Action buttons in HBox
                    HBox actionButtons = new HBox(10,  manageButton,inscriptionButton);
                    actionButtons.setStyle("-fx-spacing: 10px;");

                    // Adding all components to the VBox
                    vbox.getChildren().addAll(titleLabel, descriptionLabel, durationLabel,capacityLabel, actionButtons);

                    // Set the graphic for the cell
                    setGraphic(vbox);
                }
            }
        };
    }





    private void handleManageSessions(long formationId) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Fxml/Session/inscriptionSessionsList.fxml"));
            Parent root = loader.load();
            // Get the controller and pass the formation ID
            inscriptionSessionController controller = loader.getController();
            controller.setFormationId(formationId);
            Stage stage = new Stage();
            stage.setTitle("Manage Sessions - Formation ID:" + formationId);
            stage.setScene(new Scene(root));
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleInscriptionFormation(Formation formation) {
        if (loggedInUser == null) {
            showAlert("Non connecté", "Veuillez entrer un login");
            return;
        }

        long userId = loggedInUser.getId();
        long formationId = formation.getId();

        // Queries
        String countQuery = "SELECT COUNT(*) FROM formation_user WHERE formation_id = ?";
        String capacityQuery = "SELECT capacity FROM formation WHERE id = ?";
        String checkUserQuery = "SELECT COUNT(*) FROM formation_user WHERE user_id = ? AND formation_id = ?";
        String insertQuery = "INSERT INTO formation_user (user_id, formation_id) VALUES (?, ?)";
        String sessionsQuery = "SELECT * FROM session WHERE formation_id = ?"; // Query to fetch sessions

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement countStatement = con.prepareStatement(countQuery);
             PreparedStatement capacityStatement = con.prepareStatement(capacityQuery);
             PreparedStatement checkUserStatement = con.prepareStatement(checkUserQuery);
             PreparedStatement sessionsStatement = con.prepareStatement(sessionsQuery)) {

            // Step 1: Get current number of enrolled users
            countStatement.setLong(1, formationId);
            ResultSet countResult = countStatement.executeQuery();
            int enrolledCount = countResult.next() ? countResult.getInt(1) : 0;

            // Step 2: Get formation capacity
            capacityStatement.setLong(1, formationId);
            ResultSet capacityResult = capacityStatement.executeQuery();
            int capacity = capacityResult.next() ? capacityResult.getInt(1) : 0;

            // Step 3: Check if capacity is reached
            if (enrolledCount >= capacity) {
                showAlert("Capacité atteinte", "Désolé, cette formation a atteint sa capacité maximale.");
                return; // Stop execution if no more places are available
            }

            // Step 4: Check if the user is already registered for this formation
            checkUserStatement.setLong(1, userId);
            checkUserStatement.setLong(2, formationId);
            ResultSet checkUserResult = checkUserStatement.executeQuery();
            boolean isAlreadyRegistered = checkUserResult.next() && checkUserResult.getInt(1) > 0;

            if (isAlreadyRegistered) {
                showAlert("Erreur d'inscription", "Vous êtes déjà inscrit à cette formation.");
                return;
            }

            // Step 5: Proceed with the registration
            try (PreparedStatement insertStatement = con.prepareStatement(insertQuery)) {
                insertStatement.setLong(1, userId);
                insertStatement.setLong(2, formationId);
                insertStatement.executeUpdate();
                showSuccessAlert("Inscription réussie", "Vous êtes inscrit à la formation: " + formation.getTitre() + "\n"+"Veuille verifier votre" +
                        " email pour consulter les sessions.");

                // Step 6: Fetch session details for the formation
                sessionsStatement.setLong(1, formationId); // Use sessionsStatement here
                ResultSet sessionsResult = sessionsStatement.executeQuery();

                // Debug: Print column names
                ResultSetMetaData metaData = sessionsResult.getMetaData();
                int columnCount = metaData.getColumnCount();
                for (int i = 1; i <= columnCount; i++) {
                    System.out.println("Column " + i + ": " + metaData.getColumnName(i));
                }

                StringBuilder sessionsInfo = new StringBuilder();
                int rowCount = 0;
                while (sessionsResult.next()) {
                    rowCount++;

                    Date sessionDate = sessionsResult.getDate("date");
                    boolean isOnline = sessionsResult.getBoolean("is_online");

                    sessionsInfo.append("Date: ").append(sessionDate != null ? sessionDate.toString() : "No date").append("\n");

                    if (isOnline) {
                        // Online session: Include Zoom link
                        String zoomLink = sessionsResult.getString("link");
                        sessionsInfo.append("Type: En ligne\n");
                        sessionsInfo.append("Lien Zoom: ").append(zoomLink != null ? zoomLink : "No link").append("\n");
                    } else {
                        // Presentiel session: Include salle
                        String salle = sessionsResult.getString("salle");
                        sessionsInfo.append("Type: Présentiel\n");
                        sessionsInfo.append("Salle: ").append(salle != null ? salle : "No salle").append("\n");
                    }

                    sessionsInfo.append("\n"); // Add a separator between sessions
                }

                System.out.println("Total sessions found: " + rowCount);

                // Step 7: Prepare email content
                String emailContent = "Bonjour, merci de rejoindre la formation: " + formation.getTitre() + "\n\n" +
                        "Voici les sessions:\n\n" + sessionsInfo.toString();

                // Step 8: Send email to the user
                boolean emailSent = EmailService.sendEmail(loggedInUser.getEmail(), "Confirmation d'Inscription à la Formation", emailContent);

                if (!emailSent) {
                    showAlert("Email Error", "Failed to send confirmation email.");
                }
            }

        } catch (SQLException e) {
            showAlert("Erreur de connexion", "Une erreur s'est produite lors de l'inscription : " + e.getMessage());
        }
    }


    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    private void showSuccessAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
