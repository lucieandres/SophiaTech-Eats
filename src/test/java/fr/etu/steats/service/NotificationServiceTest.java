package fr.etu.steats.service;

import fr.etu.steats.utils.CapturingLogHandler;
import fr.etu.steats.utils.LoggerUtils;
import fr.etu.steats.utils.UserLevel;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class NotificationServiceTest {

    private CapturingLogHandler capturingLogHandler;

    @BeforeEach
    public void setUp() {
        capturingLogHandler = new CapturingLogHandler();
        LoggerUtils.setHandler(capturingLogHandler);
    }

    @AfterEach
    public void tearDown() {
        capturingLogHandler.reset();
        capturingLogHandler.close();
        LoggerUtils.removeHandler(capturingLogHandler);
    }

    public static Stream<UserLevel> provideUserLevels() {
        return Stream.of(UserLevel.USER, UserLevel.DELIVERY_MAN, UserLevel.RESTAURANT_MANAGER);
    }

    @ParameterizedTest
    @MethodSource("provideUserLevels")
    void testSendNotificationToUser(UserLevel userLevel) {
        NotificationService.sendNotificationToUser(userLevel, "This is a test notification message.");
        assertEquals("🔔 This is a test notification message.", capturingLogHandler.getCapturedData());
    }

    @ParameterizedTest
    @MethodSource("provideUserLevels")
    void testSendNotificationToUserWithUserName(UserLevel userLevel) {
        NotificationService.sendNotificationToUser(userLevel, "This is a test notification message.", "John Doe");
        assertEquals("(John Doe) 🔔 This is a test notification message.", capturingLogHandler.getCapturedData());
    }

    @Test
    void testErrorAtInitialization() {
        assertThrows(IllegalStateException.class, NotificationService::new);
    }
}
