package bgu.spl.mics.application.objects;

/**
 * Represents the robot's pose (position and orientation) in the environment.
 * Includes x, y coordinates and the yaw angle relative to a global coordinate system.
 * Invariants:
 * - Yaw is measured in radians (range: -π to π or equivalent wrapping).
 * - Time is a non-negative integer.
 * Parameters:
 * - x: The x-coordinate of the robot in the environment.
 * - y: The y-coordinate of the robot in the environment.
 * - yaw: The orientation angle of the robot relative to the global frame (in radians).
 * - time: The timestamp associated with this pose.
 */
public class Pose {
    private final float x; // X-coordinate of the robot
    private final float y; // Y-coordinate of the robot
    private final float yaw; // Orientation in radians
    private final int time; // Time associated with the pose

    /**
     * Constructs a new Pose instance.
     *
     * @param x The x-coordinate of the robot.
     * @param y The y-coordinate of the robot.
     * @param yaw The orientation angle in radians (range: -π to π).
     * @param time The timestamp of this pose (non-negative).
     * @throws IllegalArgumentException if time is negative.
     */
    public Pose(float x, float y, float yaw, int time) {
        if (time < 0) {
            throw new IllegalArgumentException("Time must be non-negative.");
        }
        this.x = x;
        this.y = y;
        this.yaw = yaw;
        this.time = time;
    }

    /**
     * @return The x-coordinate of the robot.
     */
    public float getX() {
        return x;
    }

    /**
     * @return The y-coordinate of the robot.
     */
    public float getY() {
        return y;
    }

    /**
     * @return The orientation angle in radians.
     */
    public float getYaw() {
        return yaw;
    }

    /**
     * @return The timestamp associated with this pose.
     */
    public int getTime() {
        return time;
    }

    @Override
    public String toString() {
        return "Pose{" +
                "x=" + x +
                ", y=" + y +
                ", yaw=" + yaw +
                ", time=" + time +
                '}';
    }

}
