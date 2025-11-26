package bgu.spl.mics.application.messages;

import bgu.spl.mics.Event;
import bgu.spl.mics.application.objects.Pose;

/**
 * PoseEvent represents an event containing the current pose of the robot.
 * It is sent by the PoseService to other components that require pose updates.
 */
public class PoseEvent implements Event<Void> {

    private final Pose pose;

    /**
     * Constructor for PoseEvent.
     *
     * @param pose The current pose of the robot.
     */
    public PoseEvent(Pose pose) {
        this.pose = pose;
    }

    /**
     * Gets the pose associated with this event.
     *
     * @return The current pose.
     */
    public Pose getPose() {
        return pose;
    }

    @Override
    public String toString() {
        return "PoseEvent{" +
                "pose=" + pose +
                '}';
    }
}

