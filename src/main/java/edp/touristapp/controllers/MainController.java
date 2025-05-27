package edp.touristapp.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MainController {


    @FXML
    private TextField searchBar;

    @FXML
    private Button searchButton;

    @FXML
    private Label notificationLabel;

    @FXML
    private void handleSearch(){
        if(searchBar.getText().isEmpty()){
            notificationLabel.setText("Please enter your city");
        }
    }

    private static final Logger LOGGER = Logger.getLogger(MainController.class.getName());

    @FXML
    private WebView mapView;

    private WebEngine webEngine;

    @FXML
    private void initialize(){
        webEngine = mapView.getEngine();
        webEngine.load(Objects.requireNonNull(getClass().getResource("/edp/touristapp/map/index.html")).toExternalForm());
    }

    public void showLocation(double lat, double lon) {
        String script = String.format("showLocation(%f, %f)", lat, lon);
        webEngine.executeScript(script);
    }


}