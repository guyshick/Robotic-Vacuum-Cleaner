package bgu.spl.mics.application.messages;

import bgu.spl.mics.Broadcast;

/**
 * A broadcast message sent to notify all MicroServices that a component has crashed.
 */
public class CrashedBroadcast implements Broadcast {

    private final String source;

    /**
     * Constructor for CrashedBroadcast.
     *
     * @param source The source of the crash signal (e.g., a specific service or component).
     */
    public CrashedBroadcast(String source) {
        this.source = source;
    }

    /**
     * Gets the source of the crash signal.
     *
     * @return The source as a string.
     */
    public String getSource() {
        return source;
    }

    @Override
    public String toString() {
        return "CrashedBroadcast{" +
                "source='" + source + '\'' +
                '}';
    }
}
