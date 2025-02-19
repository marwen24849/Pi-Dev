package esprit.tn.pidevrh;

import esprit.tn.pidevrh.Poste.Poste;
import esprit.tn.pidevrh.Poste.PosteService;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.http.*;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class MainApp extends Application {
    @Override
    public void start(Stage stage) throws IOException {


        FXMLLoader loader = new FXMLLoader(getClass().getResource("/Fxml/SideBar/sidebar.fxml"));
        Parent root = loader.load();

        Scene scene = new Scene(root);
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/Fxml/SideBar/sidebar.css")).toExternalForm());

        stage.setTitle("Gestion des Questions");
        stage.setScene(scene);
        stage.setResizable(true);

        stage.show();

        PosteService posteService = new PosteService();




        List<Poste> postes = posteService.getAllPostes();
        System.out.println("All Postes:");
        for (Poste p : postes) {
            System.out.println(p);
        }



        Poste poste = new Poste();
        poste.setUserId(1);
        poste.setContent("new post content ");
        poste.setSalaire(50000.0);
        poste.setDescription("new post  Description");
        // Convert java.util.Date to java.sql.Date
        java.util.Date utilDate = new java.util.Date(); // Current date and time
        java.sql.Date sqlDate = new java.sql.Date(utilDate.getTime()); // Convert to java.sql.Da

        poste.setDatePoste(sqlDate);
        poste.setState("non active");

        posteService.addPoste(poste, 1L);


       /* update main test
        int posteId = 2;

        Poste updatedPoste = new Poste();
        updatedPoste.setId(posteId);
        updatedPoste.setContent("Updated Content");
        updatedPoste.setSalaire(60000.0);
        updatedPoste.setDescription("Updated Description");

        // Set the updated date
        java.util.Date newUtilDate = new java.util.Date(); // Current date and time
        java.sql.Date newSqlDate = new java.sql.Date(newUtilDate.getTime()); // Convert to java.sql.Date
        updatedPoste.setDatePoste(newSqlDate);

        // Call the updatePoste method
        posteService.updatePoste(updatedPoste);
        System.out.println("Poste added successfully!");
        /*


        */


/* test posteupdateIDservice

   long posteIdToUpdate = 2; // Change this ID to an existing one in your database

        // Create a new Poste object with updated values
        Poste updatedPoste = new Poste();
        updatedPoste.setContent("updated 3 ");
        updatedPoste.setSalaire(7500.00);
        updatedPoste.setDescription("Updated job 3 .");
        updatedPoste.setDatePoste(new java.sql.Date(System.currentTimeMillis())); // ✅ Correct
        updatedPoste.setState("Disactive");


        // Call updatePosteById
        posteService.updatePosteById(posteIdToUpdate, updatedPoste);


 */


        /*DEL

        long posteIdToDelete = 2; // Change this ID to an existing one in your database


        boolean deleted = posteService.deletePoste(posteIdToDelete);

        if (deleted) {
            System.out.println("Poste with ID " + posteIdToDelete + " deleted successfully!");
        } else {
            System.out.println("No Poste found with ID: " + posteIdToDelete);
        }

        */

    }
    //test

    public static void main(String[] args) {
        launch(args);
    }
}
