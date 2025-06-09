package edp.touristapp.controllers;

import edp.touristapp.api.APIConnector;
import edp.touristapp.databases.DatabaseManager;
import edp.touristapp.events.AppEventBus;
import edp.touristapp.events.PlaceAddedEvent;
import edp.touristapp.models.Place;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.Properties;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MainController {

    private static final Logger LOGGER = Logger.getLogger(MainController.class.getName());
    private String apiKey;
    private Integer currentTripId = null;
    private final ObservableList<Place> tripPlaces = FXCollections.observableArrayList();
    @FXML
    private ImageView placeImage;
    @FXML
    private TextField searchBar;
    @FXML
    private Label aLabel;
    @FXML
    private Label notificationLabel;
    @FXML
    private ListView<Place> cityView;
    @FXML
    private ListView<Place> myTripView;
    @FXML
    private Label listName;

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

        showDetails(cityView);
        showDetails(myTripView);

        AppEventBus.getInstance().register(this);
    }

    private void showDetails(ListView<Place> myTripView) {
        myTripView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
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
    private void handleCreate() {
        myTripView.getItems().clear();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/edp/touristapp/create-view.fxml"));
            Parent page = loader.load();

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Create trip");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(myTripView.getScene().getWindow());
            dialogStage.setScene(new Scene(page));

            CreateTripController controller = loader.getController();
            controller.setDialogStage(dialogStage);

            dialogStage.showAndWait();

            if (controller.isOkClicked()) {
                String name = controller.getTripName();
                tripPlaces.clear();
                myTripView.setItems(tripPlaces);
                listName.setText(name);
                notificationLabel.setText("New trip: " + name);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleAddPlace() {
        if (listName.getText() == null || listName.getText().isBlank()) {
            notificationLabel.setText("Create a new trip first.");
            return;
        }

        Place selected = cityView.getSelectionModel().getSelectedItem();
        if (selected != null && !tripPlaces.contains(selected)) {
            tripPlaces.add(selected);

            AppEventBus.getInstance().post(new PlaceAddedEvent(selected));

        } else if (selected == null) {
            notificationLabel.setText("Select a place to add.");
        } else {
            notificationLabel.setText("Place already in the trip.");
        }
    }


    @FXML
    private void handleDelete() {
        Place selected = myTripView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            tripPlaces.remove(selected);
            notificationLabel.setText("Deleted: " + selected.name());
        } else {
            notificationLabel.setText("Select a place to delete.");
        }
    }

    @FXML
    private void handleSave() {
        if (tripPlaces.isEmpty()) {
            notificationLabel.setText("Trip is empty!");
            return;
        }

        String tripTitle = listName.getText();
        if (tripTitle == null || tripTitle.isBlank()) {
            notificationLabel.setText("Trip name is missing!");
            return;
        }

        notificationLabel.setText("Saving trip...");

        Task<Void> saveTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                DatabaseManager.getInstance().saveTrip(tripTitle, tripPlaces, currentTripId);
                return null;
            }
        };

        saveTask.setOnSucceeded(e -> {
            notificationLabel.setText("Trip saved to database!");
            tripPlaces.clear();
            myTripView.setItems(tripPlaces);
            listName.setText("");
        });

        saveTask.setOnFailed(e -> {
            Throwable ex = saveTask.getException();
            ex.printStackTrace();
            notificationLabel.setText("Failed to save trip: " + ex.getMessage());
        });

        new Thread(saveTask).start();
    }


    @FXML
    private void handleShow() {

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/edp/touristapp/trip-view.fxml"));
            Parent root = loader.load();

            TripController controller = loader.getController();

            Stage stage = new Stage();
            stage.setTitle("Select a Trip");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            String selectedTripName = controller.getSelectedTripName();
            if (selectedTripName != null) {
                loadTripPlaces(selectedTripName);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadTripPlaces(String tripName) {
        Task<ObservableList<Place>> loadTask = new Task<>() {
            @Override
            protected ObservableList<Place> call() throws Exception {
                String query = "SELECT p.* FROM places p JOIN trips t ON p.trip_id = t.id WHERE t.name = ?";
                ObservableList<Place> tripPlaces = FXCollections.observableArrayList();

                try (Connection conn = DriverManager.getConnection("jdbc:sqlite:touristapp.sqlite");
                     PreparedStatement stmt = conn.prepareStatement(query)) {

                    stmt.setString(1, tripName);
                    ResultSet rs = stmt.executeQuery();

                    while (rs.next()) {
                        String name = rs.getString("name");
                        String address = rs.getString("address");
                        String photoReference = rs.getString("photo_reference");

                        Place place = new Place(name, address, photoReference);
                        tripPlaces.add(place);
                    }
                }
                return tripPlaces;
            }
        };

        loadTask.setOnSucceeded(e -> {
            ObservableList<Place> places = loadTask.getValue();
            tripPlaces.clear();
            tripPlaces.addAll(places);
            myTripView.setItems(tripPlaces);
            listName.setText(tripName);
            notificationLabel.setText("Loaded places for " + tripName);
        });

        loadTask.setOnFailed(e -> {
            Throwable ex = loadTask.getException();
            ex.printStackTrace();
            notificationLabel.setText("Failed to load places: " + ex.getMessage());
        });

        new Thread(loadTask).start();
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