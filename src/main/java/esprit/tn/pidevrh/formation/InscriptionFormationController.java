package esprit.tn.pidevrh.formation;

import esprit.tn.pidevrh.connection.DatabaseConnection;
import esprit.tn.pidevrh.session.inscriptionSessionController;
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

            {

                manageButton.setOnAction(event -> {
                    Formation selectedFormation = getItem();
                    if (selectedFormation != null) {
                        long formationId = selectedFormation.getId();
                        handleManageSessions(formationId);
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
                    titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");

                    // Description label
                    Label descriptionLabel = new Label("Description: " + formation.getDescription());
                    descriptionLabel.setStyle("-fx-font-size: 14px;");

                    // Duration label
                    Label durationLabel = new Label("Durée: " + formation.getDuree() + " jours");
                    durationLabel.setStyle("-fx-font-size: 14px;");

                    // Capacity label
                    Label capacityLabel = new Label("Capacité: " + formation.getCapacity());
                    capacityLabel.setStyle("-fx-font-size: 14px;");

                    // Action buttons in HBox
                    HBox actionButtons = new HBox(10,  manageButton);
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

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
