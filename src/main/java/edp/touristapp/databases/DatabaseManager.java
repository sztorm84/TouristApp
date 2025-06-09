package edp.touristapp.databases;

import java.sql.*;
import java.util.List;

import edp.touristapp.models.Place;

public class DatabaseManager {
    private static final String DB_URL = "jdbc:sqlite:touristapp.sqlite";

    public static void initializeDatabase() {
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {

            String createTrips = """
                CREATE TABLE IF NOT EXISTS trips (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    name TEXT NOT NULL
                );
                """;

            String createPlaces = """
                CREATE TABLE IF NOT EXISTS places (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    trip_id INTEGER NOT NULL,
                    name TEXT NOT NULL,
                    address TEXT,
                    photo_reference TEXT,
                    FOREIGN KEY(trip_id) REFERENCES trips(id)
                );
                """;

            stmt.execute(createTrips);
            stmt.execute(createPlaces);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void saveTrip(String tripName, List<Place> places, Integer tripId) throws SQLException {
        String insertTrip = "INSERT INTO trips(name) VALUES (?)";
        String updateTrip = "UPDATE trips SET name = ? WHERE id = ?";
        String insertPlace = "INSERT INTO places(trip_id, name, address, photo_reference) VALUES (?, ?, ?, ?)";
        String deletePlaces = "DELETE FROM places WHERE trip_id = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            conn.setAutoCommit(false);

            if (tripId == null) {
                try (PreparedStatement tripStmt = conn.prepareStatement(insertTrip, Statement.RETURN_GENERATED_KEYS)) {
                    tripStmt.setString(1, tripName);
                    tripStmt.executeUpdate();

                    ResultSet rs = tripStmt.getGeneratedKeys();
                    if (rs.next()) {
                        tripId = rs.getInt(1);
                    }
                }
            } else {
                try (PreparedStatement updateStmt = conn.prepareStatement(updateTrip)) {
                    updateStmt.setString(1, tripName);
                    updateStmt.setInt(2, tripId);
                    updateStmt.executeUpdate();
                }
                try (PreparedStatement deleteStmt = conn.prepareStatement(deletePlaces)) {
                    deleteStmt.setInt(1, tripId);
                    deleteStmt.executeUpdate();
                }
            }
            try (PreparedStatement placeStmt = conn.prepareStatement(insertPlace)) {
                for (Place p : places) {
                    if (tripId == null) {
                        throw new IllegalStateException("tripId cannot be null here!");
                    }
                    placeStmt.setInt(1, tripId);
                    placeStmt.setString(2, p.name());
                    placeStmt.setString(3, p.address());
                    placeStmt.setString(4, p.photoReference());
                    placeStmt.addBatch();
                }
                placeStmt.executeBatch();
            }
            conn.commit();
        } catch (SQLException e) {
            e.printStackTrace();
            throw e;
        }
    }

}
