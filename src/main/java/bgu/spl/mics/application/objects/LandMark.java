package bgu.spl.mics.application.objects;

import java.util.List;

/**
 * Represents a landmark in the environment map.
 * Landmarks are identified and updated by the FusionSlam service.
 */
public class LandMark {

    // Unique identifier for the landmark
    private final String id;

    // Description of the landmark
    private final String description;

    // List of coordinates representing the landmark's global position
    private List<CloudPoint> coordinates;

    /**
     * Constructor to initialize a new landmark.
     *
     * @param id          The unique identifier of the landmark.
     * @param description A description of the landmark.
     * @param coordinates The initial coordinates of the landmark.
     */
    public LandMark(String id, String description, List<CloudPoint> coordinates) {
        this.id = id;
        this.description = description;
        this.coordinates = coordinates;
    }

    /**
     * Get the unique identifier of the landmark.
     *
     * @return The landmark ID.
     */
    public String getId() {
        return id;
    }

    /**
     * Get the description of the landmark.
     *
     * @return The landmark description.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Get the coordinates of the landmark.
     *
     * @return A list of CloudPoint objects representing the coordinates.
     */
    public List<CloudPoint> getCoordinates() {
        return coordinates;
    }

    /**
     * Set the coordinates of the landmark.
     *
     * @param coordinates A list of CloudPoint objects representing the updated coordinates.
     */
    public void setCoordinates(List<CloudPoint> coordinates) {
        this.coordinates = coordinates;
    }


    @Override
    public String toString() {
        StringBuilder coordinatesString = new StringBuilder("[");
        for (int i = 0; i < coordinates.size(); i++) {
            CloudPoint point = coordinates.get(i);
            coordinatesString.append("{\"x\":").append(point.getX()).append(",\"y\":").append(point.getY()).append("}");
            if (i < coordinates.size() - 1) {
                coordinatesString.append(",");
            }
        }
        coordinatesString.append("]");

        return "{\"id\":\"" + id + "\","
                + "\"description\":\"" + description + "\","
                + "\"coordinates\":" + coordinatesString + "}";
    }
}

