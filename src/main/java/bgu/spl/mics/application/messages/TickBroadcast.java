package bgu.spl.mics.application.messages;

import bgu.spl.mics.Broadcast;

/**
 * TickBroadcast is a message sent periodically by the TimeService
 * to indicate the passage of time (ticks) in the system.
 */
public class TickBroadcast implements Broadcast {

    private final int time;

    /**
     * Constructor for TickBroadcast.
     *
     * @param time The current tick time.
     */
    public TickBroadcast(int time) {
        this.time = time;
    }

    /**
     * Gets the current tick time.
     *
     * @return The current tick time.
     */
    public int getTime() {
        return time;
    }
}
