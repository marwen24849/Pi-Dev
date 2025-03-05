module esprit.tn.pidevrh {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires net.synedra.validatorfx;
    requires org.kordamp.bootstrapfx.core;
    requires javafx.graphics;
    requires java.sql;
    requires java.net.http;
    requires jdk.httpserver;

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
    requires static lombok;


    opens esprit.tn.pidevrh.leave to javafx.fxml;

    requires com.google.api.client.auth;
    requires com.google.api.client.extensions.java6.auth;
    requires com.google.api.client.extensions.jetty.auth;
    requires google.api.client;
    requires com.google.api.client;
    requires com.google.api.services.calendar;
    requires com.google.api.client.json.jackson2;

    exports esprit.tn.pidevrh.teams_departements to javafx.graphics, javafx.fxml;
    opens esprit.tn.pidevrh.teams_departements to javafx.fxml, javafx.base;

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

    exports esprit.tn.pidevrh.session;  // Export the session package for FXML access
    opens esprit.tn.pidevrh.session to javafx.fxml;  // Allows FXMLLoader to access the session controller
    exports esprit.tn.pidevrh.leave;  // Export leave module
    exports esprit.tn.pidevrh.chat to javafx.fxml;
    opens esprit.tn.pidevrh.chat to javafx.fxml, java.base, javafx.base;

    exports esprit.tn.pidevrh.dashboard to javafx.fxml;
    opens esprit.tn.pidevrh.dashboard to javafx.fxml, java.base, javafx.base;

    opens esprit.tn.pidevrh.Poste to javafx.fxml , javafx.base;
    exports esprit.tn.pidevrh.Poste;

    opens esprit.tn.pidevrh.projet to javafx.fxml, javafx.base;
}
