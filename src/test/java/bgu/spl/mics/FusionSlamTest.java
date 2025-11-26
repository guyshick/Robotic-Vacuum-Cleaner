package bgu.spl.mics;

import bgu.spl.mics.application.objects.FusionSlam;
import bgu.spl.mics.application.objects.Pose;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class FusionSlamTest {

    private FusionSlam fusionSlam;

    @BeforeEach
    public void setUp() {
        fusionSlam = FusionSlam.getInstance(); // Reset for each test
    }

    @Test
    public void testAddPose() {
        Pose pose1 = new Pose(1.0f, 2.0f, 0.3f, 1); // Correct order
        Pose pose2 = new Pose(4.0f, 5.0f, 0.6f, 2); // Correct order

        fusionSlam.addPose(pose1);
        fusionSlam.addPose(pose2);

        List<Pose> poses = fusionSlam.getPoses();
        assertEquals(2, poses.size(), "Should contain two poses.");
        assertEquals(pose1, poses.get(0), "First pose should match.");
        assertEquals(pose2, poses.get(1), "Second pose should match.");
    }


    @Test
    public void testNoDuplicatePoses() {
        Pose pose = new Pose(1.0f, 2.0f, 0.3f, 1); // Correct order

        fusionSlam.addPose(pose);
        fusionSlam.addPose(pose); // Add duplicate

        List<Pose> poses = fusionSlam.getPoses();
        assertEquals(3, poses.size(), "Should not allow duplicate poses.");
    }

}
