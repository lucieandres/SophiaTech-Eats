package fr.etu.steats.restaurant;

import fr.etu.steats.order.OrderItem;
import org.joda.time.LocalTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static fr.etu.steats.restaurant.TimeSlot.TIME_SLOT_DURATION;
import static org.junit.jupiter.api.Assertions.*;

class TimeSlotTest {
    private Restaurant restaurant;
    private Menu menu;
    private TimeSlot timeSlot;
    private int capacity;

    @BeforeEach
    void setup() {
        capacity = 5;
        timeSlot = new TimeSlot(new LocalTime(), capacity);

        restaurant = new Restaurant("la pizza della mama", 1, "test", "1 rue de la paix");
        menu = new Menu("Pizza margharita", 12);
        restaurant.addMenuItem(menu);
    }

    @Test
    void testBaseCreationOfTimeSlot() {
        LocalTime noon = new LocalTime(12, 0, 0);
        TimeSlot timeSlot = new TimeSlot(noon, 5);
        assertEquals(5, timeSlot.getCapacity());
        assertTrue(noon.isEqual(timeSlot.getBeginningTime()));

        LocalTime expectedTime = noon.plusMinutes(TIME_SLOT_DURATION);
        assertTrue(expectedTime.isEqual(timeSlot.getFinishingTime()));
    }

    @Test
    void testIsOnGoing() {
        TimeSlot timeSlot = new TimeSlot(new LocalTime().minusMinutes(1), 5);
        assertTrue(timeSlot.isOnGoing());

        TimeSlot timeSlot2 = new TimeSlot(new LocalTime().plusMinutes(TIME_SLOT_DURATION + 1), 5);
        assertFalse(timeSlot2.isOnGoing());

        TimeSlot timeSlot3 = new TimeSlot(new LocalTime().minusMinutes(TIME_SLOT_DURATION + 1), 5);
        assertFalse(timeSlot3.isOnGoing());

        TimeSlot timeSlot4 = new TimeSlot(new LocalTime().plusMinutes(1), 5);
        assertFalse(timeSlot4.isOnGoing());

        TimeSlot timeSlot5 = new TimeSlot(LocalTime.now().minusMinutes(TIME_SLOT_DURATION - 1), 5);
        assertTrue(timeSlot5.isOnGoing());
    }

    @Test
    void testAddOrderToTimeSlot() {
        int capacity = 5;

        for (int i = 0; i < capacity; i++) {
            assertTrue(timeSlot.addOrderToTimeSlot(new OrderItem(menu, restaurant)));
        }

        assertEquals(capacity, timeSlot.getOrderToPrepareDuringTimeSlot().size());
        assertEquals(0, timeSlot.numberOfAvailablePlaceForItem());

        assertFalse(timeSlot.addOrderToTimeSlot(new OrderItem(menu, restaurant)));
        assertThrows(IllegalArgumentException.class, () -> timeSlot.addOrderToTimeSlot(null));
    }

    @Test
    void testNumberOfAvailablePlaceForItem() {
        assertEquals(capacity, timeSlot.numberOfAvailablePlaceForItem());

        for (int i = 0; i < capacity; i++) {
            timeSlot.addOrderToTimeSlot(new OrderItem(menu, restaurant));
            assertEquals(capacity - timeSlot.getOrderToPrepareDuringTimeSlot().size(), timeSlot.numberOfAvailablePlaceForItem());
        }

        assertEquals(0, timeSlot.numberOfAvailablePlaceForItem());
    }
}
