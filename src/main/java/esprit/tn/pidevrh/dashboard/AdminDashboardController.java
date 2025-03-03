package esprit.tn.pidevrh.dashboard;



import esprit.tn.pidevrh.connection.DatabaseConnection;
import esprit.tn.pidevrh.login.SessionManager;
import javafx.fxml.FXML;
import javafx.scene.chart.*;
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
    @FXML private PieChart difficultyChart;
    @FXML private BarChart<String, Number> barChart;
    @FXML private BarChart<String, Number> projectChart;
    @FXML private Label totalLeavesLabel;
    @FXML private Label pendingLeavesLabel;
    @FXML private Label approvedLeavesLabel;
    @FXML private Label rejectedLeavesLabel;

    public void initialize() {
        loadGeneralStats();
        loadLeaveStats();
        loadDifficultyChart();
        loadBarChart();
        loadProjectChart();
    }

    private void loadProjectChart() {
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Projets");

        String query = """
        SELECT 
            SUM(CASE WHEN date_debut > NOW() THEN 1 ELSE 0 END) AS a_venir,
            SUM(CASE WHEN date_debut <= NOW() AND (date_fin IS NULL OR date_fin > NOW()) THEN 1 ELSE 0 END) AS en_cours,
            SUM(CASE WHEN date_fin <= NOW() THEN 1 ELSE 0 END) AS termines
        FROM projet;
    """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement stmt = connection.prepareStatement(query)) {
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                series.getData().add(new XYChart.Data<>("À venir", rs.getInt("a_venir")));
                series.getData().add(new XYChart.Data<>("En cours", rs.getInt("en_cours")));
                series.getData().add(new XYChart.Data<>("Terminés", rs.getInt("termines")));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        projectChart.getData().clear();
        projectChart.getData().add(series);
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

}
