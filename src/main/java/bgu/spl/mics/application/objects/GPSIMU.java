package bgu.spl.mics.application.objects;

import com.google.gson.Gson;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;

/**
 * GPSIMU represents the Global Positioning System and Inertial Measurement Unit module.
 * It tracks the robot's position and orientation and provides this data to other components.
 */
public class GPSIMU {

    private int currentTick; // Represents the current time in ticks.
    private final STATUS status;
    private final List<Pose> poseList; // A list of Pose objects representing time-stamped poses of the robot.


    /**
     * Constructor for GPSIMU.
     * Initializes the GPSIMU with default status and an empty pose list.
     */
    public GPSIMU(String filePath) {
        this.currentTick = 0;
        this.status = STATUS.UP;
        this.poseList = loadPoseList(filePath);
    }

    private List<Pose> loadPoseList(String filePath) {
        try (FileReader reader = new FileReader(filePath)) {
            Type type = new TypeToken<List<Pose>>() {}.getType();
            return new Gson().fromJson(JsonParser.parseReader(reader).getAsJsonArray(), type);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load GPSIMU data from file: " + filePath, e);
        }
    }


    /**
     * Sets the current tick of the GPSIMU.
     *
     * @param currentTick The new current tick.
     */
    public void setCurrentTick(int currentTick) {
        this.currentTick = currentTick;
    }

    /**
     * Gets the status of the GPSIMU.
     *
     * @return The current status.
     */
    public STATUS getStatus() {
        return status;
    }


    public int getLatestDetectionTime() {
        int maxTime = poseList.stream()
                .mapToInt(Pose::getTime)
                .max()
                .orElse(-1); // Return -1 if no detections are present
        return maxTime;
    }

    public Pose getPoseByTime(int timeTick) {
        return poseList.stream()
                .filter(pose -> pose.getTime() == timeTick)
                .findFirst()
                .orElse(null);
    }

}
