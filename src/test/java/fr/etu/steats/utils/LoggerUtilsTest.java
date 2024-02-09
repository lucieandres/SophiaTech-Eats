package fr.etu.steats.utils;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.logging.Handler;
import java.util.logging.Level;

import static fr.etu.steats.utils.LoggerUtils.LOGGER;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LoggerUtilsTest {

    private CapturingLogHandler capturingLogHandler;

    @BeforeEach
    public void setUp() {
        //Remove all handlers
        for (Handler handler : LOGGER.getHandlers()) {
            LOGGER.removeHandler(handler);
        }

        capturingLogHandler = new CapturingLogHandler();
        LOGGER.addHandler(capturingLogHandler);
    }

    @AfterEach
    public void tearDown() {
        capturingLogHandler.reset();
        capturingLogHandler.close();
        LOGGER.removeHandler(capturingLogHandler);
    }

    @Test
    void testNumberOfHandler() {
        assertEquals(1, LOGGER.getHandlers().length);
    }

    @Test
    void testLogInfo() {
        LoggerUtils.log(Level.INFO, "This is a test log message.");

        assertEquals("This is a test log message.", capturingLogHandler.getCapturedData());
    }

    @Test
    void testLogWarning() {
        LoggerUtils.log(Level.WARNING, "This is a test log message.");

        assertEquals("This is a test log message.", capturingLogHandler.getCapturedData());
    }

    @Test
    void testLogSevere() {
        LoggerUtils.log(Level.SEVERE, "This is a test log message.");

        assertEquals("This is a test log message.", capturingLogHandler.getCapturedData());
    }

    @Test
    void testLoggerInitialization() {
        assertThrows(IllegalStateException.class, LoggerUtils::new);
    }

    @Test
    void testRemoveHandler() {
        LoggerUtils.removeHandler(capturingLogHandler);
        LoggerUtils.log(Level.INFO, "This is a test log message.");
        assertEquals("", capturingLogHandler.getCapturedData());
    }
}
