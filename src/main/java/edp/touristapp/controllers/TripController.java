package edp.touristapp.controllers;

import edp.touristapp.databases.DatabaseManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import java.sql.*;

public class TripController {
    @FXML
    private ListView<String> tripsListView;
    private String selectedTripName = null;

    public void initialize() {
        loadTrips();
    }

    private void loadTrips() {
        String query = "SELECT name FROM trips";
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:touristapp.sqlite");
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                tripsListView.getItems().add(rs.getString("name"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleSelect() {
        selectedTripName = tripsListView.getSelectionModel().getSelectedItem();
        if (selectedTripName != null) {
            tripsListView.getScene().getWindow().hide();
        }
    }

    public String getSelectedTripName() {
        return selectedTripName;
    }

    public void handleDeleteTrip(ActionEvent actionEvent) {
        selectedTripName = tripsListView.getSelectionModel().getSelectedItem();

        if (selectedTripName == null || selectedTripName.isBlank()) {
            return;
        }

        try {
            DatabaseManager.deleteTripByName(selectedTripName);
            tripsListView.getItems().remove(selectedTripName);
            selectedTripName = null;
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
