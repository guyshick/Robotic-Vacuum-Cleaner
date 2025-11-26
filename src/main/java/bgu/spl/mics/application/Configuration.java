package bgu.spl.mics.application;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class Configuration {

    @SerializedName("Cameras")
    private Cameras cameras;

    @SerializedName("LiDarWorkers")
    private LidarWorkers lidarWorkers;

    @SerializedName("poseJsonFile")
    private String poseJsonFile;

    @SerializedName("TickTime")
    private int tickTime;

    @SerializedName("Duration")
    private int duration;

    // Getters and Setters
    public Cameras getCameras() {
        return cameras;
    }

    public LidarWorkers getLidarWorkers() {
        return lidarWorkers;
    }

    public String getPoseJsonFile() {
        return poseJsonFile;
    }

    public int getTickTime() {
        return tickTime;
    }

    public int getDuration() {
        return duration;
    }

    // Nested class for Cameras
    public static class Cameras {
        @SerializedName("CamerasConfigurations")
        private List<CamerasConfiguration> camerasConfigurations;

        @SerializedName("camera_datas_path")
        private String cameraDatasPath;

        // Getters and Setters
        public List<CamerasConfiguration> getCamerasConfigurations() {
            return camerasConfigurations;
        }

        public String getCameraDatasPath() {
            return cameraDatasPath;
        }

        // Nested class for CamerasConfiguration
        public static class CamerasConfiguration {
            @SerializedName("id")
            private int id;

            @SerializedName("frequency")
            private int frequency;

            @SerializedName("camera_key")
            private String cameraKey;

            // Getters and Setters
            public int getId() {
                return id;
            }

            public void setId(int id) {
                this.id = id;
            }

            public int getFrequency() {
                return frequency;
            }

            public String getCameraKey() {
                return cameraKey;
            }
        }
    }

    // Nested class for LidarWorkers
    public static class LidarWorkers {
        @SerializedName("LidarConfigurations")
        private List<LidarConfiguration> lidarConfigurations;

        @SerializedName("lidars_data_path")
        private String lidarsDataPath;

        // Getters and Setters
        public List<LidarConfiguration> getLidarConfigurations() {
            return lidarConfigurations;
        }

        public String getLidarsDataPath() {
            return lidarsDataPath;
        }

        // Nested class for LidarConfiguration
        public static class LidarConfiguration {
            @SerializedName("id")
            private int id;

            @SerializedName("frequency")
            private int frequency;

            // Getters and Setters
            public int getId() {
                return id;
            }

            public void setId(int id) {
                this.id = id;
            }

            public int getFrequency() {
                return frequency;
            }
        }
    }
}
