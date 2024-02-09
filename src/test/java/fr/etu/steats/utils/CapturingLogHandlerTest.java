package fr.etu.steats.utils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.logging.Level;
import java.util.logging.LogRecord;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CapturingLogHandlerTest {

    private CapturingLogHandler capturingLogHandler;

    @BeforeEach
    public void setUp() {
        capturingLogHandler = new CapturingLogHandler();
    }

    @Test
    void testLogCapture() {
        LogRecord record = new LogRecord(Level.INFO, "Test message");
        capturingLogHandler.publish(record);

        assertEquals("Test message", capturingLogHandler.getCapturedData());
    }

    @Test
    void testReset() {
        LogRecord record = new LogRecord(Level.INFO, "Test message");
        capturingLogHandler.publish(record);

        capturingLogHandler.reset();
        assertEquals("", capturingLogHandler.getCapturedData());
    }

    @Test
    void testFlushAndClose() {
        capturingLogHandler.flush();
        capturingLogHandler.close();

        assertEquals("", capturingLogHandler.getCapturedData());
    }
}
