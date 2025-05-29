package edp.touristapp.controllers;

import edp.touristapp.api.APIConnector;
import edp.touristapp.models.Place;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.TilePane;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MainController {

    private static final Logger LOGGER = Logger.getLogger(MainController.class.getName());
    private String apiKey;

    @FXML
    private ImageView placeImage;

    @FXML
    public void initialize() {
        try (InputStream input = new FileInputStream("config.properties")) {
            Properties props = new Properties();
            props.load(input);
            apiKey = props.getProperty("API_KEY");

            if (apiKey == null || apiKey.isEmpty()) {
                LOGGER.warning("API key is missing in config.properties");
            }

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to load API key", e);
        }

        cityView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {aLabel.setText(newVal.name());
            }
            if (newVal != null && newVal.photoReference() != null) {
                String imageUrl = "https://maps.googleapis.com/maps/api/place/photo?maxwidth=800"
                        + "&photoreference=" + newVal.photoReference()
                        + "&key=" + apiKey;
                placeImage.setImage(new Image(imageUrl, true));
            } else {
                placeImage.setImage(null);
            }
        });
    }

    @FXML
    private TextField searchBar;
    @FXML
    private Label aLabel;
    @FXML
    private Button searchButton;
    @FXML
    private Label notificationLabel;
    @FXML
    private ListView<Place> cityView;
    @FXML
    private ListView<Place> myTripView;
    @FXML
    private Button createButton;
    @FXML
    private Button addButton;
    @FXML
    private Button deleteButton;
    @FXML
    private Button saveButton;

    private final ObservableList<Place> tripPlaces = FXCollections.observableArrayList();

    @FXML
    private void handleCreate(){
        tripPlaces.clear();
        myTripView.setItems(tripPlaces);
        notificationLabel.setText("New trip created!");
    }

    @FXML
    private void handleAddPlace(){
        Place selected = cityView.getSelectionModel().getSelectedItem();
        if (selected != null && !tripPlaces.contains(selected)) {
            tripPlaces.add(selected);
            notificationLabel.setText("Added: " + selected.name());
        } else if (selected == null) {
            notificationLabel.setText("Select a place to add.");
        } else {
            notificationLabel.setText("Place already in the trip.");
        }
    }

    @FXML
    private void handleSearch() {
        String city = searchBar.getText().trim();
        if (city.isEmpty()) return;

        try {
            String query = URLEncoder.encode("tourist attractions in " + city, StandardCharsets.UTF_8);
            String url = "https://maps.googleapis.com/maps/api/place/textsearch/json?query=" + query + "&key=" + apiKey;

            edp.touristapp.api.APIConnector connector = new APIConnector("");
            JSONObject json = connector.getJSONObject(url);

            JSONArray results = (JSONArray) json.get("results");

            cityView.getItems().clear();

            for (Object obj : results) {
                JSONObject placeJson = (JSONObject) obj;
                String name = (String) placeJson.get("name");
                String address = (String) placeJson.get("formatted_address");
                String photoReference = null;

                JSONArray photos = (JSONArray) placeJson.get("photos");
                if (photos != null && !photos.isEmpty()) {
                    JSONObject photoObj = (JSONObject) photos.getFirst();
                    photoReference = (String) photoObj.get("photo_reference");
                }

                Place place = new Place(name, address, photoReference);
                cityView.getItems().add(place);
            }

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error while searching for places", e);
            notificationLabel.setText("Search failed.");
        }
    }
}