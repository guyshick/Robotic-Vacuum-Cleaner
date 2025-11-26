package bgu.spl.mics.application.services;

import bgu.spl.mics.*;
import bgu.spl.mics.application.GurionRockRunner;
import bgu.spl.mics.application.messages.*;
import bgu.spl.mics.application.objects.*;

import java.util.List;

/**
 * FusionSlamService processes sensor data for simultaneous localization and mapping (SLAM).
 * It integrates data from cameras, LiDAR, and pose sensors to build and update the robot's global map.
 */
public class FusionSlamService extends MicroService {

    private final FusionSlam fusionSlam;

    /**
     * Constructs a new FusionSlamService.
     *
     * @param name The name of the service.
     */
    public FusionSlamService(String name) {
        super(name);
        this.fusionSlam = FusionSlam.getInstance();
    }

    @Override
    protected void initialize() {
        // Subscribe to TrackedObjectsEvent
        subscribeEvent(TrackedObjectsEvent.class, event -> {
            List<TrackedObject> trackedObjects = event.getTrackedObjects();
                for (TrackedObject obj : trackedObjects) {
                    if(GurionRockRunner.hasCrashOccurred()){
                        return;
                    }
                    int detectionTime = obj.getTimestamp(); // Use the detection time from the tracked object itself
                    Pose poseAtDetectionTime = fusionSlam.getPoses().stream()
                        .filter(p -> p.getTime() == detectionTime)
                        .findFirst()
                        .orElse(null); // If pose does not exist, skip processing

                if (poseAtDetectionTime != null) {
                    fusionSlam.updateLandmark(
                            obj.getId(),
                            obj.getDescription(),
                            obj.getCoordinates(),
                            poseAtDetectionTime
                    );
                }
            }
        });

        // Subscribe to PoseEvent
        subscribeEvent(PoseEvent.class, event -> {
            Pose pose = event.getPose();
            fusionSlam.addPose(pose);
        });

        // Subscribe to TerminatedBroadcast
        subscribeBroadcast(TerminatedBroadcast.class, broadcast -> {
            if (broadcast.getSource().equals("TimeService")) {
                System.out.println(getName() + " received TerminatedBroadcast from TimeService. Terminating.");
                terminate();
            }
        });

        // Subscribe to CrashedBroadcast
        subscribeBroadcast(CrashedBroadcast.class, broadcast -> {
            terminate();
        });
    }
}
