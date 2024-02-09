package fr.etu.steats.utils;

import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.StreamHandler;

/**
 * This class is used to log using the java.util.logging.Logger.
 */
public class LoggerUtils {
    protected static final Logger LOGGER = Logger.getLogger(LoggerUtils.class.getName());

    LoggerUtils() {
        throw new IllegalStateException("Utility class cannot be instantiated");
    }

    public static void log(Level level, String message) {
        if (LOGGER.getHandlers().length == 0) {
            LOGGER.addHandler(new StreamHandler(System.out, new LogFormatter()));
        }
        LOGGER.setLevel(Level.ALL);
        LOGGER.setUseParentHandlers(false);
        LOGGER.log(level, message);
    }

    public static void setHandler(Handler handler) {
        LOGGER.addHandler(handler);
    }

    public static void removeHandler(Handler handler) {
        LOGGER.removeHandler(handler);
    }
}


