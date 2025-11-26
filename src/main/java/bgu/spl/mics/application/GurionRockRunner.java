package bgu.spl.mics.application;

import bgu.spl.mics.MicroService;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import bgu.spl.mics.application.objects.*;
import bgu.spl.mics.application.services.*;

public class GurionRockRunner {

    private static volatile boolean crashOccurred = false;
    private static volatile String faultySensor = null;
    private static volatile String sensorType = null;
    private final Map<String, CameraService> camerasMap = new HashMap<>();
    private final Map<String, LiDarService> lidarWorkersMap = new HashMap<>();

    public static synchronized void reportCrash(String sensor, String type) {
        if (!crashOccurred) {
            crashOccurred = true;
            faultySensor = sensor;
            sensorType = type;
            System.out.println("Crash reported by: " + sensor);
        }
    }

    public static boolean hasCrashOccurred() {
        return crashOccurred;
    }

    public static String getFaultySensor() {
        return faultySensor;
    }

    public static String getFaultySensorType() {
        return sensorType;
    }

    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("Error: The path to the Configuration JSON File must be provided as an argument.");
            System.exit(1);
        }

        String configFilePath = args[0];
        String outputFilePath = new File(configFilePath).getParent() + "/output_file.json";

        try {
            Configuration config = loadConfiguration(configFilePath);
            new GurionRockRunner().startSimulation(config, configFilePath, outputFilePath);
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static Configuration loadConfiguration(String filePath) throws IOException {
        try (FileReader reader = new FileReader(filePath)) {
            Gson gson = new Gson();
            Configuration config = gson.fromJson(reader, Configuration.class);
            System.out.println("Parsed Configuration: " + new Gson().toJson(config));
            return config;
        } catch (Exception e) {
            throw new IOException("Failed to load configuration from file: " + filePath, e);
        }
    }

    public void startSimulation(Configuration config, String configFilePath, String outputFilePath) {
        FusionSlam fusionSlam = FusionSlam.getInstance();
        StatisticalFolder statistics = StatisticalFolder.getInstance();
        try {
            List<Camera> cameras = new ArrayList<>();
            List<LiDarWorkerTracker> lidarWorkers = new ArrayList<>();
            GPSIMU gpsimu = initializeSimulation(config, configFilePath, cameras, lidarWorkers);
            runSimulation(config, gpsimu, cameras, lidarWorkers);

            if (hasCrashOccurred()) {
                generateCrashOutput(fusionSlam, statistics, getFaultySensor(), getFaultySensorType() , outputFilePath);
            } else {
                generateOutput(fusionSlam, statistics, outputFilePath, null);
            }
        } catch (Exception e) {
            System.err.println("Unexpected error: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }



    private GPSIMU initializeSimulation(Configuration config, String configFilePath, List<Camera> cameras, List<LiDarWorkerTracker> lidarWorkers) {
        System.out.println("Initializing simulation...");
        String basePath = new File(configFilePath).getParent();

        // Resolve paths
        String absoluteCameraDataPath = new File(basePath, config.getCameras().getCameraDatasPath()).getAbsolutePath();
        String absoluteLidarDataPath = new File(basePath, config.getLidarWorkers().getLidarsDataPath()).getAbsolutePath();
        String absolutePoseJsonPath = new File(basePath, config.getPoseJsonFile()).getAbsolutePath();

        // Initialize Cameras
        config.getCameras().getCamerasConfigurations().forEach(cameraConfig -> {
            System.out.println("Initializing Camera with ID: " + cameraConfig.getId());
            Camera camera = new Camera(
                    cameraConfig.getId(),
                    cameraConfig.getFrequency(),
                    absoluteCameraDataPath,
                    cameraConfig.getCameraKey()
            );
            CameraService cameraService = new CameraService(camera);
            cameras.add(camera); // Collect all camera objects
            camerasMap.put("Camera-" + camera.getId(), cameraService);
        });

        // Initialize LiDAR Workers
        config.getLidarWorkers().getLidarConfigurations().forEach(lidarConfig -> {
            System.out.println("Initializing LiDAR with ID: " + lidarConfig.getId());
            LiDarDataBase lidarDB = LiDarDataBase.getInstance(absoluteLidarDataPath);
            LiDarWorkerTracker lidarWorker = new LiDarWorkerTracker(
                    lidarConfig.getId(),
                    lidarConfig.getFrequency(),
                    lidarDB
            );
            LiDarService lidarService = new LiDarService(lidarWorker);
            lidarWorkers.add(lidarWorker); // Collect all lidar worker objects
            lidarWorkersMap.put("LiDAR-" + lidarWorker.getId(), lidarService);
        });

        // Initialize GPSIMU
        System.out.println("Initializing GPSIMU...");
        return new GPSIMU(absolutePoseJsonPath);
    }



    private void runSimulation(Configuration config, GPSIMU gpsimu, List<Camera> cameras, List<LiDarWorkerTracker> lidarWorkers) {
        System.out.println("Running simulation...");

        try {
            // Determine minimum duration
            int latestTime = Math.max(
                    gpsimu.getLatestDetectionTime(),
                    Math.max(
                            cameras.stream().mapToInt(camera -> camera.getLatestDetectionTime() + camera.getFrequency()).max().orElse(0),
                            lidarWorkers.stream().mapToInt(lidar -> lidar.getLatestDetectionTime() + lidar.getFrequency()).max().orElse(0)
                    )
            );
            int duration = Math.min(config.getDuration(), latestTime);
            System.out.println("Simulation duration set to: " + duration);

            // Add services
            List<MicroService> services = new ArrayList<>();
            camerasMap.values().forEach(services::add);
            lidarWorkersMap.values().forEach(services::add);

            services.add(new PoseService(gpsimu));
            services.add(new FusionSlamService("FusionSlamService"));
            services.add(new TimeService(config.getTickTime(), duration));

            // Start threads
            List<Thread> threads = new ArrayList<>();
            services.forEach(service -> threads.add(new Thread(service)));
            threads.forEach(Thread::start);

            for (Thread thread : threads) {
                try {
                    thread.join();
                } catch (InterruptedException e) {
                    thread.interrupt();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void generateOutput(FusionSlam fusionSlam, StatisticalFolder statistics, String outputFilePath, Exception error) {
        JsonObject output = new JsonObject();
        if (error == null) {
            // Using StatisticalFolder instance directly for statistics
            output.addProperty("systemRuntime", statistics.getSystemRuntime());
            output.addProperty("numDetectedObjects", statistics.getNumDetectedObjects());
            output.addProperty("numTrackedObjects", statistics.getNumTrackedObjects());
            output.addProperty("numLandmarks", statistics.getNumLandmarks());
            output.add("landMarks", new Gson().toJsonTree(fusionSlam.getLandmarks()));
        } else {
            output.addProperty("Error", error.getMessage());
            output.addProperty("faultySensor", "Unknown");
        }

        // Log the statistics
        System.out.println("Simulation completed.");
        System.out.println(statistics); // Prints the entire StatisticalFolder's details to the console

        writeToFile(output, outputFilePath);
    }


    private void generateCrashOutput(FusionSlam fusionSlam, StatisticalFolder statistics, String source, String sensorType , String outputFilePath) {
        JsonObject output = new JsonObject();
        output.addProperty("error", sensorType + " disconnected");
        output.addProperty("faultySensor", source);

        // Add last frames
        output.add("lastFrames", captureLastFrames());

        // Add poses
        output.add("poses", new Gson().toJsonTree(fusionSlam.getPoses()));

        // Add statistics
        JsonObject statisticsJson = new JsonObject();
        statisticsJson.addProperty("systemRuntime", statistics.getSystemRuntime());
        statisticsJson.addProperty("numDetectedObjects", statistics.getNumDetectedObjects());
        statisticsJson.addProperty("numTrackedObjects", statistics.getNumTrackedObjects());
        statisticsJson.addProperty("numLandmarks", statistics.getNumLandmarks());
        statisticsJson.add("landMarks", new Gson().toJsonTree(fusionSlam.getLandmarks()));
        output.add("statistics", statisticsJson);

        writeToFile(output, outputFilePath);
    }


    private JsonObject captureLastFrames() {
        JsonObject lastFrames = new JsonObject();

        // Collect cameras' last frames
        JsonObject camerasFrames = new JsonObject();
        camerasMap.forEach((id, cameraService) -> {
            StampedDetectedObjects lastDetected = cameraService.camera.getLastDetectedObjects();
            if (lastDetected != null) {
                camerasFrames.add(id, new Gson().toJsonTree(lastDetected));
            }
        });

        // Collect LiDAR workers' last frames
        JsonObject lidarFrames = new JsonObject();
        lidarWorkersMap.forEach((id, lidarService) -> {
            List<TrackedObject> lastTracked = lidarService.workerTracker.getLastTrackedObjects();
            if (!lastTracked.isEmpty()) {
                lidarFrames.add(id, new Gson().toJsonTree(lastTracked));
            }
        });

        // Add cameras and LiDAR frames to lastFrames
        lastFrames.add("cameras", camerasFrames);
        lastFrames.add("lidar", lidarFrames);

        return lastFrames;
    }

    private void writeToFile(JsonObject output, String outputFilePath) {
        try (FileWriter writer = new FileWriter(outputFilePath)) {
            // Use GsonBuilder for pretty-printed JSON
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            gson.toJson(output, writer);
            System.out.println("Output written to: " + outputFilePath);
        } catch (IOException e) {
            System.err.println("Failed to write output file: " + e.getMessage());
        }
    }
}
