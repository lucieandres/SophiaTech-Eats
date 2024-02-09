package fr.etu.steats.utils;

import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import static fr.etu.steats.service.NotificationService.RESET;

/**
 * This class is used to format the log with the color of the user.
 */
public class LogFormatter extends Formatter {

    @Override
    public String format(LogRecord logRecord) {
        if (logRecord.getLevel() == Level.SEVERE) {
            return "\u001B[31m" + new Date(logRecord.getMillis()) + " [" + logRecord.getLevel() + "] : " + logRecord.getMessage() + RESET + "\n";
        } else if (logRecord.getLevel() instanceof UserLevel logLevel) {
            return logLevel.getColor() + new Date(logRecord.getMillis()) + " [" + logLevel.getName() + "] : " + logRecord.getMessage() + RESET + "\n";
        } else {
            return new Date(logRecord.getMillis()) + " [" + logRecord.getLevel() + "] : " + logRecord.getMessage() + "\n";
        }
    }
}