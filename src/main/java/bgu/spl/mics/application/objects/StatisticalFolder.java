package bgu.spl.mics.application.objects;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Holds statistical information about the system's operation.
 * This class aggregates metrics such as the runtime of the system,
 * the number of objects detected and tracked, and the number of landmarks identified.
 */
public class StatisticalFolder {

    private final AtomicInteger systemRuntime; // Total runtime of the system in ticks
    private final AtomicInteger numDetectedObjects; // Number of objects detected by cameras
    private final AtomicInteger numTrackedObjects; // Number of objects tracked by LiDAR workers
    private final AtomicInteger numLandmarks; // Number of unique landmarks identified by Fusion-SLAM

    // Private constructor for Singleton
    private StatisticalFolder() {
        this.systemRuntime = new AtomicInteger(0);
        this.numDetectedObjects = new AtomicInteger(0);
        this.numTrackedObjects = new AtomicInteger(0);
        this.numLandmarks = new AtomicInteger(0);
    }

    // Bill Pugh Singleton Holder Pattern
    private static class Holder {
        private static final StatisticalFolder INSTANCE = new StatisticalFolder();
    }

    /**
     * Provides access to the singleton instance of StatisticalFolder.
     *
     * @return The singleton instance.
     */
    public static StatisticalFolder getInstance() {
        return Holder.INSTANCE;
    }

    /**
     * Increments the system runtime by a given number of ticks.
     *
     * @param ticks Number of ticks to increment.
     */
    public void incrementSystemRuntime(int ticks) {
        this.systemRuntime.addAndGet(ticks);
    }

    /**
     * Increments the number of detected objects.
     *
     * @param count Number of objects detected.
     */
    public void incrementDetectedObjects(int count) {
        this.numDetectedObjects.addAndGet(count);
    }

    /**
     * Increments the number of tracked objects.
     *
     * @param count Number of objects tracked.
     */
    public void incrementTrackedObjects(int count) {
        this.numTrackedObjects.addAndGet(count);
    }

    /**
     * Increments the number of landmarks identified.
     *
     * @param count Number of new landmarks identified.
     */
    public void incrementLandmarks(int count) {
        this.numLandmarks.addAndGet(count);
    }

    /**
     * Gets the total runtime of the system.
     *
     * @return The system runtime in ticks.
     */
    public int getSystemRuntime() {
        return systemRuntime.get();
    }

    /**
     * Gets the number of detected objects.
     *
     * @return The number of detected objects.
     */
    public int getNumDetectedObjects() {
        return numDetectedObjects.get();
    }

    /**
     * Gets the number of tracked objects.
     *
     * @return The number of tracked objects.
     */
    public int getNumTrackedObjects() {
        return numTrackedObjects.get();
    }

    /**
     * Gets the number of landmarks identified.
     *
     * @return The number of landmarks identified.
     */
    public int getNumLandmarks() {
        return numLandmarks.get();
    }

    @Override
    public String toString() {
        return "StatisticalFolder{" +
                "systemRuntime=" + systemRuntime +
                ", numDetectedObjects=" + numDetectedObjects +
                ", numTrackedObjects=" + numTrackedObjects +
                ", numLandmarks=" + numLandmarks +
                ", landmarks:" + FusionSlam.getInstance().getLandmarks() +
                '}';
    }
}
