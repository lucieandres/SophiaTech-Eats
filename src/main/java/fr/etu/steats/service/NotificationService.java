package fr.etu.steats.service;

import fr.etu.steats.utils.LoggerUtils;
import fr.etu.steats.utils.UserLevel;

/**
 * This class represents a notification service.
 * It is used to send notifications to the users, delivery man and restaurant managers.
 * It is used to notify the users when their order is ready for example.
 */
public class NotificationService {
    public static final String TEXT_GREEN = "\u001B[32m";
    public static final String TEXT_YELLOW = "\u001B[33m";
    public static final String TEXT_BLUE = "\u001B[34m";
    public static final String RESET = "\u001B[0m";

    NotificationService() {
        throw new IllegalStateException("Utility class cannot be instantiated");
    }

    /**
     * This method is used to send a notification to a user.
     *
     * @param userLevel The type of user to send the notification to.
     * @param message   The message to send.
     */
    public static void sendNotificationToUser(UserLevel userLevel, String message) {
        LoggerUtils.log(userLevel, "🔔 " + message);
    }

    /**
     * This method is used to send a notification to a user with his name.
     *
     * @param userLevel The type of user to send the notification to.
     * @param message   The message to send.
     * @param userName  The name of the user to send the notification to.
     */
    public static void sendNotificationToUser(UserLevel userLevel, String message, String userName) {
        LoggerUtils.log(userLevel, "(" + userName + ")" + " 🔔 " + message);
    }
}
