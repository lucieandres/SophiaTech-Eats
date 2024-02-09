package fr.etu.steats.order;

import fr.etu.steats.restaurant.Menu;
import fr.etu.steats.restaurant.Restaurant;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OrderItemTest {
    @Test
    void testOrderItemCreation() {
        Menu menu = new Menu("Pizza", 12);
        Restaurant restaurant = new Restaurant("Pizza della mama", 1, "test", "1 rue de la paix");
        assertThrows(IllegalArgumentException.class, () -> new OrderItem(null, restaurant));
        assertThrows(IllegalArgumentException.class, () -> new OrderItem(menu, null));
        assertThrows(IllegalArgumentException.class, () -> new OrderItem(null, null));

        OrderItem orderItem = new OrderItem(menu, restaurant);
        assertEquals(menu, orderItem.getMenu());
        assertEquals(restaurant, orderItem.getRestaurant());
    }

    @Test
    void testDelivrable() {
        OrderItem item = new OrderItem(new Menu("Kebab", 10), new Restaurant("Kebabier", 2, "password", "2 rue la paix"));
        assertTrue(item.needToBeDelivered());

        item.setNeedToBeDelivered(false);

        assertFalse(item.needToBeDelivered());
    }
}
