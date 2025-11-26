package bgu.spl.mics.application.objects;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * LiDarDataBase is a singleton class responsible for managing LiDAR data.
 * It provides access to cloud point data and other relevant information for tracked objects.
 */
public class LiDarDataBase {

    /**
     * Private static inner class responsible for holding the Singleton instance.
     */
    private static class Holder {
        private static final LiDarDataBase INSTANCE = new LiDarDataBase();
    }

    /**
     * Represents the data points collected by the LiDAR sensor, stored as a list of StampedCloudPoints.
     */
    private final List<StampedCloudPoints> cloudPoints;

    /**
     * Private constructor for LiDarDataBase to ensure singleton pattern.
     */
    private LiDarDataBase() {
        this.cloudPoints = new ArrayList<>();
    }

    /**
     * Returns the singleton instance of LiDarDataBase.
     *
     * @param filePath The path to the LiDAR data file.
     * @return The singleton instance of LiDarDataBase.
     */
    public static LiDarDataBase getInstance(String filePath) {
        LiDarDataBase instance = Holder.INSTANCE;
        instance.loadData(filePath);
        return instance;
    }

    /**
     * Loads LiDAR data from a JSON file.
     *
     * @param filePath The path to the JSON file containing LiDAR data.
     */

    private void loadData(String filePath) {
        try (FileReader reader = new FileReader(filePath)) {
            // Parse the file into a JsonArray
            JsonArray jsonArray = JsonParser.parseReader(reader).getAsJsonArray();
            List<StampedCloudPoints> data = new ArrayList<>();

            // Process each JSON element in the array
            for (com.google.gson.JsonElement element : jsonArray) {
                JsonObject jsonObject = element.getAsJsonObject();
                int time = jsonObject.get("time").getAsInt();
                String id = jsonObject.get("id").getAsString();

                // Manually process cloudPoints
                JsonArray cloudPointsArray = jsonObject.get("cloudPoints").getAsJsonArray();
                List<CloudPoint> cloudPoints = new ArrayList<>();
                for (com.google.gson.JsonElement pointElement : cloudPointsArray) {
                    JsonArray pointArray = pointElement.getAsJsonArray();
                    double x = pointArray.get(0).getAsDouble();
                    double y = pointArray.get(1).getAsDouble();
                    cloudPoints.add(new CloudPoint(x, y));
                }

                // Create StampedCloudPoints object and add to list
                StampedCloudPoints stampedCloudPoints = new StampedCloudPoints(id, time, cloudPoints);
                data.add(stampedCloudPoints);
            }

            // Add all processed data to cloudPoints
            cloudPoints.addAll(data);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load LiDAR data from file: " + filePath, e);
        }
    }

    /**
     * Retrieves all stamped cloud points in the database.
     *
     * @return A list of all stamped cloud points.
     */
    public List<StampedCloudPoints> getCloudPoints() {
        return Collections.unmodifiableList(cloudPoints);
    }

}
