package esprit.tn.pidevrh.teams_departements;


import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class DepartmentEditController {

    @FXML
    private TextField editNameField;

    @FXML
    private TextField editDescriptionField;

    @FXML
    private Button saveButton;

    @FXML
    private Button cancelButton;

    private Department department;
    private DepartmentController parentController;

    public void setDepartment(Department department, DepartmentController parentController) {
        this.department = department;
        this.parentController = parentController;

        editNameField.setText(department.getName());
        editDescriptionField.setText(department.getDescription());
    }

    @FXML
    public void initialize() {
        saveButton.setOnAction(event -> saveChanges());
        cancelButton.setOnAction(event -> closeModal());
    }

    private void saveChanges() {
        department.setName(editNameField.getText());
        department.setDescription(editDescriptionField.getText());

        parentController.updateDepartment(department);
        closeModal();
    }

    private void closeModal() {
        Stage stage = (Stage) editNameField.getScene().getWindow();
        stage.close();
    }

}
