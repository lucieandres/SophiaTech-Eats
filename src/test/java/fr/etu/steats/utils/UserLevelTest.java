package fr.etu.steats.utils;

import org.junit.jupiter.api.Test;

import static fr.etu.steats.service.NotificationService.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class UserLevelTest {

    @Test
    void testUserLevelCreation() {
        // Test the creation of each UserLevel instance
        assertEquals("USER", UserLevel.USER.getName());
        assertEquals(800, UserLevel.USER.intValue());
        assertEquals(TEXT_BLUE, UserLevel.USER.getColor());

        assertEquals("DELIVERY MAN", UserLevel.DELIVERY_MAN.getName());
        assertEquals(800, UserLevel.DELIVERY_MAN.intValue());
        assertEquals(TEXT_GREEN, UserLevel.DELIVERY_MAN.getColor());

        assertEquals("RESTAURANT MANAGER", UserLevel.RESTAURANT_MANAGER.getName());
        assertEquals(800, UserLevel.RESTAURANT_MANAGER.intValue());
        assertEquals(TEXT_YELLOW, UserLevel.RESTAURANT_MANAGER.getColor());
    }

    @Test
    void testEqualityAndHashcode() {
        UserLevel userLevel1 = new UserLevel("USER", 800, TEXT_BLUE);
        UserLevel userLevel2 = new UserLevel("USER", 800, TEXT_BLUE);
        UserLevel differentUserLevel = new UserLevel("ADMIN", 900, TEXT_YELLOW);

        // Test equality with itself
        assertEquals(userLevel1, userLevel1);

        // Test equality with null
        assertNotEquals(null, userLevel1);

        // Test equality with different class
        assertNotEquals(userLevel1, new Object());

        // Test equality with same values
        assertEquals(userLevel1, userLevel2);

        // Test equality with different values
        assertNotEquals(userLevel1, differentUserLevel);

        // Test hashcode consistency with equals
        assertEquals(userLevel1.hashCode(), userLevel2.hashCode());
        assertNotEquals(userLevel1.hashCode(), differentUserLevel.hashCode());
    }
}
