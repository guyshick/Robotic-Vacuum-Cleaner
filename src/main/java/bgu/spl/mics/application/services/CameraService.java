package bgu.spl.mics.application.services;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.GurionRockRunner;
import bgu.spl.mics.application.messages.*;
import bgu.spl.mics.application.objects.*;


/**
 * CameraService is responsible for processing data from the camera and
 * sending DetectObjectsEvents to LiDAR workers.
 * This service interacts with the Camera object to detect objects and updates
 * the system's StatisticalFolder upon sending its observations.
 */
public class CameraService extends MicroService {
    public final Camera camera;
    private final int terminationTime;
    private final int errorTime;

    /**
     * Constructor for CameraService.
     *
     * @param camera The Camera object that this service will use to detect objects.
     */
    public CameraService(Camera camera) {
        super("CameraService");
        this.camera = camera;
        this.terminationTime = camera.getLatestDetectionTime() + camera.getFrequency();
        this.errorTime = camera.hasError();
    }

    /**
     * Initializes the CameraService.
     * Registers the service to handle TickBroadcasts and sets up callbacks for sending
     * DetectObjectsEvents.
     */
    @Override
    protected void initialize() {
        System.out.println(getName() + " initialized.");

        // Subscribe to TickBroadcast to process periodic tasks
        subscribeBroadcast(TickBroadcast.class, tick -> {
            if (tick.getTime() > terminationTime) {
                camera.setStatus(STATUS.DOWN);
                sendBroadcast(new TerminatedBroadcast(this.getName()));
                terminate();
                return;
            }


            // Detect objects and send a single event with the entire list
            StampedDetectedObjects detectedObjects = camera.detectObjects(tick.getTime());
            if (detectedObjects!=null && !detectedObjects.getDetectedObjects().isEmpty()) {
                sendEvent(new DetectObjectsEvent(detectedObjects));
                StatisticalFolder.getInstance().incrementDetectedObjects(detectedObjects.getDetectedObjects().size());
                System.out.println(getName() + " sent DetectObjectsEvent with detected objects: " + detectedObjects.getDetectedObjects());
            }

            if(tick.getTime() == errorTime){
                sendBroadcast(new CrashedBroadcast(this.getName()));
                GurionRockRunner.reportCrash("Camera" + camera.getId(), "Camera"); // Register the crash in GurionRockPro
                terminate();
            }
        });

        // Subscribe to TerminatedBroadcast for clean termination
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
