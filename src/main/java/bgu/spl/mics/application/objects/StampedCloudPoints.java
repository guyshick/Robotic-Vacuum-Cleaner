package bgu.spl.mics.application.objects;

import java.util.List;

/**
 * Represents a group of cloud points corresponding to a specific timestamp.
 * Used by the LiDAR system to store and process point cloud data for tracked objects.
 */
public class StampedCloudPoints {

    /**
     * The ID of the object this data corresponds to.
     */
    private final String id;

    /**
     * The timestamp when these points were captured.
     */
    private final int time;

    /**
     * A list of cloud points representing 3D spatial data.
     */
    private final List<CloudPoint> cloudPoints;

    /**
     * Constructor for StampedCloudPoints.
     *
     * @param id The ID of the object.
     * @param time The timestamp of the data.
     * @param cloudPoints A list of cloud points.
     */
    public StampedCloudPoints(String id, int time, List<CloudPoint> cloudPoints) {
        this.id = id;
        this.time = time;
        this.cloudPoints = cloudPoints;
    }

    /**
     * Gets the ID of the object.
     *
     * @return The object ID.
     */
    public String getId() {
        return id;
    }

    /**
     * Gets the timestamp of the data.
     *
     * @return The timestamp.
     */
    public int getTime() {
        return time;
    }

    /**
     * Gets the list of cloud points.
     *
     * @return A list of cloud points.
     */
    public List<CloudPoint> getCloudPoints() {
        return cloudPoints;
    }
}
