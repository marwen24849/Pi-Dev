package esprit.tn.pidevrh.projet;

import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import esprit.tn.pidevrh.connection.DatabaseConnection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;


import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import esprit.tn.pidevrh.google.GoogleCalendarService;
import com.google.api.services.calendar.Calendar;


public class ProjetController {

    @FXML private ListView<String> projectListView;


    @FXML private TextField nomProjetField;
    @FXML private ComboBox<String> equipeComboBox;
    @FXML private TextField responsableField;
    @FXML private DatePicker dateDebutPicker;
    @FXML private DatePicker dateFinPicker;
    @FXML private Button addProjectButton;
    @FXML private Button addToCalendarButton;  // New button
    private GoogleCalendarService calendarService;


    @FXML
    private Button deleteProjectButton;
    @FXML
    private Button updateProjectButton;

    @FXML private ComboBox<String> monthComboBox;
    @FXML private ComboBox<String> yearComboBox;
    @FXML private CheckBox statusCheckBox;

    //@FXML private TableView<Projet> projectTableView;
   // @FXML private TableColumn<Projet, String> nomColumn;
   // @FXML private TableColumn<Projet, String> equipeColumn;
   // @FXML private TableColumn<Projet, String> responsableColumn;
   // @FXML private TableColumn<Projet, LocalDateTime> dateDebutColumn;
   // @FXML private TableColumn<Projet, LocalDateTime> dateFinColumn;

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
       // Initialize month and year ComboBoxes
       monthComboBox.setItems(FXCollections.observableArrayList(
               "JANUARY", "FEBRUARY", "MARCH", "APRIL", "MAY", "JUNE",
               "JULY", "AUGUST", "SEPTEMBER", "OCTOBER", "NOVEMBER", "DECEMBER"
       ));
       yearComboBox.setItems(FXCollections.observableArrayList(
               "2023", "2024", "2025", "2026", "2027"
       ));

       // Load projects and available teams
       loadProjects();
       loadAvailableTeams();

       // Add listeners to filters
       monthComboBox.setOnAction(event -> loadProjects());
       yearComboBox.setOnAction(event -> loadProjects());
       statusCheckBox.setOnAction(event -> loadProjects());

       // Use a cell factory to customize the ListView with buttons for each item
       projectListView.setCellFactory(param -> new ListCell<String>() {
           private final VBox cardContainer = new VBox(10);
           private final Label projectNameLabel = new Label();
           private final Label teamIdLabel = new Label();
           private final Label projectManagerLabel = new Label();
           private final Label dateLabel = new Label();
           private final Button deleteButton = new Button("Delete");
           private final Button updateButton = new Button("Update");
           private final Button addToCalendarButton = new Button("Add to Calendar");
           private final HBox buttonContainer = new HBox(10);

           {
               // Styling for the card
               cardContainer.setStyle("-fx-background-color: #f9fafc; -fx-border-color: #dfe4ea; -fx-border-radius: 5px; -fx-padding: 10px;");
               cardContainer.setPrefHeight(120);

               // Styling for labels
               projectNameLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #2c3e50;");
               teamIdLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #5d6d7e;");
               projectManagerLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #5d6d7e;");
               dateLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #5d6d7e;");

               // Styling for buttons
               deleteButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-border-radius: 5px;");
               updateButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-border-radius: 5px;");
               addToCalendarButton.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white; -fx-border-radius: 5px;");
               buttonContainer.setAlignment(Pos.CENTER);
               buttonContainer.getChildren().addAll(updateButton, deleteButton, addToCalendarButton);

               // Add all components to the card
               cardContainer.getChildren().addAll(
                       projectNameLabel,
                       teamIdLabel,
                       projectManagerLabel,
                       dateLabel,
                       buttonContainer
               );
           }

           @Override
           protected void updateItem(String item, boolean empty) {
               super.updateItem(item, empty);

               if (empty || item == null) {
                   setGraphic(null);
               } else {
                   // Parse the project details from the string
                   String[] parts = item.split(" - ");
                   if (parts.length >= 4) {
                       String projectName = parts[0];
                       String teamName = parts[1];
                       String projectManager = parts[2];
                       String dates = parts[3];

                       // Set the text for each label
                       projectNameLabel.setText("Project Name: " + projectName);
                       teamIdLabel.setText("Team ID: " + teamName);
                       projectManagerLabel.setText("Project Manager: " + projectManager);
                       dateLabel.setText("Dates: " + dates);

                       // Add action to delete button
                       deleteButton.setOnAction(event -> handleDeleteProject(projectName));

                       // Add action to update button
                       updateButton.setOnAction(event -> handleUpdateProject(item));

                       // Add action to add to calendar button
                       addToCalendarButton.setOnAction(event -> handleAddToCalendar(projectName, projectManager, dates));

                       // Set the card as the graphic for the list cell
                       setGraphic(cardContainer);
                   }
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
            int equipeId = getTeamId(equipeName);

            String insertSQL = "INSERT INTO projet (nom_projet, equipe, responsable, date_debut, date_fin) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement preparedStatement = connection.prepareStatement(insertSQL);
            preparedStatement.setString(1, nomProjet);
            preparedStatement.setInt(2, equipeId);
            preparedStatement.setString(3, responsable);
            preparedStatement.setTimestamp(4, Timestamp.valueOf(dateDebut.atStartOfDay()));
            preparedStatement.setTimestamp(5, Timestamp.valueOf(dateFin.atStartOfDay()));
            preparedStatement.executeUpdate();

            String projectSummary = nomProjet + " - " + equipeName + " - " + responsable + " - " + dateDebut + " to " + dateFin;
            projectListView.getItems().add(projectSummary);

            clearFields();
            loadAvailableTeams();
            showAlert("Succès", "Projet ajouté avec succès!", Alert.AlertType.INFORMATION);
        } catch (SQLException e) {
            showAlert("Erreur", "Erreur lors de l'ajout du projet: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void handleAddToCalendar(String projectName, String projectManager, String dates) {
        String[] dateParts = dates.split(" to ");
        LocalDateTime startDate = LocalDate.parse(dateParts[0]).atStartOfDay();
        LocalDateTime endDate = LocalDate.parse(dateParts[1]).atStartOfDay();

        try {
            GoogleCalendarService.addEvent(projectName, "Managed by: " + projectManager, startDate, endDate);
            showAlert("Succès", "Projet ajouté au calendrier Google avec succès!", Alert.AlertType.INFORMATION);
        } catch (GeneralSecurityException | IOException e) {
            showAlert("Erreur", "Erreur lors de l'ajout du projet au calendrier Google: " + e.getMessage(), Alert.AlertType.ERROR);
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
  /* private void loadProjects() {
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
   }*/


    @FXML
    public void handleResetFilters() {
        // Clear the selected filters
        monthComboBox.getSelectionModel().clearSelection();
        yearComboBox.getSelectionModel().clearSelection();
        statusCheckBox.setSelected(false);

        // Reload all projects without filters
        loadProjects();
    }

    private void loadProjects() {
        List<String> projectDetails = new ArrayList<>();
        String selectedMonth = monthComboBox.getValue();
        String selectedYear = yearComboBox.getValue();
        boolean showOnlyOngoing = statusCheckBox.isSelected();

        try (Connection connection = DatabaseConnection.getConnection()) {
            String fetchSQL = "SELECT * FROM projet";
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(fetchSQL);

            while (resultSet.next()) {
                LocalDate startDate = resultSet.getTimestamp("date_debut").toLocalDateTime().toLocalDate();
                LocalDate endDate = resultSet.getTimestamp("date_fin").toLocalDateTime().toLocalDate();

                // Apply filters only if a filter is selected
                boolean matchesMonth = selectedMonth == null || startDate.getMonth().toString().equalsIgnoreCase(selectedMonth);
                boolean matchesYear = selectedYear == null || String.valueOf(startDate.getYear()).equals(selectedYear);
                boolean isOngoing = !showOnlyOngoing || endDate.isAfter(LocalDate.now());

                if (matchesMonth && matchesYear && isOngoing) {
                    String projetInfo = resultSet.getString("nom_projet") + " - " +
                            resultSet.getString("equipe") + " - " +
                            resultSet.getString("responsable") + " - " +
                            startDate + " to " + endDate;
                    projectDetails.add(projetInfo);
                }
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


    private void handleDeleteProject(String projectName) {
        try (Connection connection = DatabaseConnection.getConnection()) {
            String deleteSQL = "DELETE FROM projet WHERE nom_projet = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(deleteSQL);
            preparedStatement.setString(1, projectName);
            preparedStatement.executeUpdate();

            // Reload the projects in the ListView
            loadProjects();
            showAlert("Succès", "Projet supprimé avec succès!", Alert.AlertType.INFORMATION);
        } catch (SQLException e) {
            showAlert("Erreur", "Erreur lors de la suppression du projet: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }



    private void handleUpdateProject(String selectedProject) {
        String[] parts = selectedProject.split(" - ");
        if (parts.length >= 4) {
            String projectName = parts[0];
            String teamName = parts[1];
            String projectManager = parts[2];
            String dates = parts[3];

            // Split the dates into start and end
            String[] dateParts = dates.split(" to ");
            String startDate = dateParts[0];
            String endDate = dateParts[1];
            System.out.println(parts);

            // Debugging: Check if the FXML file is found
            //URL url = getClass().getResource("/Fxml/Projet/edit_projet_modal.fxml");
            URL url = getClass().getResource("/Fxml/Projet/edit_project_modal.fxml");
            /*if (url == null) {
                System.err.println("FXML file not found!");
                return;
            } else {
                System.out.println("FXML file found at: " + url);
            }*/

            // Open the edit modal with the project details
            try {
                FXMLLoader loader = new FXMLLoader(url);
                Parent root = loader.load();

                // Pass the project details to the EditProjectController
                EditProjectController controller = loader.getController();
                controller.initialize(projectName, teamName, projectManager, startDate, endDate);

                // Show the modal dialog
                Stage stage = new Stage();
                stage.initModality(Modality.APPLICATION_MODAL);
                stage.setTitle("Modifier le Projet");
                stage.setScene(new Scene(root));
                stage.showAndWait();

                // Reload the projects after the modal is closed
                loadProjects();
            } catch (IOException e) {
                showAlert("Erreur", "Erreur lors de l'ouverture de la fenêtre d'édition: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }

}
