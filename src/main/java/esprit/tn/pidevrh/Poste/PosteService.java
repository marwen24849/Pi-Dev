package esprit.tn.pidevrh.Poste;

import esprit.tn.pidevrh.connection.DatabaseConnection;
import esprit.tn.pidevrh.login.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PosteService {

    private final Connection connection = DatabaseConnection.getConnection();

    java.util.logging.Logger logger =  java.util.logging.Logger.getLogger(this.getClass().getName());


    public List<User> getAvailableUsers() {

        List<User> users = new ArrayList<>();

        String query = "SELECT id ,  first_name, last_name    FROM user";

        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                User user = new User();
                user.setId(rs.getLong("id"));
                user.setFirstName(rs.getString("first_name"));
                user.setLastName(rs.getString("last_name"));
                users.add(user);
            }


        } catch (SQLException e) {
            logger.info("getAvailableUsers function error ");
            e.printStackTrace();
        }
        logger.info("users most be sent Loading , from service poste ");

        return users;

    }


    public boolean userExists(Long userId) {
        logger.info("execution userExists function ");

        String query = "SELECT COUNT(*) FROM user WHERE id = ?";
        logger.info("querry most be done for users with id  " + userId);

        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setLong(1, userId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }


    // CREATE: Add a new Poste
    public void addPoste(Poste poste , Long userId) {

        // First verify if user exists
        if (!userExists(userId)) {
            throw new IllegalArgumentException("User with ID " + userId + " does not exist");
        }
        String querys = "INSERT INTO post (user_id, content, salaire, description, date_poste, state) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

        String query = "INSERT INTO post ( user_id ,content , salaire, description, date_poste, state) VALUES (?, ?, ?, ?, ?, ?)";
        java.sql.Date sqlDate = new java.sql.Date(poste.getDatePoste().getTime());  /*castusecasefordate*/

        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setLong(1, poste.getUserId());
            ps.setString(2, poste.getContent());
            ps.setDouble(3, poste.getSalaire());
            ps.setString(4, poste.getDescription());
            ps.setDate(5, sqlDate);
            ps.setString(6, poste.getState());
            ps.executeUpdate();
            System.out.println("Poste added successfully!");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }



    // READ: Get all Postes
    public List<Poste> getAllPostes() {
        List<Poste> postes = new ArrayList<>();
        String query = "SELECT * FROM post";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                postes.add(new Poste(
                        rs.getLong("id"),
                        rs.getLong("user_id"),
                        rs.getString("content"),
                        rs.getDouble("salaire"),
                        rs.getString("description"),
                        rs.getDate("date_poste"),
                        rs.getString("state")

                        ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return postes;
    }


    // UPDATE: Modify a Poste
    public void updatePoste(Poste poste) {
        String query = "UPDATE post SET content = ?, salaire = ?, description = ?, date_poste = ? WHERE id = ?";

        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, poste.getContent());
            ps.setDouble(2, poste.getSalaire());
            ps.setString(3, poste.getDescription());
            java.sql.Date sqlDate = new java.sql.Date(poste.getDatePoste().getTime());

            ps.setDate(4, sqlDate);
            ps.setLong(5, poste.getId());
            ps.executeUpdate();
            System.out.println("Poste updated successfully!");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public void updatePosteById(long id, Poste poste) {
        String query = "UPDATE post SET  content = ?, salaire = ?, description = ?, date_poste = ? , state = ? WHERE id = ?";

        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setString(1, poste.getContent());
            preparedStatement.setDouble(2, poste.getSalaire());
            preparedStatement.setString(3, poste.getDescription());
            java.sql.Date sqlDate = new java.sql.Date(poste.getDatePoste().getTime());

            preparedStatement.setDate(4, sqlDate);
            preparedStatement.setString(5, poste.getState());
            preparedStatement.setLong(6, id);
            // Use the provided ID for the WHERE clause

            int rowsUpdated = preparedStatement.executeUpdate();
            if (rowsUpdated > 0) {
                System.out.println("Poste with ID " + id + " updated successfully!");
            } else {
                System.out.println("No Poste found with ID: " + id);
            }
        } catch (SQLException e) {
            System.err.println("Error updating Poste: " + e.getMessage());
            e.printStackTrace();
        }
    }


    // DELETE: Remove a Poste by ID
    public boolean deletePoste(long id) {
        String query = "DELETE FROM post WHERE id = ?";

        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setLong(1, id);
            ps.executeUpdate();
            System.out.println("Poste deleted successfully!");
            return true;
        } catch (SQLException e) {
            System.out.println("Poste 'un'deleted successfully!");
            e.printStackTrace();
            return false;
        }

    }

    public List<Poste> getPostesByState(String state) {
        List<Poste> postes = new ArrayList<>();
        String query = "SELECT * FROM post WHERE state = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, state);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Poste poste = new Poste(
                        rs.getLong("id"),
                        rs.getString("content"),
                        rs.getDouble("salaire"),
                        rs.getString("description"),
                        rs.getDate("date_poste"),
                        rs.getString("state")
                );
                postes.add(poste);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return postes;
    }

}
