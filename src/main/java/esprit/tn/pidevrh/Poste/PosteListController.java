    package esprit.tn.pidevrh.Poste;

    import javafx.collections.FXCollections;
    import javafx.collections.ObservableList;
    import javafx.fxml.FXML;
    import javafx.fxml.FXMLLoader;
    import javafx.scene.Node;
    import javafx.scene.Scene;
    import javafx.scene.control.*;
    import javafx.scene.control.cell.PropertyValueFactory;
    import javafx.scene.layout.AnchorPane;
    import javafx.stage.Modality;
    import javafx.stage.Stage;

    import java.text.SimpleDateFormat;
    import java.util.List;

    public class PosteListController {


        @FXML
        private ComboBox<String> stateFilter;
        @FXML
        private Button filterButton;
        @FXML
        private TableView<Poste> posteTable;
        @FXML
        private TableColumn<Poste, Integer> idColumn;
        @FXML
        private TableColumn<Poste, String> contentColumn;
        @FXML
        private TableColumn<Poste, Double> salaireColumn;
        @FXML
        private TableColumn<Poste, String> descriptionColumn;
        @FXML
        private TableColumn<Poste, String> dateColumn;
        @FXML
        private TableColumn<Poste, String> stateColumn;
        @FXML
        private Button editButton;
        @FXML
        private Pagination pagination;

        private static final int ROWS_PER_PAGE = 10;

        private final PosteService posteService = new PosteService();
        private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        @FXML
        public void initialize() {
            idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
            contentColumn.setCellValueFactory(new PropertyValueFactory<>("content"));
            salaireColumn.setCellValueFactory(new PropertyValueFactory<>("salaire"));
            descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
            dateColumn.setCellValueFactory(cellData -> {
                String formattedDate = dateFormat.format(cellData.getValue().getDatePoste());
                return new javafx.beans.property.SimpleStringProperty(formattedDate);
            });
            stateColumn.setCellValueFactory(new PropertyValueFactory<>("state"));

            if (editButton != null) {
                editButton.setOnAction(event -> handleUpdatePoste());
            }

            // Populate the filter combo box
            stateFilter.setItems(FXCollections.observableArrayList("Active", "Not Active", "Pending", "All"));
            stateFilter.setValue("All"); // Default selection

            // Set the filter button event
            filterButton.setOnAction(event -> handleFilterAction());


            loadPostes();
        }


        @FXML
        private void handleFilterAction() {
            String selectedState = stateFilter.getValue();

            try {
                List<Poste> postes;
                if ("All".equals(selectedState)) {
                    postes = posteService.getAllPostes();
                } else {
                    postes = posteService.getPostesByState(selectedState);
                }

                ObservableList<Poste> filteredPostes = FXCollections.observableArrayList(postes);
                posteTable.setItems(filteredPostes);
            } catch (Exception e) {
                showAlert("Error", "Failed to apply filter.", Alert.AlertType.ERROR);
            }
        }

        private void loadPostes() {
            try {
                List<Poste> postes = posteService.getAllPostes();
                ObservableList<Poste> posteList = FXCollections.observableArrayList(postes);
                posteTable.setItems(posteList);
            } catch (Exception e) {
                showAlert("Error", "Failed to load posts.", Alert.AlertType.ERROR);
            }
        }

        @FXML
        private void handleUpdatePoste() {
            Poste selectedPoste = posteTable.getSelectionModel().getSelectedItem();
            if (selectedPoste != null) {
                openUpdateDialog(selectedPoste);
            } else {
                showAlert("Error", "Please select a poste to edit.", Alert.AlertType.ERROR);
            }
        }

        private void openUpdateDialog(Poste poste) {
            try {

                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Poste/PosteUpdate.fxml"));
                AnchorPane dialogPane = loader.load();

                PosteUpdateController controller = loader.getController();
                controller.setPoste(poste);

                Stage dialogStage = new Stage();
                dialogStage.setTitle("Modifier un Poste");
                dialogStage.initModality(Modality.APPLICATION_MODAL);
                dialogStage.setScene(new Scene(dialogPane));
                dialogStage.showAndWait();

                loadPostes(); // Refresh after update
            } catch (Exception e) {
                e.printStackTrace();
                showAlert("Error", "Failed to open update dialog.", Alert.AlertType.ERROR);
            }
        }


        @FXML
        private void handleDeletePoste() {
            Poste selectedPoste = posteTable.getSelectionModel().getSelectedItem();
            if (selectedPoste != null) {
                Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
                confirmation.setTitle("Confirm Deletion");
                confirmation.setHeaderText(null);
                confirmation.setContentText("Are you sure you want to delete this post?");

                confirmation.showAndWait().ifPresent(response -> {
                    if (response == ButtonType.OK) {
                        posteService.deletePoste(selectedPoste.getId()); // Delete from DB
                        loadPostes(); // Refresh list
                        showAlert("Success", "Poste deleted successfully.", Alert.AlertType.INFORMATION);
                    }
                });
            } else {
                showAlert("Error", "Please select a poste to delete.", Alert.AlertType.WARNING);
            }
        }

        private void showAlert(String title, String message, Alert.AlertType type) {
            Alert alert = new Alert(type);
            alert.setTitle(title);
            alert.setContentText(message);
            alert.show();
        }
    }