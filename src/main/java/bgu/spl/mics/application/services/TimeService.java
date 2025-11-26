package bgu.spl.mics.application.services;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.GurionRockRunner;
import bgu.spl.mics.application.messages.*;
import bgu.spl.mics.application.objects.StatisticalFolder;

/**
 * TimeService acts as the global timer for the system, broadcasting TickBroadcast messages
 * at regular intervals and controlling the simulation's duration.
 */
public class TimeService extends MicroService {

    private final int tickTimeMillis; // Store tick time in milliseconds
    private final int duration;
    /**
     * Constructor for TimeService.
     *
     * @param TickTime  The duration of each tick in milliseconds.
     * @param Duration  The total number of ticks before the service terminates.
     */

    public TimeService(int TickTime, int Duration) {
        super("TimeService");
        this.tickTimeMillis = TickTime * 1000; // Convert to milliseconds
        this.duration = Duration;
    }

    /**
     * Initializes the TimeService.
     * Starts broadcasting TickBroadcast messages and terminates after the specified duration.
     */
    @Override
    protected void initialize() {
        System.out.println(getName() + " initialized and ready.");

        // Subscribe to CrashedBroadcast
        subscribeBroadcast(CrashedBroadcast.class, crash -> {
            System.out.println(getName() + ": Received CrashedBroadcast from " + crash.getSource());
            System.out.println(getName() + ": Terminating due to crash.");
            terminate();
        });

        try {
            for (int currentTick = 1; currentTick <= duration ; currentTick++) {
                if (terminated || GurionRockRunner.hasCrashOccurred()) {
                    System.out.println(getName() + ": Termination flag detected. Exiting.");
                    break;
                }

                // Send the TickBroadcast
                try {
                    sendBroadcast(new TickBroadcast(currentTick));
                    StatisticalFolder.getInstance().incrementSystemRuntime(1);
                    System.out.println(getName() + " broadcasted tick: " + currentTick);
                } catch (Exception e) {
                    System.err.println(getName() + ": Error broadcasting TickBroadcast: " + e.getMessage());
                    break;
                }

                // Sleep for the tick interval
                try {
                    Thread.sleep(tickTimeMillis);
                } catch (InterruptedException e) {
                    System.out.println(getName() + ": Interrupted during sleep. Exiting.");
                    Thread.currentThread().interrupt(); // Restore interrupted status
                    break;
                }
            }
        } finally {
            try {
                sendBroadcast(new TerminatedBroadcast(getName()));
                System.out.println(getName() + " sending TerminatedBroadcast and terminating.");
            } catch (Exception e) {
                System.err.println(getName() + ": Error sending TerminatedBroadcast: " + e.getMessage());
            } finally {
                terminate();
                System.out.println(getName() + " has fully terminated.");
            }
        }
    }
}
