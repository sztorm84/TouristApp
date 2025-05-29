module edp.touristapp {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires org.kordamp.ikonli.javafx;
    requires eu.hansolo.tilesfx;
    requires json.simple;
    requires java.logging;
    requires java.net.http;

    opens edp.touristapp to javafx.fxml;
    opens edp.touristapp.controllers to javafx.fxml;

    exports edp.touristapp.controllers;
    exports edp.touristapp;
    exports edp.touristapp.models;
}