package bgu.spl.mics.application.messages;

import bgu.spl.mics.Broadcast;

/**
 * A broadcast message sent to notify all MicroServices that the system is terminating.
 */
public class TerminatedBroadcast implements Broadcast {
    private final String source;

    /**
     * Constructor for TerminatedBroadcast.
     *
     * @param source The source of the termination signal (e.g., TimeService).
     */
    public TerminatedBroadcast(String source) {
        this.source = source;
    }

    /**
     * Gets the source of the termination signal.
     *
     * @return The source as a string.
     */
    public String getSource() {
        return source;
    }
}
