module esprit.tn.pidevrh {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.controlsfx.controls;
    requires net.synedra.validatorfx;
    requires org.kordamp.bootstrapfx.core;
    requires javafx.graphics;
    requires java.sql;
    requires java.net.http;

    requires java.desktop;
    requires mysql.connector.java;
    requires jbcrypt;
    requires jdk.jdi;
    requires java.mail;
    requires org.json;
    requires io.github.cdimascio.dotenv.java;

    requires com.fasterxml.jackson.databind;
    requires okhttp3;
    requires annotations;



    opens esprit.tn.pidevrh.leave to javafx.fxml;
    opens esprit.tn.pidevrh to javafx.fxml;
    exports esprit.tn.pidevrh;
    exports esprit.tn.pidevrh.question to javafx.fxml;
    exports esprit.tn.pidevrh.appbar to javafx.fxml;
    opens esprit.tn.pidevrh.appbar to javafx.fxml;
    opens esprit.tn.pidevrh.question to javafx.fxml, javafx.base;
    exports esprit.tn.pidevrh.quiz to javafx.fxml;
    opens esprit.tn.pidevrh.quiz to javafx.fxml, java.base, javafx.base;
    exports esprit.tn.pidevrh.response to javafx.fxml;
    opens esprit.tn.pidevrh.response to javafx.fxml, java.base, javafx.base;
    exports esprit.tn.pidevrh.login to javafx.fxml;
    opens esprit.tn.pidevrh.login to javafx.fxml, java.base;


    opens esprit.tn.pidevrh.Reclamation to javafx.fxml;

    exports esprit.tn.pidevrh.formation to javafx.fxml;
    opens esprit.tn.pidevrh.formation to javafx.fxml, java.base, javafx.base;
    exports esprit.tn.pidevrh.chat to javafx.fxml;
    opens esprit.tn.pidevrh.chat to javafx.fxml, java.base, javafx.base;

    exports esprit.tn.pidevrh.leave;

}
