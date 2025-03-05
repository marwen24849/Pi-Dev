module esprit.tn.pidevrh {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires javafx.base;

    requires org.controlsfx.controls;
    requires net.synedra.validatorfx;
    requires org.kordamp.bootstrapfx.core;

    requires java.net.http;
    requires com.google.gson;
    requires java.sql;



    requires java.mail;

    requires jdk.httpserver;
    requires java.desktop;
    requires mysql.connector.java;
    requires jbcrypt;
    requires jdk.jdi;

    requires org.json;
    requires io.github.cdimascio.dotenv.java;

    requires com.fasterxml.jackson.databind;
    requires okhttp3;
    requires annotations;
    requires static lombok;

    // Google API dependencies
    requires com.google.api.client.auth;
    requires com.google.api.client.extensions.java6.auth;
    requires com.google.api.client.extensions.jetty.auth;
    requires com.google.api.client;
    requires com.google.api.services.calendar;
    requires com.google.api.client.json.jackson2;
    requires google.api.client;
    requires kernel;
    requires layout;
    requires activation;
    requires io;

    // Opens and Exports for JavaFX Controllers
    opens esprit.tn.pidevrh to javafx.fxml;
    exports esprit.tn.pidevrh;

    opens esprit.tn.pidevrh.congeApprove to javafx.fxml;
    exports esprit.tn.pidevrh.congeApprove;

    opens esprit.tn.pidevrh.leave to javafx.fxml;
    exports esprit.tn.pidevrh.leave;

    opens esprit.tn.pidevrh.teams_departements to javafx.fxml, javafx.base;
    exports esprit.tn.pidevrh.teams_departements;

    opens esprit.tn.pidevrh.appbar to javafx.fxml;
    exports esprit.tn.pidevrh.appbar;

    opens esprit.tn.pidevrh.question to javafx.fxml, javafx.base;
    exports esprit.tn.pidevrh.question;

    opens esprit.tn.pidevrh.quiz to javafx.fxml, java.base, javafx.base;
    exports esprit.tn.pidevrh.quiz;

    opens esprit.tn.pidevrh.response to javafx.fxml, java.base, javafx.base;
    exports esprit.tn.pidevrh.response;

    opens esprit.tn.pidevrh.login to javafx.fxml, java.base;
    exports esprit.tn.pidevrh.login;

    opens esprit.tn.pidevrh.Reclamation to javafx.fxml;

    opens esprit.tn.pidevrh.formation to javafx.fxml, java.base, javafx.base;
    exports esprit.tn.pidevrh.formation;

    opens esprit.tn.pidevrh.session to javafx.fxml;
    exports esprit.tn.pidevrh.session;

    opens esprit.tn.pidevrh.chat to javafx.fxml, java.base, javafx.base;
    exports esprit.tn.pidevrh.chat;

    opens esprit.tn.pidevrh.dashboard to javafx.fxml, java.base, javafx.base;
    exports esprit.tn.pidevrh.dashboard;

    opens esprit.tn.pidevrh.projet to javafx.fxml, javafx.base;
}
