package bgu.spl.mics.application.objects;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * LiDarWorkerTracker is responsible for managing a LiDAR worker.
 * It processes data for detected objects and generates tracked objects based on the LiDAR database.
 */
public class LiDarWorkerTracker {

    private final int id;
    private final int frequency;
    private final STATUS status; // Enum indicating the status
    private List<TrackedObject> lastTrackedObjects;
    private final LiDarDataBase dataBase;


    /**
     * Constructor for LiDarWorkerTracker.
     *
     * @param id The ID of the LiDAR worker.
     * @param frequency The frequency of updates for this LiDAR worker.
     * @param dataBase The LiDarDataBase to retrieve cloud point data.
     */
    public LiDarWorkerTracker(int id, int frequency, LiDarDataBase dataBase) {
        this.id = id;
        this.frequency = frequency;
        this.status = STATUS.UP; // Default status is UP
        this.lastTrackedObjects = new ArrayList<>();
        this.dataBase = dataBase;
    }

    /**
     * Gets the frequency of updates for this LiDAR worker.
     *
     * @return The frequency.
     */
    public int getFrequency() {
        return frequency;
    }

    /**
     * Gets the status of the LiDAR worker.
     *
     * @return The current status.
     */
    public STATUS getStatus() {
        return status;
    }

    public int getId() {return id;}

    /**
     * Gets the latest detection time from the database.
     *
     * @return The latest detection time plus the frequency, or -1 if no detections.
     */
    public int getLatestDetectionTime() {
        return dataBase.getCloudPoints().stream()
                .mapToInt(StampedCloudPoints::getTime)
                .max()
                .orElse(-1) + frequency;
    }


    /**
     * Processes a list of detected objects and returns corresponding TrackedObjects.
     *
     * @param objectsToTrack The list of objects to track.
     * @param detectionTime The current time to set for the tracked objects.
     * @return A list of TrackedObjects representing the processed data.
     */
    public List<TrackedObject> processData(StampedDetectedObjects objectsToTrack, int detectionTime) {
        lastTrackedObjects = new ArrayList<>();

        // Process each detected object individually
        objectsToTrack.getDetectedObjects().forEach(detectedObject -> {
            TrackedObject trackedObject = processSingleObject(detectedObject, detectionTime);
                lastTrackedObjects.add(trackedObject);
        });

        return lastTrackedObjects;
    }


    /**
     * Processes a single detected object and returns a corresponding TrackedObject.
     *
     * @param detectedObject The detected object to process.
     * @param detectionTime The current time to set for the tracked object.
     * @return A TrackedObject representing the processed data.
     */
    private TrackedObject processSingleObject(DetectedObject detectedObject, int detectionTime) {
        // Retrieve relevant cloud points from the database
        List<CloudPoint> cloudPoints = dataBase.getCloudPoints().stream()
                .filter(scp -> scp.getId().equals(detectedObject.getId()) && scp.getTime() == detectionTime)
                .flatMap(scp -> scp.getCloudPoints().stream())
                .collect(Collectors.toList());

        // Create a TrackedObject with the retrieved data
        TrackedObject trackedObject = new TrackedObject(
                detectedObject.getId(),
                detectionTime,
                detectedObject.getDescription(),
                cloudPoints
        );

        return trackedObject;
    }

    /**
     * Gets the last tracked objects by this LiDAR worker.
     *
     * @return A list of the last tracked objects.
     */
    public List<TrackedObject> getLastTrackedObjects() {
        return new ArrayList<>(lastTrackedObjects);
    }

    /**
     * Checks the database for any entries with id="error".
     *
     * @return The error time if found, or -1 if no errors.
     */
    public int hasError() {
        return dataBase.getCloudPoints().stream()
                .filter(scp -> "ERROR".equals(scp.getId()))
                .mapToInt(StampedCloudPoints::getTime)
                .findFirst()
                .orElse(-1);
    }
}
