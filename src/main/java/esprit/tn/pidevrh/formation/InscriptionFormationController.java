package esprit.tn.pidevrh.formation;

import esprit.tn.pidevrh.connection.DatabaseConnection;
import esprit.tn.pidevrh.session.SessionListController;
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
    private ComboBox<String> titleFilter;

    @FXML
    private Button filterButton;

    @FXML
    public void initialize() {
        ObservableList<Formation> formationsObservableList = loadFormations();
        formationListView.setItems(formationsObservableList);
        formationListView.setCellFactory(createFormationCellFactory());

        // Initialize the filter ComboBox
        titleFilter.setValue("Toutes les formations");

        filterButton.setOnAction(event -> {
            String selectedTitle = titleFilter.getValue();

            // Filtering the observable list based on the selected title
            ObservableList<Formation> filteredFormations;
            if ("Toutes les formations".equals(selectedTitle)) {
                // Reset to show all formations without filtering
                filteredFormations = formationsObservableList;
            } else {
                filteredFormations = FXCollections.observableArrayList(
                        formationsObservableList.stream()
                                .filter(f -> f.getTitre().equals(selectedTitle))
                                .collect(Collectors.toList())
                );
            }

            // Set the filtered items to the list view
            formationListView.setItems(filteredFormations);
        });

        configureTitleFilter(formationsObservableList.stream().map(Formation::getTitre).distinct());
    }

    private void configureTitleFilter(Stream<String> stringStream) {
        titleFilter.getItems().clear();
        titleFilter.getItems().add("Toutes les formations");
        stringStream.forEach(title -> titleFilter.getItems().add(title));
        titleFilter.setValue("Toutes les formations");
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

                formations.add(formation);
            }
        } catch (SQLException e) {
            showAlert("Erreur de base de données", "Impossible de charger les formations : " + e.getMessage());
        }

        return formations;
    }

    private Callback<ListView<Formation>, ListCell<Formation>> createFormationCellFactory() {
        return param -> new ListCell<Formation>() {
            private final Button inscriptionButton = new Button("S'inscrire");
            private final Button manageButton = new Button("Consulter les sessions");

            {
                inscriptionButton.setOnAction(event -> handleInscription(getItem()));
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

                    // Action buttons in HBox
                    HBox actionButtons = new HBox(10, inscriptionButton, manageButton);
                    actionButtons.setStyle("-fx-spacing: 10px;");

                    // Adding all components to the VBox
                    vbox.getChildren().addAll(titleLabel, descriptionLabel, durationLabel, actionButtons);

                    // Set the graphic for the cell
                    setGraphic(vbox);
                }
            }
        };
    }

    private void handleInscription(Formation formation) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Inscription");
        alert.setHeaderText("Êtes-vous sûr de vouloir s'insrire a cette formation ?");
        alert.setContentText("Formation : " + formation.getTitre());
        alert.showAndWait();
    }





    private void handleManageSessions(long formationId) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Fxml/Session/SessionList.fxml"));
            Parent root = loader.load();
            // Get the controller and pass the formation ID
            SessionListController controller = loader.getController();
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
