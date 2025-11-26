package bgu.spl.mics.application.objects;

import java.util.List;

/**
 * Represents an object tracked by the LiDAR.
 * This object includes information about the tracked object's ID, description,
 * time of tracking, and coordinates in the environment.
 */
public class TrackedObject {

    private final String id;
    private final int timestamp;
    private final String description;
    private final List<CloudPoint> coordinates;

    /**
     * Constructor for TrackedObject.
     *
     * @param id The unique ID of the tracked object.
     * @param timestamp The time the object was tracked.
     * @param description A description of the object.
     * @param coordinates A list of CloudPoints representing the object's location.
     */
    public TrackedObject(String id, int timestamp, String description, List<CloudPoint> coordinates) {
        this.id = id;
        this.timestamp = timestamp;
        this.description = description;
        this.coordinates = coordinates;
    }

    /**
     * Gets the unique ID of the tracked object.
     *
     * @return The ID of the object.
     */
    public String getId() {
        return id;
    }

    /**
     * Gets the timestamp when the object was tracked.
     *
     * @return The timestamp.
     */
    public int getTimestamp() {
        return timestamp;
    }

    /**
     * Gets the description of the tracked object.
     *
     * @return The description.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Gets the list of coordinates representing the tracked object's location.
     *
     * @return A list of CloudPoints.
     */
    public List<CloudPoint> getCoordinates() {
        return coordinates;
    }

    @Override
    public String toString() {
        return "TrackedObject{" +
                "id=" + id +
                ", timestamp=" + timestamp +
                ", description='" + description + '\'' +
                ", coordinates=" + coordinates +
                '}';
    }
}
