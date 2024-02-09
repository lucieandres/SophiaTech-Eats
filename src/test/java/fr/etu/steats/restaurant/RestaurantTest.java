package fr.etu.steats.restaurant;

import fr.etu.steats.enums.EOrderStatus;
import fr.etu.steats.exception.UnauthorizedOperationException;
import fr.etu.steats.order.OrderItem;
import fr.etu.steats.utils.Scheduler;
import org.joda.time.DateTime;
import org.joda.time.LocalTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RestaurantTest {
    private Restaurant restaurant;
    private Menu menu1;

    @BeforeEach
    public void setUp() {
        menu1 = new Menu("Steak au poivre", 25.0);
        restaurant = new Restaurant("Chez Pierre", 1, "test", "1 rue de la paix");
        restaurant.addMenuItem(menu1);
        restaurant.addMenuItem(new Menu("Salade César", 10.0));
        restaurant.addMenuItem(new Menu("Poulet rôti", 20.0));
    }

    @Test
    void testAddMenuItem() {
        Menu newItem = new Menu("Pasta Carbonara", 15.0);
        restaurant.addMenuItem(newItem);

        List<Menu> menus = restaurant.getMenuItems();
        assertTrue(menus.stream().anyMatch(menu -> menu.getName().equals("Pasta Carbonara")));
        assertThrows(IllegalArgumentException.class, () -> restaurant.addMenuItem(newItem));
    }

    @Test
    void testAddNullMenuItem() {
        assertThrows(IllegalArgumentException.class, () -> restaurant.addMenuItem(null));
    }

    @Test
    void testRemoveMenuItem() {
        restaurant.removeMenuItem("Steak au poivre");

        List<Menu> menus = restaurant.getMenuItems();
        assertFalse(menus.stream().anyMatch(menu -> menu.getName().equals("Steak au poivre")));
    }

    @Test
    void testRemoveInvalidMenuItem() {
        assertThrows(IllegalArgumentException.class, () -> restaurant.removeMenuItem(""));
        assertThrows(IllegalArgumentException.class, () -> restaurant.removeMenuItem(null));

    }

    @Test
    void testGetMenuItem() {
        Menu menuItem = restaurant.getMenuItem("Poulet rôti");
        assertNotNull(menuItem);
        assertEquals("Poulet rôti", menuItem.getName());
    }

    @Test
    void testUpdateMenuItem() {
        Menu updatedItem = new Menu("Steak poivre", 27.0);
        restaurant.updateMenuItem("Steak au poivre", updatedItem);

        Menu menuItem = restaurant.getMenuItem("Steak poivre");
        assertEquals(27.0, menuItem.getGlobalPrice());

        assertThrows(IllegalArgumentException.class, () -> restaurant.updateMenuItem(null, null));
    }

    @Test
    void testRemoveOrder() throws UnauthorizedOperationException {
        OrderItem order = new OrderItem(menu1, restaurant);
        order.setStatus(EOrderStatus.IN_PREPARATION);
        assertTrue(restaurant.getTimeSlotManager().getSlots().isEmpty());
        restaurant.addOrderListWhoNeedToBePrepareBeforeDeadline(List.of(order), new DateTime().plusDays(1).withTime(new LocalTime(12, 0, 0)));
        assertNotNull(restaurant.getTimeSlotManager().getNeareastTimeSlotWithWorkRemaining());
        assertEquals(1, restaurant.getOrders().size());
        restaurant.cancelOrder(1);
        assertNull(restaurant.getTimeSlotManager().getNeareastTimeSlotWithWorkRemaining());
        assertTrue(restaurant.getOrders().isEmpty());
    }

    @Test
    void testRemoveIncorrectOrder() {
        OrderItem orderItem = new OrderItem(menu1, restaurant);
        assertThrows(IllegalArgumentException.class, () -> restaurant.removeOrder(null));
        assertThrows(IllegalArgumentException.class, () -> restaurant.removeOrder(orderItem));
    }

    @Test
    void testSetName() {
        restaurant.setName("Chez Paul");
        assertEquals("Chez Paul", restaurant.getName());
        assertThrows(IllegalArgumentException.class, () -> restaurant.setName(null));
        assertThrows(IllegalArgumentException.class, () -> restaurant.setName(""));
    }

    @Test
    void testEquals() {
        assertEquals(restaurant, new Restaurant("Chez Pierre", 1, "test", "1 rue de la paix"));
        assertNotEquals(null, restaurant);
        assertNotEquals(restaurant, new Object());
        assertNotEquals(restaurant, new Restaurant("Chez Pierre", 2, "test", "1 rue de la paix"));
    }

    @Test
    void testPrepareOrder() throws UnauthorizedOperationException, InterruptedException {
        Scheduler scheduler = spy(new Scheduler());
        restaurant = new Restaurant("Chez Pierre", 1, "test", scheduler, "1 rue de la paix");
        restaurant.addMenuItem(menu1);
        OrderItem order = new OrderItem(menu1, restaurant);
        restaurant.addOrderListWhoNeedToBePrepareBeforeDeadline(List.of(order), new DateTime().plusDays(1).withHourOfDay(12).withMinuteOfHour(0).withSecondOfMinute(0));
        assertEquals(restaurant.getOrders().get(0), order);
        doNothing().when(scheduler).waitTenMinutes();
        restaurant.prepareOrder(1);
    }

    @Test
    void testVerifyOrder() {
        Exception exception;

        // Null order
        exception = assertThrows(IllegalArgumentException.class, () -> restaurant.verifyOrder(null));
        assertEquals("Order cannot be null", exception.getMessage());

        // Order does not exist
        OrderItem order = new OrderItem(menu1, restaurant);
        exception = assertThrows(IllegalArgumentException.class, () -> restaurant.verifyOrder(order));
        assertEquals("Order does not exist", exception.getMessage());

        // Order does not belong to this restaurant
        OrderItem order2 = new OrderItem(new Menu("Kebab", 15), restaurant);
        restaurant.addOrder(order2);
        exception = assertThrows(IllegalArgumentException.class, () -> restaurant.verifyOrder(order2));
        assertEquals("The order does not belong to this restaurant", exception.getMessage());

        // Order does not have a menu
        OrderItem order3 = spy(new OrderItem(menu1, restaurant));
        restaurant.addOrder(order3);
        when(order3.getMenu()).thenReturn(menu1).thenReturn(menu1).thenReturn(null);
        exception = assertThrows(IllegalArgumentException.class, () -> restaurant.verifyOrder(order3));
        assertEquals("The order does not have a menu", exception.getMessage());

        // Order does not belong to this restaurant
        OrderItem order4 = new OrderItem(menu1, new Restaurant("Chez Paul", 2, "test", "1 rue de la paix"));
        restaurant.addOrder(order4);
        exception = assertThrows(IllegalArgumentException.class, () -> restaurant.verifyOrder(order4));
        assertEquals("The order does not belong to this restaurant", exception.getMessage());
    }

    @Test
    void testAddOrder() {
        OrderItem order = new OrderItem(menu1, restaurant);
        restaurant.addOrder(order);
        assertEquals(1, restaurant.getOrders().size());
        assertThrows(IllegalArgumentException.class, () -> restaurant.addOrder(null));
        assertThrows(IllegalArgumentException.class, () -> restaurant.addOrder(order));
    }

    @Test
    void testGetTimeSlotManager() {
        Restaurant restaurant = new Restaurant("Chez Pierre", 1, "test", null, 2, "1 rue de la paix");
        assertNotNull(restaurant.getTimeSlotManager());
        assertEquals(2, restaurant.getTimeSlotManager().getCapacity());
    }
}
