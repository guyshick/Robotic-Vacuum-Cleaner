package bgu.spl.mics.application.messages;

import bgu.spl.mics.Event;
import bgu.spl.mics.application.objects.TrackedObject;

import java.util.List;

/**
 * TrackedObjectsEvent represents an event sent by a LiDAR worker to the Fusion-SLAM service.
 *
 * This event contains a list of TrackedObjects that have been processed by the LiDAR worker.
 */
public class TrackedObjectsEvent implements Event<Void> {

    private final List<TrackedObject> trackedObjects;

    /**
     * Constructor for TrackedObjectsEvent.
     *
     * @param trackedObjects The list of TrackedObjects processed by the LiDAR worker.
     */
    public TrackedObjectsEvent(List<TrackedObject> trackedObjects) {
        this.trackedObjects = trackedObjects;
    }

    /**
     * Gets the list of TrackedObjects associated with this event.
     *
     * @return The list of TrackedObjects.
     */
    public List<TrackedObject> getTrackedObjects() {
        return trackedObjects;
    }

    @Override
    public String toString() {
        return "TrackedObjectsEvent{" +
                "trackedObjects=" + trackedObjects +
                '}';
    }
}

