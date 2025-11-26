package bgu.spl.mics.application.services;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.GurionRockRunner;
import bgu.spl.mics.application.messages.*;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.objects.GPSIMU;
import bgu.spl.mics.application.objects.Pose;
import bgu.spl.mics.application.objects.STATUS;

/**
 * PoseService is responsible for managing the robot's pose (position and orientation) updates.
 * Responsibilities:
 * - Listens for TickBroadcast to update the current pose.
 * - Sends PoseEvent containing the updated pose.
 * - Interacts with the GPSIMU module to retrieve and manage pose data.
 */
public class PoseService extends MicroService {

    private final GPSIMU gpsimu;
    private final int terminationTime;


    /**
     * Constructor for PoseService.
     *
     * @param gpsimu The GPSIMU module used to manage pose data.
     */
    public PoseService(GPSIMU gpsimu) {
        super("PoseService");
        this.gpsimu = gpsimu;
        this.terminationTime = gpsimu.getLatestDetectionTime();

    }

    /**
     * Initializes the PoseService.
     * Subscribes to TickBroadcast and manages pose updates.
     */
    @Override
    protected void initialize() {
        System.out.println(getName() + " initialized.");

        // Subscribe to TickBroadcast for periodic updates
        subscribeBroadcast(TickBroadcast.class, tick -> {

            if (tick.getTime() > terminationTime) {
                sendBroadcast(new TerminatedBroadcast(this.getName()));
                terminate();
                return;
            }

            if (gpsimu.getStatus().equals(STATUS.ERROR)) {
                sendBroadcast(new CrashedBroadcast(this.getName()));
                GurionRockRunner.reportCrash("gpsimu", "gps"); // Register the crash in GurionRockPro
                terminate();
                return;
            }

            int currentTick = tick.getTime();
            gpsimu.setCurrentTick(currentTick);

            // Retrieve the current pose for the current tick
            Pose currentPose = gpsimu.getPoseByTime(currentTick);

            if (currentPose != null) {
                sendEvent(new PoseEvent(currentPose));
                System.out.println(getName() + " sent PoseEvent: " + currentPose);
            } else {
                System.err.println(getName() + " no pose available for tick: " + currentTick);
            }
        });

        subscribeBroadcast(TerminatedBroadcast.class, terminated -> {
            if (terminated.getSource().equals("TimeService")) {
                System.out.println(getName() + " received TerminatedBroadcast from TimeService. Terminating.");
                terminate();
            }
        });

        // Subscribe to CrashedBroadcast to handle crashes (e.g., cleanup)
        subscribeBroadcast(CrashedBroadcast.class, crashed -> {
            System.out.println("recieved CrashedBroadcast from " + crashed.getSource() + "Terminating.");
            terminate();
        });


        System.out.println(getName() + " is ready to process messages.");
    }
}

