package bgu.spl.mics.application.objects;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages the fusion of sensor data for simultaneous localization and mapping (SLAM).
 * Combines data from multiple sensors (e.g., LiDAR, camera) to build and update a global map.
 * Implements the Singleton pattern to ensure a single instance of FusionSlam exists.
 */
public class FusionSlam {

    // Singleton instance holder
    private static class FusionSlamHolder {
        private static final FusionSlam INSTANCE = new FusionSlam();
    }

    // Retrieve the singleton instance
    public static FusionSlam getInstance() {
        return FusionSlamHolder.INSTANCE;
    }

    // Fields for managing SLAM data
    private final Map<String, LandMark> landmarks;
    private final List<Pose> poses;

    // Private constructor to enforce singleton pattern
    private FusionSlam() {
        this.landmarks = new ConcurrentHashMap<>();
        this.poses = Collections.synchronizedList(new ArrayList<>());
    }

    // Add a new pose to the system
    public void addPose(Pose pose) {
        synchronized (poses) {
            if (pose != null && !poses.contains(pose)) { // Check for duplicates
                poses.add(pose);
            }
        }
    }

    // Add or update a landmark based on tracked object data
    public void updateLandmark(String id, String description, List<CloudPoint> newCoordinates, Pose currentPose) {
        synchronized (landmarks) {
            LandMark landmark = landmarks.get(id);
            List<CloudPoint> transformedCoordinates = transformCoordinates(newCoordinates, currentPose);
            if (landmark == null) {
                landmarks.put(id, new LandMark(id, description, transformedCoordinates));
                StatisticalFolder.getInstance().incrementLandmarks(1);
            } else {
                landmark.setCoordinates(averageCoordinates(landmark.getCoordinates(), transformedCoordinates));
            }
        }
    }

    // Transform coordinates to global frame based on pose
    private List<CloudPoint> transformCoordinates(List<CloudPoint> points, Pose pose) {
        List<CloudPoint> transformed = new ArrayList<>();
        for (CloudPoint point : points) {
            double x = pose.getX() + point.getX() * Math.cos(Math.toRadians(pose.getYaw())) - point.getY() * Math.sin(Math.toRadians(pose.getYaw()));
            double y = pose.getY() + point.getX() * Math.sin(Math.toRadians(pose.getYaw())) + point.getY() * Math.cos(Math.toRadians(pose.getYaw()));
            transformed.add(new CloudPoint(x, y));
        }
        return transformed;
    }


    private List<CloudPoint> averageCoordinates(List<CloudPoint> existing, List<CloudPoint> newPoints) {
        List<CloudPoint> averaged = new ArrayList<>();
        int minSize = Math.min(existing.size(), newPoints.size());

        for (int i = 0; i < minSize; i++) {
            double avgX = (existing.get(i).getX() + newPoints.get(i).getX()) / 2;
            double avgY = (existing.get(i).getY() + newPoints.get(i).getY()) / 2;
            averaged.add(new CloudPoint(avgX, avgY));
        }

        // Handle leftover points (if needed)
        if (existing.size() > minSize) {
            averaged.addAll(existing.subList(minSize, existing.size()));
        } else if (newPoints.size() > minSize) {
            averaged.addAll(newPoints.subList(minSize, newPoints.size()));
        }

        return averaged;
    }


    // Retrieve all landmarks
    public Map<String, LandMark> getLandmarks() {
        return landmarks;
    }

    // Retrieve all poses
    public List<Pose> getPoses() {
        return poses;
    }
}

