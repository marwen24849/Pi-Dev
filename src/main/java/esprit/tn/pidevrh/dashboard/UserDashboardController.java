package esprit.tn.pidevrh.dashboard;

import esprit.tn.pidevrh.connection.DatabaseConnection;
import esprit.tn.pidevrh.login.SessionManager;
import javafx.fxml.FXML;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Label;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserDashboardController {

    @FXML
    private Label username;

    @FXML
    private Label totalQuizzesTakenLabel;

    @FXML
    private Label successRateLabel;

    @FXML
    private Label totalFormationsLabel;

    @FXML
    private Label totalLeavesLabel;

    @FXML
    private Label pendingLeavesLabel;

    @FXML
    private Label approvedLeavesLabel;

    @FXML
    private Label rejectedLeavesLabel;

    @FXML
    private PieChart quizPassChart;

    @FXML
    private PieChart difficultyChart;

    private long userId;

    public void initialize() {
        this.userId= SessionManager.getInstance().getUser().getId();
        loadUserData();
        loadCharts();
    }

    private void loadUserData() {
        try (Connection connection = DatabaseConnection.getConnection()) {

            String userQuery = "SELECT first_name, last_name FROM user WHERE id = ?";
            PreparedStatement userStmt = connection.prepareStatement(userQuery);
            userStmt.setLong(1, userId);
            ResultSet userRs = userStmt.executeQuery();
            if (userRs.next()) {
                username.setText(userRs.getString("first_name") + " " + userRs.getString("last_name"));
            }

            // Load quiz data
            String quizQuery = "SELECT \n" +
                    "    COUNT(r.id) AS total, \n" +
                    "    AVG(r.percentage) AS avg \n" +
                    "FROM resultat r\n" +
                    "JOIN response res ON r.id = res.resultat_id\n" +
                    "JOIN user_quiz uq ON res.user_id = uq.user_id\n" +
                    "WHERE uq.user_id = ?";
            PreparedStatement quizStmt = connection.prepareStatement(quizQuery);
            quizStmt.setLong(1, userId);
            ResultSet quizRs = quizStmt.executeQuery();
            if (quizRs.next()) {
                totalQuizzesTakenLabel.setText(String.valueOf(quizRs.getInt("total")));
                successRateLabel.setText(String.format("%.2f%%", quizRs.getDouble("avg")));
            }

            // Load formation data
            String formationQuery = "SELECT COUNT(*) AS total FROM formation_user WHERE user_id = ?";
            PreparedStatement formationStmt = connection.prepareStatement(formationQuery);
            formationStmt.setLong(1, userId);
            ResultSet formationRs = formationStmt.executeQuery();
            if (formationRs.next()) {
                totalFormationsLabel.setText(String.valueOf(formationRs.getInt("total")));
            }

            // Load leave data
            String leaveQuery = "SELECT status, COUNT(*) AS total FROM demande_conge WHERE user_id = ? GROUP BY status";
            PreparedStatement leaveStmt = connection.prepareStatement(leaveQuery);
            leaveStmt.setLong(1, userId);
            ResultSet leaveRs = leaveStmt.executeQuery();
            while (leaveRs.next()) {
                switch (leaveRs.getString("status")) {
                    case "PENDING":
                        pendingLeavesLabel.setText(String.valueOf(leaveRs.getInt("total")));
                        break;
                    case "APPROVED":
                        approvedLeavesLabel.setText(String.valueOf(leaveRs.getInt("total")));
                        break;
                    case "REJECTED":
                        rejectedLeavesLabel.setText(String.valueOf(leaveRs.getInt("total")));
                        break;
                }
                totalLeavesLabel.setText(String.valueOf(
                        Integer.parseInt(pendingLeavesLabel.getText()) +
                                Integer.parseInt(approvedLeavesLabel.getText()) +
                                Integer.parseInt(rejectedLeavesLabel.getText())
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadCharts() {
        // Load quiz pass chart data
        quizPassChart.getData().add(new PieChart.Data("Passed", 70));
        quizPassChart.getData().add(new PieChart.Data("Failed", 30));

        // Load difficulty chart data
        difficultyChart.getData().add(new PieChart.Data("Easy", 50));
        difficultyChart.getData().add(new PieChart.Data("Medium", 30));
        difficultyChart.getData().add(new PieChart.Data("Hard", 20));
    }


}