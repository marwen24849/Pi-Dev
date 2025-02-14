package esprit.tn.pidevrh.teams_departements;

import esprit.tn.pidevrh.connection.DatabaseConnection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DepartmentController {

    @FXML
    private TextField nameField;

    @FXML
    private TextField descriptionField;

    @FXML
    private ListView<Department> departmentListView;

    @FXML
    private Button addButton;

    @FXML
    private Button refreshButton;

    private ObservableList<Department> departmentList = FXCollections.observableArrayList();

    private Connection connection;

    public DepartmentController() {
        this.connection = DatabaseConnection.getConnection();
    }

    @FXML
    public void initialize() {
        // Set the cell factory for the ListView
        //departmentListView.setCellFactory(new DepartmentListCellFactory());
        departmentListView.setCellFactory(param -> new DepartmentListCell());

        departmentListView.setItems(departmentList);

        loadDepartments();

        addButton.setOnAction(event -> addDepartment());
        refreshButton.setOnAction(event -> refreshTable());
    }

    private void loadDepartments() {
        departmentList.clear();
        try (PreparedStatement pstmt = connection.prepareStatement("SELECT * FROM department");
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                String description = rs.getString("description");
                int totalEquipe = calculateTotalEquipe(id);

                departmentList.add(new Department(id, name, description, totalEquipe));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private int calculateTotalEquipe(int departmentId) {
        int total = 0;
        try (PreparedStatement pstmt = connection.prepareStatement("SELECT COUNT(*) FROM equipe WHERE department_id = ?")) {
            pstmt.setInt(1, departmentId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                total = rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return total;
    }

    private void addDepartment() {
        String name = nameField.getText().trim();
        String description = descriptionField.getText().trim();

        // Vérification des contraintes du nom
        if (!name.matches("^[A-Za-z][A-Za-z0-9_]*$")) {
            showAlert("Erreur de saisie", "Le nom du département est invalide !");
            return;
        }

        // Vérification que la description n'est pas vide
        if (description.isEmpty()) {
            showAlert("Erreur de saisie", "La description ne peut pas être vide !");
            return;
        }
        // Vérification des contraintes du nom
        if (!description.matches("^[A-Za-z][A-Za-z0-9_]*$")) {
            showAlert("Erreur de saisie", "La description du département est invalide !");
            return;
        }

        // Insertion en base de données si la saisie est valide
        try (PreparedStatement pstmt = connection.prepareStatement("INSERT INTO department (name, description) VALUES (?, ?)", PreparedStatement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, name);
            pstmt.setString(2, description);
            pstmt.executeUpdate();

            ResultSet rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                int id = rs.getInt(1);
                departmentList.add(new Department(id, name, description, 0));
            }

            // Effacer les champs après ajout
            nameField.clear();
            descriptionField.clear();
        } catch (SQLException e) {
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


    private void refreshTable() {
        loadDepartments();
    }

    // Custom ListCell factory
    public class DepartmentListCellFactory implements Callback<ListView<Department>, ListCell<Department>> {
        @Override
        public ListCell<Department> call(ListView<Department> param) {
            return new DepartmentListCell();
        }
    }

    // Custom ListCell implementation
    private class DepartmentListCell extends ListCell<Department> {
        @Override
        protected void updateItem(Department department, boolean empty) {
            super.updateItem(department, empty);

            if (empty || department == null) {
                setText(null);
                setGraphic(null);
            } else {
                // Create a card layout
                VBox card = new VBox(10);
                card.setPadding(new Insets(10));
                card.setStyle("-fx-background-color: #f4f4f4; -fx-border-color: #ddd; -fx-border-radius: 5px;");

                // Department Name
                Label nameLabel = new Label(department.getName());
                nameLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));

                // Department Description
                Label descriptionLabel = new Label(department.getDescription());
                descriptionLabel.setWrapText(true);

                // Total Equipe
                Label totalEquipeLabel = new Label("Total Équipe: " + department.getTotalEquipe());

                // Buttons
                HBox buttonBox = new HBox(10);
                Button updateButton = new Button("Modifier");
                updateButton.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white;");
                Button deleteButton = new Button("Supprimer");
                deleteButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");

               /* updateButton.setOnAction(event -> {
                    nameField.setText(department.getName());
                    descriptionField.setText(department.getDescription());
                });*/
                updateButton.setOnAction(event -> openEditModal(department));


                /*deleteButton.setOnAction(event -> {
                    departmentList.remove(department);
                    try (PreparedStatement pstmt = connection.prepareStatement("DELETE FROM department WHERE id = ?")) {
                        pstmt.setInt(1, department.getId());
                        pstmt.executeUpdate();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                });*/
                deleteButton.setOnAction(event -> {
                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                    alert.setTitle("Confirmation de suppression");
                    alert.setHeaderText("Supprimer Département");
                    alert.setContentText("Voulez-vous vraiment supprimer le département '" + department.getName() + "' ?");

                    // Customizing buttons
                    ButtonType yesButton = new ButtonType("Oui", ButtonBar.ButtonData.OK_DONE);
                    ButtonType noButton = new ButtonType("Non", ButtonBar.ButtonData.CANCEL_CLOSE);

                    alert.getButtonTypes().setAll(yesButton, noButton);

                    alert.showAndWait().ifPresent(response -> {
                        if (response == yesButton) {
                            try (PreparedStatement pstmt = connection.prepareStatement("DELETE FROM department WHERE id = ?")) {
                                pstmt.setInt(1, department.getId());
                                pstmt.executeUpdate();

                                // Remove from ListView after successful deletion
                                departmentList.remove(department);
                            } catch (SQLException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                });


                buttonBox.getChildren().addAll(updateButton, deleteButton);

                // Add all elements to the card
                card.getChildren().addAll(nameLabel, descriptionLabel, totalEquipeLabel, buttonBox);

                // Set the card as the graphic for the cell
                setGraphic(card);
            }
        }
    }

    private void showDeleteConfirmation(Department department) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation de suppression");
        alert.setHeaderText("Supprimer Département");
        alert.setContentText("Voulez-vous vraiment supprimer le département '" + department.getName() + "' ?");

        // Customizing buttons
        ButtonType yesButton = new ButtonType("Oui", ButtonBar.ButtonData.OK_DONE);
        ButtonType noButton = new ButtonType("Non", ButtonBar.ButtonData.CANCEL_CLOSE);

        alert.getButtonTypes().setAll(yesButton, noButton);

        alert.showAndWait().ifPresent(response -> {
            if (response == yesButton) {
                deleteDepartment(department);
            }
        });
    }

    private void deleteDepartment(Department department) {
        try (PreparedStatement pstmt = connection.prepareStatement("DELETE FROM department WHERE id = ?")) {
            pstmt.setInt(1, department.getId());
            pstmt.executeUpdate();

            // Remove the department from the ListView
            departmentList.remove(department);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    private void openEditModal(Department department) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Fxml/TeamDepartement/DepartmentEdit.fxml"));
            Parent root = loader.load();

            DepartmentEditController controller = loader.getController();
            controller.setDepartment(department, this);

            Stage stage = new Stage();
            stage.setTitle("Modifier Département");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void updateDepartment(Department department) {
        String name = department.getName().trim();
        String description = department.getDescription().trim();

        // Vérification des contraintes du nom
        if (!name.matches("^[A-Za-z][A-Za-z0-9_]*$")) {
            showAlert("Erreur de saisie", "Le nom du département est invalide !\nIl doit commencer par une lettre et ne contenir que des lettres, chiffres et '_'.");
            return;
        }

        // Vérification que la description n'est pas vide
        if (description.isEmpty()) {
            showAlert("Erreur de saisie", "La description ne peut pas être vide !");
            return;
        }

        // Mise à jour en base de données si la saisie est valide
        try (PreparedStatement pstmt = connection.prepareStatement("UPDATE department SET name = ?, description = ? WHERE id = ?")) {
            pstmt.setString(1, name);
            pstmt.setString(2, description);
            pstmt.setInt(3, department.getId());
            pstmt.executeUpdate();

            refreshTable(); // Rafraîchir les données après mise à jour
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }



}