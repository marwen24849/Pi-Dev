package esprit.tn.pidevrh.formation;

import esprit.tn.pidevrh.connection.DatabaseConnection;
import esprit.tn.pidevrh.question.Question;
import esprit.tn.pidevrh.question.QuestionUpdateController;
import esprit.tn.pidevrh.session.SessionListController;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FormationListController {

    @FXML
    private TableView<Formation> formationTableView;

    @FXML
    private TableColumn<Formation, String> titleColumn, descriptionColumn;
    @FXML
    private TableColumn<Formation, Integer> durationColumn;

    @FXML
    private TableColumn<Formation, Void> actionColumn;

    @FXML
    private TableColumn<Formation, Void> sessionColumn;
    @FXML
    private ComboBox<String> titleFilter;

    @FXML
    private Button filterButton;

    @FXML
    public void initialize() {
        configureColumns();
        ObservableList<Formation> formationsObservableList = loadFormations();
        formationTableView.setItems(formationsObservableList);
        actionColumn.setCellFactory(createActionColumnFactory());
        sessionColumn.setCellFactory(createSessionColumnFactory());
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

            // Set the filtered items to the table view and reapply the action buttons column factory
            formationTableView.setItems(filteredFormations);
            actionColumn.setCellFactory(createActionColumnFactory());
        });

        configureTitleFilter(formationsObservableList.stream().map(Formation::getTitre).distinct());
    }
    private void configureTitleFilter(Stream<String> stringStream) {
        titleFilter.getItems().clear();
        titleFilter.getItems().add("Toutes les formations");
        stringStream.forEach(title -> titleFilter.getItems().add(title));
        titleFilter.setValue("Toutes les formations");

    }

    private void configureColumns() {
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("titre"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        durationColumn.setCellValueFactory(new PropertyValueFactory<>("duree"));

    }

    private Callback<TableColumn<Formation, Void>, TableCell<Formation, Void>> createActionColumnFactory() {
        return param -> new TableCell<Formation, Void>() {
            private final Button updateButton = new Button("Modifier");
            private final Button deleteButton = new Button("Supprimer");

            {
                updateButton.setOnAction(event -> handleUpdate(getTableView().getItems().get(getIndex())));
                deleteButton.setOnAction(event -> handleDelete(getTableView().getItems().get(getIndex())));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(new HBox(10, updateButton, deleteButton));
                }
            }
        };
    }


    private void handleUpdate(Formation formation) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Fxml/Formation/EditFormationForm.fxml"));
            Parent root = loader.load();
            FormationUpdateController controller = loader.getController();
            controller.setFormation(formation);
            Stage stage = new Stage();
            stage.setTitle("Modifier la Formation");
            stage.setScene(new Scene(root));
            stage.showAndWait();
            initialize();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void handleDelete(Formation formation) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Suppression");
        alert.setHeaderText("Êtes-vous sûr de vouloir supprimer cette formation ?");
        alert.setContentText("Formation : " + formation.getTitre());
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                deleteFormation(formation.getId());
            }
        });
    }


    private void deleteFormation(Long id) {
        try (Connection connection = DatabaseConnection.getConnection()) {
            String sql = "DELETE FROM formation WHERE id=?";
            PreparedStatement p = connection.prepareStatement(sql);
            p.setLong(1, id);
            p.executeUpdate();
            initialize();
        } catch (SQLException e) {
            showAlert("Erreur de base de données", "Impossible de supprimer la formation : " + id);
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

                formations.add(formation);
            }
        } catch (SQLException e) {
            showAlert("Erreur de base de données", "Impossible de charger les formations : " + e.getMessage());
        }

        return formations;
    }
    // Create the session column button
    private Callback<TableColumn<Formation, Void>, TableCell<Formation, Void>> createSessionColumnFactory() {
        return param -> new TableCell<Formation, Void>() {
            private final Button manageButton = new Button("Manage Sessions");

            {
                manageButton.setOnAction(event -> handleManageSessions(getTableView().getItems().get(getIndex())));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(new HBox(10, manageButton));
                }
            }
        };
    }

    // Handle the click event to open the session management window
    private void handleManageSessions(Formation formation) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Fxml/Session/SessionList.fxml"));
            Parent root = loader.load();
            SessionListController sessionListController = loader.getController();
            sessionListController.setFormation(formation); // Pass the selected formation to session controller
            Stage stage = new Stage();
            stage.setTitle("Manage Sessions - " + formation.getTitre());
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
