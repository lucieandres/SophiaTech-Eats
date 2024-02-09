package fr.etu.steats.restaurant;

import fr.etu.steats.enums.EOrderStatus;
import fr.etu.steats.exception.UnauthorizedOperationException;
import fr.etu.steats.order.OrderItem;
import org.joda.time.DateTime;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

class TimeSlotManagerTest {
    private TimeSlotManager timeSlotManager;
    private Restaurant restaurant;
    private Menu menu;

    @BeforeEach
    void setup() {
        timeSlotManager = spy(TimeSlotManager.class);

        restaurant = new Restaurant("la pizza della mama", 1, "test", "1 rue de la paix");
        menu = new Menu("Pizza margharita", 12);
        restaurant.addMenuItem(menu);
    }

    @Test
    void testBaseCreationOfTimeSlotManager() {
        assertEquals(5, timeSlotManager.getCapacity());
        assertTrue(new LocalTime(8, 0, 0).isEqual(timeSlotManager.getOpeningTime()));
        assertTrue(new LocalTime(20, 0, 0).isEqual(timeSlotManager.getClosingTime()));
        assertEquals(0, timeSlotManager.getSlots().size());
    }

    @Test
    void testSetOpeningTime() {
        assertTrue(new LocalTime(8, 0, 0).isEqual(timeSlotManager.getOpeningTime()));
        timeSlotManager.setOpeningTime(new LocalTime(7, 0, 0));
        assertTrue(new LocalTime(7, 0, 0).isEqual(timeSlotManager.getOpeningTime()));
        timeSlotManager.setOpeningTime(new LocalTime(21, 0, 0));
        assertTrue(new LocalTime(21, 0, 0).isEqual(timeSlotManager.getOpeningTime()));
        timeSlotManager.setOpeningTime(new LocalTime(21, 0, 12));
        assertTrue(new LocalTime(21, 0, 0).isEqual(timeSlotManager.getOpeningTime()));
        assertThrows(IllegalArgumentException.class, () -> timeSlotManager.setOpeningTime(null));
    }

    @Test
    void testSetClosingTime() {
        assertTrue(new LocalTime(20, 0, 0).isEqual(timeSlotManager.getClosingTime()));
        timeSlotManager.setClosingTime(new LocalTime(21, 0, 0));
        assertTrue(new LocalTime(21, 0, 0).isEqual(timeSlotManager.getClosingTime()));
        timeSlotManager.setClosingTime(new LocalTime(21, 0, 12));
        assertTrue(new LocalTime(21, 0, 0).isEqual(timeSlotManager.getClosingTime()));
        timeSlotManager.setClosingTime(new LocalTime(7, 0, 0));
        assertTrue(new LocalTime(7, 0, 0).isEqual(timeSlotManager.getClosingTime()));
        assertThrows(IllegalArgumentException.class, () -> timeSlotManager.setClosingTime(null));
    }

    @Test
    void testCanOrderBePreparedBeforeDeadline() throws UnauthorizedOperationException {
        //The error case
        assertThrows(UnauthorizedOperationException.class, () -> timeSlotManager.canOrderBePreparedBeforeDeadline(new ArrayList<>(), null));
        assertThrows(UnauthorizedOperationException.class, () -> timeSlotManager.canOrderBePreparedBeforeDeadline(new ArrayList<>(), new DateTime().minusSeconds(1)));
        DateTime deliveryDate = timeSlotManager.getClosingTime().plusMinutes(1).toDateTimeToday();
        assertThrows(UnauthorizedOperationException.class, () -> timeSlotManager.canOrderBePreparedBeforeDeadline(new ArrayList<>(), deliveryDate));
        DateTime deliveryDate2 = timeSlotManager.getOpeningTime().minusMinutes(1).toDateTimeToday();
        assertThrows(UnauthorizedOperationException.class, () -> timeSlotManager.canOrderBePreparedBeforeDeadline(new ArrayList<>(), deliveryDate2));

        //deliveryDate3 = tomorrow at noon
        DateTime deliveryDate3 = new DateTime().plusDays(1).withHourOfDay(12).withMinuteOfHour(0).withSecondOfMinute(0);

        //We give 1 time slot with a capacity of 1
        when(timeSlotManager.getTimeSlotForOrder(deliveryDate3)).thenReturn(List.of(new TimeSlot(LocalTime.now(), 1)));

        //We ask if we can place 1 order item
        assertTrue(timeSlotManager.canOrderBePreparedBeforeDeadline(List.of(new OrderItem(menu, restaurant)), deliveryDate3));

        //We ask if we can place 2 order item
        assertFalse(timeSlotManager.canOrderBePreparedBeforeDeadline(List.of(new OrderItem(menu, restaurant), new OrderItem(menu, restaurant)), deliveryDate3));

        //We give 0 time slot with a capacity of 1
        when(timeSlotManager.getTimeSlotForOrder(deliveryDate3)).thenReturn(null);

        //We ask if we can place 1 order item
        assertFalse(timeSlotManager.canOrderBePreparedBeforeDeadline(List.of(new OrderItem(menu, restaurant)), deliveryDate3));

        //We give 0 time slot with a capacity of 1
        when(timeSlotManager.getTimeSlotForOrder(deliveryDate3)).thenReturn(new ArrayList<>());

        //We ask if we can place 1 order item
        assertFalse(timeSlotManager.canOrderBePreparedBeforeDeadline(List.of(new OrderItem(menu, restaurant)), deliveryDate3));
    }

    @Test
    void testAddOrderToPrepareWithDeadline() throws UnauthorizedOperationException {
        //deliveryDate = tomorrow at noon
        DateTime deliveryDate = new DateTime().plusDays(1).withTime(new LocalTime(12, 0, 0));
        //deliveryDate2 = today at 11h50
        LocalTime deliveryDate2 = new LocalTime(11, 50, 0);
        List<OrderItem> items = List.of(new OrderItem(menu, restaurant));
        TimeSlot timeSlot = new TimeSlot(deliveryDate2, 1);

        when(timeSlotManager.canOrderBePreparedBeforeDeadline(items, deliveryDate)).thenReturn(true);
        when(timeSlotManager.getTimeSlotForOrder(deliveryDate)).thenReturn(List.of(timeSlot));

        assertTrue(timeSlotManager.addOrderToPrepareWithDeadline(items, deliveryDate));
        assertEquals(0, timeSlot.numberOfAvailablePlaceForItem());
        assertEquals(1, timeSlot.getOrderToPrepareDuringTimeSlot().size());

        items = List.of(new OrderItem(menu, restaurant));
        when(timeSlotManager.canOrderBePreparedBeforeDeadline(items, deliveryDate)).thenReturn(false);
        assertFalse(timeSlotManager.addOrderToPrepareWithDeadline(items, deliveryDate));
    }

    @Test
    void testGetTimeSlotForOrder() {
        //deliveryDate = tomorrow at noon
        DateTime deliveryDateNoon = new DateTime().plusDays(1).withTime(new LocalTime(12, 0, 0));
        List<DateTime> datesNoon = List.of(
                deliveryDateNoon.minusMinutes(10),
                deliveryDateNoon.minusMinutes(20),
                deliveryDateNoon.minusMinutes(30)
        );
        when(timeSlotManager.getPreviousSlotDate(new LocalTime(12, 0, 0).toDateTimeToday().toLocalDateTime(), deliveryDateNoon)).thenReturn(datesNoon);
        List<TimeSlot> timeSlotsNoon = timeSlotManager.getTimeSlotForOrder(deliveryDateNoon);
        assertEquals(3, timeSlotsNoon.size());
        assertTrue(new LocalTime(12, 0, 0).minusMinutes(10).isEqual(timeSlotsNoon.get(0).getBeginningTime()));
        assertTrue(new LocalTime(12, 0, 0).minusMinutes(20).isEqual(timeSlotsNoon.get(1).getBeginningTime()));
        assertTrue(new LocalTime(12, 0, 0).minusMinutes(30).isEqual(timeSlotsNoon.get(2).getBeginningTime()));

        //deliveryDate = tomorrow at midnight
        DateTime deliveryDateMidnight = LocalTime.MIDNIGHT.toDateTimeToday().plusDays(1);
        List<DateTime> datesMidnight = List.of(
                new LocalTime(17, 50, 0).toDateTimeToday(),
                new LocalTime(17, 40, 0).toDateTimeToday(),
                new LocalTime(17, 30, 0).toDateTimeToday()
        );
        when(timeSlotManager.getPreviousSlotDate(new LocalTime(20, 0, 0).toDateTimeToday().toLocalDateTime(), deliveryDateMidnight)).thenReturn(datesMidnight);

        List<TimeSlot> timeSlotsMidnight = timeSlotManager.getTimeSlotForOrder(deliveryDateMidnight);
        assertEquals(3, timeSlotsMidnight.size());
        assertTrue(new LocalTime(17, 50, 0).isEqual(timeSlotsMidnight.get(0).getBeginningTime()));
        assertTrue(new LocalTime(17, 40, 0).isEqual(timeSlotsMidnight.get(1).getBeginningTime()));
        assertTrue(new LocalTime(17, 30, 0).isEqual(timeSlotsMidnight.get(2).getBeginningTime()));
    }

    @Test
    void testGetPreviousSlotDate() {
        assertTrue(timeSlotManager.getPreviousSlotDate(new LocalTime(23, 59, 59).toDateTimeToday().toLocalDateTime().plusDays(1), null).isEmpty());
        DateTime tomorrow = new DateTime().plusDays(1).withTime(new LocalTime(16, 0, 0));
        LocalDateTime preparationTime = new LocalTime(16, 0, 0).toDateTimeToday().toLocalDateTime();

        List<DateTime> probablyAvailableSlots = timeSlotManager.getPreviousSlotDate(preparationTime, tomorrow);
        assertEquals(3, probablyAvailableSlots.size());
        assertEquals(tomorrow.minusMinutes(10), probablyAvailableSlots.get(0));
        assertEquals(tomorrow.minusMinutes(20), probablyAvailableSlots.get(1));
        assertEquals(tomorrow.minusMinutes(30), probablyAvailableSlots.get(2));
    }

    @Test
    void testGetNearestTimeSlotWithWorkRemaining() throws UnauthorizedOperationException {
        assertNull(timeSlotManager.getNeareastTimeSlotWithWorkRemaining());

        DateTime tomorrow = new DateTime().plusDays(1).withTime(new LocalTime(16, 0, 0));

        List<OrderItem> items = List.of(
                new OrderItem(menu, restaurant),
                new OrderItem(menu, restaurant)
        );

        items.forEach(item -> item.setStatus(EOrderStatus.IN_PREPARATION));

        when(timeSlotManager.canOrderBePreparedBeforeDeadline(items, tomorrow)).thenReturn(true);

        assertTrue(timeSlotManager.addOrderToPrepareWithDeadline(items, tomorrow));

        TimeSlot timeSlot = timeSlotManager.getNeareastTimeSlotWithWorkRemaining();

        assertNotNull(timeSlot);

        assertEquals(3, timeSlot.numberOfAvailablePlaceForItem());
        assertEquals(2, timeSlot.getOrderToPrepareDuringTimeSlot().size());
    }
}
