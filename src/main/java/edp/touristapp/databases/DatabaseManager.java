package edp.touristapp.databases;

import java.sql.*;
import java.util.List;

import edp.touristapp.models.Place;

public class DatabaseManager {
    private static final String DB_URL = "jdbc:sqlite:touristapp.sqlite";
    private static DatabaseManager instance;

    private Connection connection;

    private DatabaseManager() {
        try {
            connection = DriverManager.getConnection(DB_URL);
            connection.setAutoCommit(false);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    public void initializeDatabase() {
        try (Statement stmt = connection.createStatement()) {
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
            connection.commit();

        } catch (SQLException e) {
            e.printStackTrace();
            try {
                connection.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }

    public void saveTrip(String tripName, List<Place> places, Integer tripId) throws SQLException {
        String insertTrip = "INSERT INTO trips(name) VALUES (?)";
        String updateTrip = "UPDATE trips SET name = ? WHERE id = ?";
        String insertPlace = "INSERT INTO places(trip_id, name, address, photo_reference) VALUES (?, ?, ?, ?)";
        String deletePlaces = "DELETE FROM places WHERE trip_id = ?";

        try {
            if (tripId == null) {
                try (PreparedStatement tripStmt = connection.prepareStatement(insertTrip, Statement.RETURN_GENERATED_KEYS)) {
                    tripStmt.setString(1, tripName);
                    tripStmt.executeUpdate();

                    ResultSet rs = tripStmt.getGeneratedKeys();
                    if (rs.next()) {
                        tripId = rs.getInt(1);
                    }
                }
            } else {
                try (PreparedStatement updateStmt = connection.prepareStatement(updateTrip)) {
                    updateStmt.setString(1, tripName);
                    updateStmt.setInt(2, tripId);
                    updateStmt.executeUpdate();
                }
                try (PreparedStatement deleteStmt = connection.prepareStatement(deletePlaces)) {
                    deleteStmt.setInt(1, tripId);
                    deleteStmt.executeUpdate();
                }
            }
            try (PreparedStatement placeStmt = connection.prepareStatement(insertPlace)) {
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
            connection.commit();

        } catch (SQLException e) {
            e.printStackTrace();
            try {
                connection.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            throw e;
        }
    }

    public static void deleteTripByName(String tripName) throws SQLException {
        String selectTripId = "SELECT id FROM trips WHERE name = ?";
        String deletePlaces = "DELETE FROM places WHERE trip_id = ?";
        String deleteTrip = "DELETE FROM trips WHERE id = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            conn.setAutoCommit(false);

            try (PreparedStatement selectStmt = conn.prepareStatement(selectTripId)) {
                selectStmt.setString(1, tripName);
                ResultSet rs = selectStmt.executeQuery();

                if (rs.next()) {
                    int tripId = rs.getInt("id");

                    try (PreparedStatement deletePlacesStmt = conn.prepareStatement(deletePlaces)) {
                        deletePlacesStmt.setInt(1, tripId);
                        deletePlacesStmt.executeUpdate();
                    }

                    try (PreparedStatement deleteTripStmt = conn.prepareStatement(deleteTrip)) {
                        deleteTripStmt.setInt(1, tripId);
                        deleteTripStmt.executeUpdate();
                    }
                }

                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }
}
