package edp.touristapp.api;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Scanner;
import java.util.logging.Logger;
import java.util.logging.Level;


public class APIConnector {

    private final String urlString;
    private static final Logger logger = Logger.getLogger(APIConnector.class.getName());

    public APIConnector(String urlString) throws MalformedURLException {
        this.urlString = urlString;
    }

    public JSONArray getJSONArray(String query){
        try {
            URI uri = URI.create(urlString + query);
            URL url = uri.toURL();

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.connect();

            //Check if connect is made
            int responseCode = conn.getResponseCode();

            if (responseCode != 200) {
                throw new RuntimeException("HttpResponseCode: " + responseCode);
            } else {

                StringBuilder informationString = new StringBuilder();
                Scanner scanner = new Scanner(url.openStream());

                while (scanner.hasNext()) {
                    informationString.append(scanner.nextLine());
                }
                scanner.close();

                JSONParser parse = new JSONParser();

                return (JSONArray) parse.parse(String.valueOf(informationString));
            }
        }catch (Exception e) {
            logger.log(Level.SEVERE, "Error while calling API", e);
        }
        return null;
    }

    public JSONObject getJSONObject(String query){
        try {
            URI uri = URI.create(urlString + query);
            URL url = uri.toURL();

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.connect();

            //Check if connect is made
            int responseCode = conn.getResponseCode();

            if (responseCode != 200) {
                throw new RuntimeException("HttpResponseCode: " + responseCode);
            } else {

                StringBuilder informationString = new StringBuilder();
                Scanner scanner = new Scanner(url.openStream());

                while (scanner.hasNext()) {
                    informationString.append(scanner.nextLine());
                }
                scanner.close();

                JSONParser parse = new JSONParser();

                return (JSONObject) parse.parse(String.valueOf(informationString));
            }
        }catch (Exception e) {
            logger.log(Level.SEVERE, "Error while calling API", e);
        }
        return null;
    }
}

