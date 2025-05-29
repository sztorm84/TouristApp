package edp.touristapp.models;


import org.json.simple.JSONArray;

public record Place(String name, String address, String photoReference) {

    public String toString() {
        return name + "\n" + address;
    }
}