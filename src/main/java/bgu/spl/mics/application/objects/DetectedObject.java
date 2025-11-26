package bgu.spl.mics.application.objects;

/**
 * DetectedObject represents an object detected by the camera.
 * It contains information such as the object's ID and description.
 */
public class DetectedObject {
    private final String id;
    private final String description;

    /**
     * Constructor for DetectedObject.
     *
     * @param id          The unique identifier of the object.
     * @param description A description of the object.
     */
    public DetectedObject(String id, String description) {
        this.id = id;
        this.description = description;
    }

    /**
     * Gets the ID of the detected object.
     *
     * @return The ID of the object.
     */
    public String getId() {
        return id;
    }

    /**
     * Gets the description of the detected object.
     *
     * @return The description of the object.
     */
    public String getDescription() {
        return description;
    }


    @Override
    public String toString() {
        return "DetectedObject{" +
                "id='" + id + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}

