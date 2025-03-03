package esprit.tn.pidevrh.dashboard;



import esprit.tn.pidevrh.connection.DatabaseConnection;
import esprit.tn.pidevrh.login.SessionManager;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class AdminDashboardController {

    public Label username;
    @FXML private Label totalUsersLabel;
    @FXML private Label totalQuizzesLabel;
    @FXML private Label totalFormationsLabel;
    @FXML private Label averageSuccessLabel;

    @FXML private PieChart quizPassChart;
    @FXML private PieChart difficultyChart;
    @FXML private BarChart<String, Number> barChart;

    @FXML private TableView<UserStat> topUsersTable;
    @FXML private TableColumn<UserStat, String> userColumn;
    @FXML private TableColumn<UserStat, Integer> scoreColumn;



    @FXML private Label totalLeavesLabel;
    @FXML private Label pendingLeavesLabel;
    @FXML private Label approvedLeavesLabel;
    @FXML private Label rejectedLeavesLabel;

    public void initialize() {

        loadGeneralStats();
        loadLeaveStats();
        loadQuizPassChart();
        loadDifficultyChart();
        loadBarChart();
    }

    private void loadGeneralStats() {
        try (Connection connection = DatabaseConnection.getConnection()) {
            ResultSet rs1 = connection.createStatement().executeQuery("SELECT COUNT(*) FROM user");
            if (rs1.next()) totalUsersLabel.setText(String.valueOf(rs1.getInt(1)));

            ResultSet rs2 = connection.createStatement().executeQuery("SELECT COUNT(*) FROM quiz");
            if (rs2.next()) totalQuizzesLabel.setText(String.valueOf(rs2.getInt(1)));

            ResultSet rs3 = connection.createStatement().executeQuery("SELECT COUNT(*) FROM formation");
            if (rs3.next()) totalFormationsLabel.setText(String.valueOf(rs3.getInt(1)));

            ResultSet rs4 = connection.createStatement().executeQuery("SELECT AVG(percentage) FROM resultat");
            if (rs4.next()) averageSuccessLabel.setText(String.format("%.2f%%", rs4.getDouble(1)));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadLeaveStats() {
        try (Connection connection = DatabaseConnection.getConnection()) {
            ResultSet rs = connection.createStatement().executeQuery("SELECT status, COUNT(*) FROM demande_conge GROUP BY status");

            int total = 0, pending = 0, approved = 0, rejected = 0;
            while (rs.next()) {
                switch (rs.getString(1)) {
                    case "PENDING" -> pending = rs.getInt(2);
                    case "APPROVED" -> approved = rs.getInt(2);
                    case "REJECTED" -> rejected = rs.getInt(2);
                }
                total += rs.getInt(2);
            }

            totalLeavesLabel.setText(String.valueOf(total));
            pendingLeavesLabel.setText(String.valueOf(pending));
            approvedLeavesLabel.setText(String.valueOf(approved));
            rejectedLeavesLabel.setText(String.valueOf(rejected));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadQuizPassChart() {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement stmt = connection.prepareStatement("SELECT passer, COUNT(*) FROM quiz GROUP BY passer")) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                String label = rs.getBoolean(1) ? "Passé" : "Non passé";
                int count = rs.getInt(2);
                quizPassChart.getData().add(new PieChart.Data(label, count));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadDifficultyChart() {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement stmt = connection.prepareStatement("SELECT difficultylevel, COUNT(*) FROM question GROUP BY difficultylevel")) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                difficultyChart.getData().add(new PieChart.Data(rs.getString(1), rs.getInt(2)));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadBarChart() {
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Utilisateurs par rôle");

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement stmt = connection.prepareStatement("SELECT role, COUNT(*) FROM user GROUP BY role")) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                series.getData().add(new XYChart.Data<>(rs.getString(1), rs.getInt(2)));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        barChart.getData().add(series);
    }

    private void loadTopUsersTable() {
        userColumn.setCellValueFactory(new PropertyValueFactory<>("user"));
        scoreColumn.setCellValueFactory(new PropertyValueFactory<>("score"));

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement stmt = connection.prepareStatement("SELECT u.first_name, AVG(r.score) AS avg_score FROM user u JOIN resultat r ON u.id = r.id GROUP BY u.id ORDER BY avg_score DESC LIMIT 5")) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                topUsersTable.getItems().add(new UserStat(rs.getString(1), rs.getInt(2)));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
