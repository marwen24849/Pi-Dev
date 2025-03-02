package esprit.tn.pidevrh.dashboard;


import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class UserDashboardController {

    @FXML
    private Label welcomeLabel;

    @FXML
    private Label dateTimeLabel;

    @FXML
    private ListView<String> formationListView;

    @FXML
    private TableView<String> leaveStatusTable;

    @FXML
    private TableColumn<String, String> statusColumn;

    @FXML
    private TableColumn<String, String> startDateColumn;

    @FXML
    private TableColumn<String, String> endDateColumn;

    @FXML
    private ListView<String> quizListView;

    @FXML
    private ListView<String> newsListView;

    @FXML
    private ImageView progressChart;

    @FXML
    private ImageView leaveChart;

    @FXML
    private TextArea chatArea;

    @FXML
    private ListView<String> teamMembersListView;

    public void initialize() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        dateTimeLabel.setText(LocalDateTime.now().format(formatter));


        welcomeLabel.setText("Bienvenue, John Doe");
        formationListView.getItems().addAll("Formation Java", "Formation SQL");
        quizListView.getItems().addAll("Quiz Java - Score: 80%", "Quiz SQL - Score: 90%");
        newsListView.getItems().addAll("Nouvelle formation disponible : Python", "Événement RH le 15/10");
        teamMembersListView.getItems().addAll("Alice", "Bob", "Charlie");

        // Charger des images pour les graphiques
        progressChart.setImage(new Image(getClass().getResourceAsStream("progress_chart.png")));
        leaveChart.setImage(new Image(getClass().getResourceAsStream("leave_chart.png")));
    }
}