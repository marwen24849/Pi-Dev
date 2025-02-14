package esprit.tn.pidevrh.projet;

import esprit.tn.pidevrh.connection.DatabaseConnection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;



public class ProjetController {

    @FXML private ListView<String> projectListView;


    @FXML private TextField nomProjetField;
    @FXML private ComboBox<String> equipeComboBox;
    @FXML private TextField responsableField;
    @FXML private DatePicker dateDebutPicker;
    @FXML private DatePicker dateFinPicker;
    @FXML private Button addProjectButton;
    @FXML private TableView<Projet> projectTableView;
    @FXML private TableColumn<Projet, String> nomColumn;
    @FXML private TableColumn<Projet, String> equipeColumn;
    @FXML private TableColumn<Projet, String> responsableColumn;
    @FXML private TableColumn<Projet, LocalDateTime> dateDebutColumn;
    @FXML private TableColumn<Projet, LocalDateTime> dateFinColumn;

    private ObservableList<Projet> projetList = FXCollections.observableArrayList();

   /* @FXML
    public void initialize() {
        // Initialize columns
        nomColumn.setCellValueFactory(new PropertyValueFactory<>("nomProjet"));
        equipeColumn.setCellValueFactory(new PropertyValueFactory<>("equipe"));
        responsableColumn.setCellValueFactory(new PropertyValueFactory<>("responsable"));
        dateDebutColumn.setCellValueFactory(new PropertyValueFactory<>("dateDebut"));
        dateFinColumn.setCellValueFactory(new PropertyValueFactory<>("dateFin"));

        // Load data
        loadProjects();
        projectTableView.setItems(projetList);
        loadAvailableTeams();
    }
*/
   @FXML
   public void initialize() {
       // Initialize ListView with project data
       loadProjects();
       loadAvailableTeams();


       // Use a cell factory to customize the list view
       projectListView.setCellFactory(param -> new ListCell<String>() {
           @Override
           protected void updateItem(String item, boolean empty) {
               super.updateItem(item, empty);
               if (empty || item == null) {
                   setText(null);
               } else {
                   setText(item);
                   setStyle("-fx-font-size: 16px; -fx-text-fill: #2c3e50; -fx-padding: 10px; -fx-background-color: #f9fafc;");
               }
           }
       });
   }


   /* @FXML
    public void handleAddProject() {
        String nomProjet = nomProjetField.getText();
        String equipeName = equipeComboBox.getValue();
        String responsable = responsableField.getText();
        LocalDate dateDebut = dateDebutPicker.getValue();
        LocalDate dateFin = dateFinPicker.getValue();

        if (!validateInputs(nomProjet, equipeName, responsable, dateDebut, dateFin)) {
            return;
        }

        try (Connection connection = DatabaseConnection.getConnection()) {
            // Fetch the team ID based on the team name selected
            int equipeId = getTeamId(equipeName);

            String insertSQL = "INSERT INTO projet (nom_projet, equipe, responsable, date_debut, date_fin) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement preparedStatement = connection.prepareStatement(insertSQL);
            preparedStatement.setString(1, nomProjet);
            preparedStatement.setInt(2, equipeId);  // Save the team ID, not the name
            preparedStatement.setString(3, responsable);
            preparedStatement.setTimestamp(4, Timestamp.valueOf(dateDebut.atStartOfDay()));
            preparedStatement.setTimestamp(5, Timestamp.valueOf(dateFin.atStartOfDay()));
            preparedStatement.executeUpdate();

            projetList.add(new Projet(0, nomProjet, equipeName, responsable, dateDebut.atStartOfDay(), dateFin.atStartOfDay()));

            clearFields();
            loadAvailableTeams();
            showAlert("Succès", "Projet ajouté avec succès!", Alert.AlertType.INFORMATION);
        } catch (SQLException e) {
            showAlert("Erreur", "Erreur lors de l'ajout du projet: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }*/

    @FXML
    public void handleAddProject() {
        String nomProjet = nomProjetField.getText();
        String equipeName = equipeComboBox.getValue();
        String responsable = responsableField.getText();
        LocalDate dateDebut = dateDebutPicker.getValue();
        LocalDate dateFin = dateFinPicker.getValue();

        if (!validateInputs(nomProjet, equipeName, responsable, dateDebut, dateFin)) {
            return;
        }

        try (Connection connection = DatabaseConnection.getConnection()) {
            // Fetch the team ID based on the team name selected
            int equipeId = getTeamId(equipeName);

            String insertSQL = "INSERT INTO projet (nom_projet, equipe, responsable, date_debut, date_fin) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement preparedStatement = connection.prepareStatement(insertSQL);
            preparedStatement.setString(1, nomProjet);
            preparedStatement.setInt(2, equipeId);  // Save the team ID, not the name
            preparedStatement.setString(3, responsable);
            preparedStatement.setTimestamp(4, Timestamp.valueOf(dateDebut.atStartOfDay()));
            preparedStatement.setTimestamp(5, Timestamp.valueOf(dateFin.atStartOfDay()));
            preparedStatement.executeUpdate();

            // Add the new project to the ListView
            String projectSummary = nomProjet + " - " + equipeName + " - " + responsable + " - " + dateDebut + " to " + dateFin;
            projectListView.getItems().add(projectSummary);

            clearFields();
            loadAvailableTeams();
            showAlert("Succès", "Projet ajouté avec succès!", Alert.AlertType.INFORMATION);
        } catch (SQLException e) {
            showAlert("Erreur", "Erreur lors de l'ajout du projet: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }


    private int getTeamId(String teamName) {
        try (Connection connection = DatabaseConnection.getConnection()) {
            String query = "SELECT id FROM equipe WHERE name = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, teamName);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                return resultSet.getInt("id");
            }
        } catch (SQLException e) {
            showAlert("Erreur", "Erreur lors de la récupération de l'ID de l'équipe: " + e.getMessage(), Alert.AlertType.ERROR);
        }
        return -1;  // If no matching team found, return -1
    }


    private boolean validateInputs(String nomProjet, String equipe, String responsable, LocalDate dateDebut, LocalDate dateFin) {
        if (nomProjet.isEmpty() || equipe == null || responsable.isEmpty() || dateDebut == null || dateFin == null) {
            showAlert("Erreur", "Veuillez remplir tous les champs", Alert.AlertType.ERROR);
            return false;
        }

        // Check that the project name is valid (it should start with a letter and can include letters, numbers, and underscores)
        if (!nomProjet.matches("^[A-Za-z][A-Za-z0-9_]*$")) {
            showAlert("Erreur", "Le nom du projet doit commencer par une lettre et ne contenir que des lettres, chiffres et underscores.", Alert.AlertType.ERROR);
            return false;
        }

        // Ensure that the start date (dateDebut) is today or any day after today
        if (dateDebut.isBefore(LocalDate.now())) {
            showAlert("Erreur", "La date de début ne peut pas être avant aujourd'hui.", Alert.AlertType.ERROR);
            return false;
        }

        // Ensure that the end date (dateFin) is after the start date (dateDebut)
        if (dateFin.isBefore(dateDebut)) {
            showAlert("Erreur", "La date de fin ne peut pas être avant la date de début.", Alert.AlertType.ERROR);
            return false;
        }

        // Ensure that the end date (dateFin) is not the same as the start date (dateDebut)
        if (dateFin.isEqual(dateDebut)) {
            showAlert("Erreur", "La date de fin ne peut pas être égale à la date de début.", Alert.AlertType.ERROR);
            return false;
        }

        return true;
    }

    private void clearFields() {
        nomProjetField.clear();
        equipeComboBox.getSelectionModel().clearSelection();
        responsableField.clear();
        dateDebutPicker.setValue(null);
        dateFinPicker.setValue(null);
    }

    private void loadAvailableTeams() {
        try (Connection connection = DatabaseConnection.getConnection()) {
            // Fetch the team names (no need to use the 'members' field)
            String fetchSQL = "SELECT name FROM equipe WHERE id NOT IN (SELECT equipe FROM projet)";
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(fetchSQL);

            List<String> availableTeams = new ArrayList<>();
            while (resultSet.next()) {
                availableTeams.add(resultSet.getString("name"));
            }

            equipeComboBox.setItems(FXCollections.observableArrayList(availableTeams));
        } catch (SQLException e) {
            showAlert("Erreur", "Erreur lors du chargement des équipes disponibles: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }



   /* private void loadProjects() {
        projetList.clear();
        try (Connection connection = DatabaseConnection.getConnection()) {
            String fetchSQL = "SELECT * FROM projet";
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(fetchSQL);

            while (resultSet.next()) {
                Projet projet = new Projet(
                        resultSet.getInt("id"),
                        resultSet.getString("nom_projet"),
                        resultSet.getString("equipe"),
                        resultSet.getString("responsable"),
                        resultSet.getTimestamp("date_debut").toLocalDateTime(),
                        resultSet.getTimestamp("date_fin").toLocalDateTime()
                );
                projetList.add(projet);
            }
        } catch (SQLException e) {
            showAlert("Erreur", "Erreur lors du chargement des projets: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }*/
   private void loadProjects() {
       List<String> projectDetails = new ArrayList<>();
       try (Connection connection = DatabaseConnection.getConnection()) {
           String fetchSQL = "SELECT * FROM projet";
           Statement statement = connection.createStatement();
           ResultSet resultSet = statement.executeQuery(fetchSQL);

           while (resultSet.next()) {
               String projetInfo = resultSet.getString("nom_projet") + " - " +
                       resultSet.getString("equipe") + " - " +
                       resultSet.getString("responsable") + " - " +
                       resultSet.getTimestamp("date_debut").toLocalDateTime().toLocalDate() + " to " +
                       resultSet.getTimestamp("date_fin").toLocalDateTime().toLocalDate();
               projectDetails.add(projetInfo);
           }
       } catch (SQLException e) {
           showAlert("Erreur", "Erreur lors du chargement des projets: " + e.getMessage(), Alert.AlertType.ERROR);
       }
       projectListView.setItems(FXCollections.observableArrayList(projectDetails));
   }

    private void showAlert(String title, String message, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
