package esprit.tn.pidevrh.projet;

import esprit.tn.pidevrh.connection.DatabaseConnection;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class EditProjectController {

    @FXML
    private TextField nomProjetField;
    @FXML
    private ComboBox<String> equipeComboBox;
    @FXML
    private TextField responsableField;
    @FXML
    private DatePicker dateDebutPicker;
    @FXML
    private DatePicker dateFinPicker;
    @FXML
    private Button saveButton;
    @FXML
    private Button cancelButton;

    private String originalProjectName;  // To store the project name for update

    public void initialize(String projectName, String equipe, String responsable, String startDate, String endDate) {
        originalProjectName = projectName;

        // Pré-remplir les champs avec les données existantes du projet
        nomProjetField.setText(projectName);
        responsableField.setText(responsable);

        // Convertir les dates en LocalDate
        LocalDate initialStartDate = LocalDate.parse(startDate);
        LocalDate initialEndDate = LocalDate.parse(endDate);

        // Définir les dates dans les DatePicker
        dateDebutPicker.setValue(initialStartDate);
        dateFinPicker.setValue(initialEndDate);

        // Charger les équipes disponibles
        loadAvailableTeams();

        // Définir l'équipe actuelle comme valeur sélectionnée dans la ComboBox
        equipeComboBox.setValue(equipe);

        // Vérifier si la date de début initiale est dépassée
        if (initialStartDate.isBefore(LocalDate.now())) {
            // Désactiver le DatePicker de la date de début
            dateDebutPicker.setDisable(true);

            // Afficher un message d'information
            showAlert("Information", "La date de début ne peut pas être modifiée car elle est déjà dépassée.", Alert.AlertType.INFORMATION);
        }
    }

   /* @FXML
    private void handleSaveChanges() {
        // Get updated values
        String newNomProjet = nomProjetField.getText();
        String newEquipe = equipeComboBox.getValue();
        String newResponsable = responsableField.getText();
        String newStartDate = dateDebutPicker.getValue().toString();
        String newEndDate = dateFinPicker.getValue().toString();

        // Call method to update project in database (use originalProjectName for identification)
        updateProjectInDatabase(originalProjectName, newNomProjet, newEquipe, newResponsable, newStartDate, newEndDate);

        // Close the modal
        Stage stage = (Stage) saveButton.getScene().getWindow();
        stage.close();
    }*/

    @FXML
    private void handleSaveChanges() {
        // Récupérer les valeurs des champs
        String newNomProjet = nomProjetField.getText();
        String newEquipe = equipeComboBox.getValue();
        String newResponsable = responsableField.getText();
        LocalDate newStartDate = dateDebutPicker.getValue();
        LocalDate newEndDate = dateFinPicker.getValue();

        // Valider les saisies
        if (!validateInputs(newNomProjet, newEquipe, newResponsable, newStartDate, newEndDate)) {
            return; // Arrêter si les saisies sont invalides
        }

        // Mettre à jour le projet dans la base de données
        updateProjectInDatabase(originalProjectName, newNomProjet, newEquipe, newResponsable, newStartDate.toString(), newEndDate.toString());

        // Fermer le modal
        Stage stage = (Stage) saveButton.getScene().getWindow();
        stage.close();
    }

    private boolean validateInputs(String nomProjet, String equipe, String responsable, LocalDate dateDebut, LocalDate dateFin) {
        // Vérifier que tous les champs obligatoires sont remplis
        if (nomProjet.isEmpty() || equipe == null || responsable.isEmpty() || dateDebut == null || dateFin == null) {
            showAlert("Erreur", "Veuillez remplir tous les champs.", Alert.AlertType.ERROR);
            return false;
        }

        // Vérifier que le nom du projet est valide
        if (!nomProjet.matches("^[A-Za-z][A-Za-z0-9_]*$")) {
            showAlert("Erreur", "Le nom du projet doit commencer par une lettre et ne contenir que des lettres, chiffres et underscores.", Alert.AlertType.ERROR);
            return false;
        }

        // Vérifier que la date de début n'est pas antérieure à aujourd'hui
        if (dateDebut.isBefore(LocalDate.now())) {
            showAlert("Erreur", "La date de début ne peut pas être avant aujourd'hui.", Alert.AlertType.ERROR);
            return false;
        }

        // Vérifier que la date de fin est postérieure à la date de début
        if (dateFin.isBefore(dateDebut)) {
            showAlert("Erreur", "La date de fin ne peut pas être avant la date de début.", Alert.AlertType.ERROR);
            return false;
        }

        // Vérifier que la date de fin n'est pas égale à la date de début
        if (dateFin.isEqual(dateDebut)) {
            showAlert("Erreur", "La date de fin ne peut pas être égale à la date de début.", Alert.AlertType.ERROR);
            return false;
        }

        return true; // Toutes les validations sont passées
    }

    @FXML
    private void handleCancel() {
        // Close the modal without saving changes
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }

    private void updateProjectInDatabase(String oldProjectName, String newProjectName, String equipe, String responsable, String startDate, String endDate) {
        // Use the original project name to identify the record and update it in the database
        try (Connection connection = DatabaseConnection.getConnection()) {
            String updateSQL = "UPDATE projet SET nom_projet = ?, equipe = ?, responsable = ?, date_debut = ?, date_fin = ? WHERE nom_projet = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(updateSQL);
            preparedStatement.setString(1, newProjectName);
            preparedStatement.setString(2, equipe);
            preparedStatement.setString(3, responsable);
            preparedStatement.setString(4, startDate);
            preparedStatement.setString(5, endDate);
            preparedStatement.setString(6, oldProjectName);
            preparedStatement.executeUpdate();

            showAlert("Succès", "Projet mis à jour avec succès!", Alert.AlertType.INFORMATION);
        } catch (SQLException e) {
            showAlert("Erreur", "Erreur lors de la mise à jour du projet: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void loadAvailableTeams() {
        try (Connection connection = DatabaseConnection.getConnection()) {
            // Fetch the team names that are not assigned to any project
            String fetchSQL = "SELECT name FROM equipe WHERE id NOT IN (SELECT equipe FROM projet)";
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(fetchSQL);

            List<String> availableTeams = new ArrayList<>();
            while (resultSet.next()) {
                availableTeams.add(resultSet.getString("name"));
            }

            // Set the available teams in the ComboBox
            equipeComboBox.setItems(FXCollections.observableArrayList(availableTeams));
        } catch (SQLException e) {
            showAlert("Erreur", "Erreur lors du chargement des équipes disponibles: " + e.getMessage(), Alert.AlertType.ERROR);
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