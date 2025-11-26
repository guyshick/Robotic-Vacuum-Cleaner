package bgu.spl.mics;

import bgu.spl.mics.application.objects.Camera;
import bgu.spl.mics.application.objects.DetectedObject;
import bgu.spl.mics.application.objects.StampedDetectedObjects;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class CameraTest {

    private Camera camera;

    @BeforeEach
    public void setUp() {
        // Preconditions
        assert camera == null : "Camera instance should not be initialized before setUp.";

        camera = new Camera(1, 5); // Initialize with id 1 and frequency 5

        // Postconditions
        assert camera != null : "Camera instance should be initialized.";
        assert camera.getId() == 1 : "Camera ID should be correctly initialized.";
        assert camera.getFrequency() == 5 : "Camera frequency should be correctly initialized.";

    }

    @Test
    public void testAddDetectedObjects() {
        // Preconditions
        assert camera != null : "Camera instance must be initialized.";
        assert camera.getLastDetectedObjects() == null : "Last detected objects should initially be null.";

        // Add first detected object
        List<DetectedObject> objects1 = new ArrayList<>();
        objects1.add(new DetectedObject("Wall_1", "Wall"));
        StampedDetectedObjects firstDetection = new StampedDetectedObjects(2, objects1);
        camera.addDetectedObjects(firstDetection);

        // Postconditions
        StampedDetectedObjects lastDetected = camera.getLastDetectedObjects();
        assertNotNull(lastDetected, "Last detected objects should not be null after adding.");
        assertEquals(firstDetection, lastDetected, "Last detected objects should match the added detection.");

        // Invariant
        assert lastDetected.getTime() == 2 : "Last detected time should match the added detection.";
        assert lastDetected.getDetectedObjects().size() == 1 : "Detected objects list size should match.";

        // Add another detected object
        List<DetectedObject> objects2 = new ArrayList<>();
        objects2.add(new DetectedObject("Chair_Base_1", "Chair Base"));
        StampedDetectedObjects secondDetection = new StampedDetectedObjects(5, objects2);
        camera.addDetectedObjects(secondDetection);

        // Postconditions
        lastDetected = camera.getLastDetectedObjects();
        assertNotNull(lastDetected, "Last detected objects should not be null after adding.");
        assertEquals(secondDetection, lastDetected, "Last detected objects should match the most recently added detection.");

        // Invariant
        assert lastDetected.getTime() == 5 : "Last detected time should match the most recently added detection.";
        assert lastDetected.getDetectedObjects().size() == 1 : "Detected objects list size should match.";
    }

    @Test
    public void testEmptyLastDetectedObjects() {
        // Preconditions
        assert camera != null : "Camera instance must be initialized.";

        // Verify that last detected objects is initially null
        StampedDetectedObjects lastDetected = camera.getLastDetectedObjects();
        assertNull(lastDetected, "Last detected objects should be null initially.");

        // Invariant
        assert lastDetected == null : "Last detected objects should remain null if nothing is added.";
    }

    @Test
    public void testLastDetectedObjectsAfterMultipleAdditions() {
        // Preconditions
        assert camera != null : "Camera instance must be initialized.";

        // Add multiple detected objects
        List<DetectedObject> objects1 = new ArrayList<>();
        objects1.add(new DetectedObject("Wall_1", "Wall"));
        StampedDetectedObjects firstDetection = new StampedDetectedObjects(2, objects1);
        camera.addDetectedObjects(firstDetection);

        List<DetectedObject> objects2 = new ArrayList<>();
        objects2.add(new DetectedObject("Circular_Base_1", "Circular Base"));
        StampedDetectedObjects secondDetection = new StampedDetectedObjects(4, objects2);
        camera.addDetectedObjects(secondDetection);

        List<DetectedObject> objects3 = new ArrayList<>();
        objects3.add(new DetectedObject("Chair_Base_1", "Chair Base"));
        StampedDetectedObjects thirdDetection = new StampedDetectedObjects(6, objects3);
        camera.addDetectedObjects(thirdDetection);

        // Verify that the last detected objects match the most recent addition
        StampedDetectedObjects lastDetected = camera.getLastDetectedObjects();
        assertNotNull(lastDetected, "Last detected objects should not be null after multiple additions.");
        assertEquals(thirdDetection, lastDetected, "Last detected objects should match the most recently added detection.");

        // Invariant
        assert lastDetected.getTime() == 6 : "Last detected time should match the most recently added detection.";
        assert lastDetected.getDetectedObjects().size() == 1 : "Detected objects list size should match.";
    }
}
