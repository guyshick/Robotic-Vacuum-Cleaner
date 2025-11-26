package bgu.spl.mics.application.services;

import bgu.spl.mics.*;
import bgu.spl.mics.application.GurionRockRunner;
import bgu.spl.mics.application.messages.*;
import bgu.spl.mics.application.objects.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * LiDarService is responsible for processing data from the LiDAR sensor
 * and sending TrackedObjectsEvents to the FusionSLAM service.
 * Responsibilities:
 * - Processes DetectObjectsEvents.
 * - Subscribes to TickBroadcast for periodic updates.
 * - Interacts with the LiDarWorkerTracker to process LiDAR data.
 * - Sends TrackedObjectsEvents to FusionSLAM.
 */
public class LiDarService extends MicroService {

    public final LiDarWorkerTracker workerTracker;
    private final List<StampedDetectedObjects> receivedObjects;
    private final int terminationTime;
    private final int errorTime;
    private int currentTick;

    /**
     * Constructor for LiDarService.
     *
     * @param workerTracker The LiDarWorkerTracker object responsible for handling LiDAR data.
     */
    public LiDarService(LiDarWorkerTracker workerTracker) {
        super("LiDarService");
        this.workerTracker = workerTracker;
        this.receivedObjects = new ArrayList<>();
        this.terminationTime = workerTracker.getLatestDetectionTime();
        this.errorTime = workerTracker.hasError();
        this.currentTick = 0;
    }

    /**
     * Initializes the LiDarService.
     * Subscribes to TickBroadcast and DetectObjectsEvent, and sets up callbacks
     * for processing LiDAR data and sending TrackedObjectsEvents.
     */
    @Override
    protected void initialize() {
        System.out.println(getName() + " initialized.");

        // Subscribe to TickBroadcast for periodic updates
        subscribeBroadcast(TickBroadcast.class, tick -> {

            currentTick = tick.getTime();

            if(currentTick == errorTime){
                sendBroadcast(new CrashedBroadcast(this.getName()));
                GurionRockRunner.reportCrash("WorkerTracker" + workerTracker.getId(), "LidarWorkerTracker"); // Register the crash in GurionRockPro
                terminate();
                return;
            }

            if (currentTick > terminationTime) {
                // Process these objects and create tracked data
                List<TrackedObject> trackedObjects = receivedObjects.stream()
                        .flatMap(objects -> workerTracker.processData(objects, objects.getTime()).stream())
                        .collect(Collectors.toList());

                // Send TrackedObjectsEvent with the entire list
                if (!trackedObjects.isEmpty()) {
                    sendEvent(new TrackedObjectsEvent(trackedObjects));
                    StatisticalFolder.getInstance().incrementTrackedObjects(trackedObjects.size());
                    System.out.println(getName() + " sent TrackedObjectsEvent with data: " + trackedObjects);
                }
                sendBroadcast(new TerminatedBroadcast(this.getName()));
                terminate();
                return;
            }


            System.out.println(getName() + " received TickBroadcast at tick: " + currentTick);

            // Create a list of objects that need to be tracked now
            List<StampedDetectedObjects> toTrack = receivedObjects.stream()
                    .filter(obj -> obj.getTime() + workerTracker.getFrequency() <= currentTick)
                    .collect(Collectors.toList());


            // Remove the tracked objects from receivedObjects
            receivedObjects.removeAll(toTrack);

            // Process these objects and create tracked data
            List<TrackedObject> trackedObjects = toTrack.stream()
                    .flatMap(objects -> workerTracker.processData(objects, objects.getTime()).stream())
                    .collect(Collectors.toList());
            trackedObjects.forEach(trackedObject -> System.out.println("Tracked object: " + trackedObject));


            // Send TrackedObjectsEvent with the entire list
            if (!trackedObjects.isEmpty()) {
                sendEvent(new TrackedObjectsEvent(trackedObjects));
                StatisticalFolder.getInstance().incrementTrackedObjects(trackedObjects.size());
                System.out.println(getName() + " sent TrackedObjectsEvent with data: " + trackedObjects);
            }

        });

        // Subscribe to DetectObjectsEvent to process detected objects
        subscribeEvent(DetectObjectsEvent.class, event -> {
            System.out.println(getName() + " received DetectObjectsEvent with objects: " + event.getDetectedObjects().getDetectedObjects());
            // Store the objects in the received objects list
            receivedObjects.add(event.getDetectedObjects());
            List<StampedDetectedObjects> toTrack = receivedObjects.stream()
                    .filter(obj -> obj.getTime() + workerTracker.getFrequency() <= currentTick)
                    .collect(Collectors.toList());


            // Remove the tracked objects from receivedObjects
            receivedObjects.removeAll(toTrack);

            // Process these objects and create tracked data
            List<TrackedObject> trackedObjects = toTrack.stream()
                    .flatMap(objects -> workerTracker.processData(objects, objects.getTime()).stream())
                    .collect(Collectors.toList());
            trackedObjects.forEach(trackedObject -> System.out.println("Tracked object: " + trackedObject));


            // Send TrackedObjectsEvent with the entire list
            if (!trackedObjects.isEmpty()) {
                sendEvent(new TrackedObjectsEvent(trackedObjects));
                StatisticalFolder.getInstance().incrementTrackedObjects(trackedObjects.size());
                System.out.println(getName() + " sent TrackedObjectsEvent with data: " + trackedObjects);
            }
        });

        // Handle TerminatedBroadcast
        subscribeBroadcast(TerminatedBroadcast.class, terminated -> {
            if (terminated.getSource().equals("TimeService")) {
                System.out.println(getName() + " received TerminatedBroadcast from TimeService. Terminating.");
                terminate();
            }
        });

        // Handle CrashedBroadcast
        subscribeBroadcast(CrashedBroadcast.class, crashed -> {
            System.out.println(getName() + " received CrashedBroadcast from " + crashed.getSource() + ". Terminating.");
            terminate();
        });

        System.out.println(getName() + " is ready to process messages.");
    }
}
