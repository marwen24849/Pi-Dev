module esprit.tn.pidevrh {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.controlsfx.controls;
    requires net.synedra.validatorfx;
    requires org.kordamp.bootstrapfx.core;
    requires javafx.graphics;
    requires java.sql;
    requires java.net.http;
    requires com.google.api.client.auth;
    requires com.google.api.client.extensions.java6.auth;
    requires com.google.api.client.extensions.jetty.auth;
    requires google.api.client;
    requires com.google.api.client;
    requires com.google.api.services.calendar;
    requires com.google.api.client.json.jackson2;
    requires jdk.httpserver;
    requires java.desktop;

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
    exports esprit.tn.pidevrh.login to javafx.fxml;
    opens esprit.tn.pidevrh.login to javafx.fxml, java.base;
    opens esprit.tn.pidevrh.projet to javafx.fxml, javafx.base;  // Added this line
}
