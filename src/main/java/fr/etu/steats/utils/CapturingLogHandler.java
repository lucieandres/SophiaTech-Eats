package fr.etu.steats.utils;

import java.util.logging.LogRecord;
import java.util.logging.StreamHandler;

/**
 * This class implements a dummy log handler that allows capturing the log messages.
 * This allows checking the contents of the log messages in unit tests.
 */
public class CapturingLogHandler extends StreamHandler {
    private final StringBuilder buffer = new StringBuilder();

    @Override
    public synchronized void publish(final LogRecord logRecord) {
        this.buffer.append(logRecord.getMessage());
        super.publish(logRecord);
    }

    /**
     * Get the captured data from the messages
     *
     * @return messages as string
     */
    public String getCapturedData() {
        return this.buffer.toString();
    }

    /**
     * Reset the capture buffer
     */
    public void reset() {
        this.buffer.setLength(0);
        this.flush();
    }
}