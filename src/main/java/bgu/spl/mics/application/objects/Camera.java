package bgu.spl.mics.application.objects;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents a camera sensor on the robot.
 * Responsible for detecting objects in the environment.
 */
public class Camera {
    private final int id;
    private final int frequency;
    private STATUS status;
    private final List<StampedDetectedObjects> detectedObjectsList;
    private StampedDetectedObjects lastDetectedObjects;


    /**
     * Constructors for Camera.
     *
     * @param id                 The ID of the camera.
     * @param frequency          The frequency at which the camera sends new events.
     * @param filePath           The file path to the camera data JSON file.
     * @param cameraKey          The key corresponding to this camera in the JSON file.
     */
    public Camera(int id, int frequency, String filePath, String cameraKey) {
        this.id = id;
        this.frequency = frequency;
        this.status = STATUS.UP; // Default status is UP
        this.detectedObjectsList = loadDetectedObjects(filePath, cameraKey);
        this.lastDetectedObjects = null; // Initialize as null
    }

    public Camera(int id, int frequency) {
        this.id = id;
        this.frequency = frequency;
        this.detectedObjectsList = new ArrayList<>();
        this.lastDetectedObjects = null;

    }

    /**
     * Loads the detected objects data from a JSON file.
     *
     * @param filePath  The path to the camera data JSON file.
     * @param cameraKey The key corresponding to this camera in the JSON file.
     * @return A list of stamped detected objects.
     */
    private List<StampedDetectedObjects> loadDetectedObjects(String filePath, String cameraKey) {
        try (FileReader reader = new FileReader(filePath)) {
            JsonObject jsonObject = JsonParser.parseReader(reader).getAsJsonObject();
            Type type = new TypeToken<List<StampedDetectedObjects>>() {}.getType();
            return new Gson().fromJson(jsonObject.getAsJsonArray(cameraKey), type);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load camera data for " + cameraKey + " from file: " + filePath, e);
        }
    }

    public StampedDetectedObjects getLastDetectedObjects() {
        return lastDetectedObjects;
    }

    /**
     * Detects objects visible at the given simulation time.
     *
     * @param time The current simulation time.
     * @return A list of detected objects at the given time, considering the camera's frequency and status.
     */
    public StampedDetectedObjects detectObjects(int time) {
        if (status != STATUS.UP) {
            lastDetectedObjects = new StampedDetectedObjects(time, Collections.emptyList());
            return lastDetectedObjects;
        }

        StampedDetectedObjects detected = detectedObjectsList.stream()
                .filter(detectedObj -> detectedObj.getTime() + frequency == time)
                .findFirst() // Retrieve the first matching StampedDetectedObjects
                .orElse(new StampedDetectedObjects(time, Collections.emptyList())); // Default to empty if no match

        // Check if all objects in the detected StampedDetectedObjects have valid IDs
        boolean hasNoError = detected.getDetectedObjects().stream()
                .noneMatch(obj -> obj.getId().equals("ERROR")); // Replace isError() with your error-checking logic

        // Update lastDetectedObjects only if no object has an error ID
        if (!detected.getDetectedObjects().isEmpty() && hasNoError) {
            lastDetectedObjects = detected;
        }
        return detected;
    }

    /**
     * Returns the latest time that an object will be detected by the camera.
     *
     * @return The latest detection time, adjusted by the camera's frequency.
     */
    public int getLatestDetectionTime() {
        int maxTime = detectedObjectsList.stream()
                .mapToInt(StampedDetectedObjects::getTime)
                .max()
                .orElse(-1); // Return -1 if no detections are present
        return maxTime == -1 ? -1 : maxTime + frequency;
    }


    public void addDetectedObjects(StampedDetectedObjects detectedObjects) {
        if (detectedObjects == null || detectedObjects.getDetectedObjects() == null) {
            throw new IllegalArgumentException("Detected objects cannot be null.");
        }

        detectedObjectsList.add(detectedObjects);
        lastDetectedObjects = detectedObjects;
    }


    /**
     * Sets the status of the camera.
     *
     * @param status The new status of the camera.
     */
    public void setStatus(STATUS status) {
        this.status = status;
    }

    /**
     * Gets the ID of the camera.
     *
     * @return The ID of the camera.
     */
    public int getId() {
        return id;
    }

    public int getFrequency() {return frequency;}

    /**
     * Checks if the camera has any detected objects with an "ERROR" ID.
     *
     * @return The time of the first occurrence of an "ERROR" detected object,
     *         or -1 if no errors are found.
     */
    public int hasError() {
        return detectedObjectsList.stream()
                .filter(detected -> detected.getDetectedObjects().stream()
                        .anyMatch(obj -> "ERROR".equals(obj.getId())))
                .mapToInt(StampedDetectedObjects::getTime)
                .findFirst()
                .orElse(-1);
    }


}