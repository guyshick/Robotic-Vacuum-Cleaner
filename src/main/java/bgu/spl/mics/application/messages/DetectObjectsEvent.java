package bgu.spl.mics.application.messages;

import bgu.spl.mics.Event;
import bgu.spl.mics.application.objects.StampedDetectedObjects;
/**
 * DetectObjectsEvent represents an event sent by the CameraService
 * to LiDAR workers, containing a list of detected objects.
 */
public class DetectObjectsEvent implements Event<Void> {

    private final StampedDetectedObjects detectedObjects;

    /**
     * Constructor for DetectObjectsEvent.
     *
     * @param detectedObjects The list of detected objects from the camera.
     */
    public DetectObjectsEvent(StampedDetectedObjects detectedObjects) {
        this.detectedObjects = detectedObjects;
    }

    /**
     * Gets the list of detected objects associated with this event.
     *
     * @return The list of detected objects.
     */
    public StampedDetectedObjects getDetectedObjects() {
        return detectedObjects;
    }

    @Override
    public String toString() {
        return "DetectObjectsEvent{" +
                "detectedObjects=" + detectedObjects +
                '}';
    }
}
