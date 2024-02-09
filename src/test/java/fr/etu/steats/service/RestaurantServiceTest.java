package fr.etu.steats.service;

import fr.etu.steats.exception.BadPasswordException;
import fr.etu.steats.exception.NonExistantRestaurantException;
import fr.etu.steats.restaurant.Menu;
import fr.etu.steats.restaurant.Restaurant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class RestaurantServiceTest {
    private RestaurantService service;

    @BeforeEach
    public void setUp() {
        this.service = new RestaurantService(new HashSet<>());
        Restaurant restaurant = this.service.addRestaurant("Chez Pierre", "test", "1 rue de la paix");
        restaurant.addMenuItem(new Menu("Steak au poivre", 25.0));
        restaurant.addMenuItem(new Menu("Salade César", 10.0));
        restaurant.addMenuItem(new Menu("Poulet rôti", 20.0));
    }

    @Test
    void testViewMenu() {
        List<Menu> menus = service.viewMenu(1);
        assertNotNull(menus);
        assertEquals(3, menus.size());
    }

    @Test
    void testViewMenuOfInvalidRestaurant() {
        assertThrows(IllegalArgumentException.class, () -> service.viewMenu(-2));
    }

    @Test
    void getRestaurantById() {
        Restaurant restaurant = service.findRestaurantById(1);
        assertNotNull(restaurant);
        assertEquals(1, restaurant.getId());
    }

    @Test
    void getRestaurantByInvalidId() {
        Restaurant restaurant = service.findRestaurantById(-5);
        assertNull(restaurant);
    }

    @Test
    void getRestaurants() {
        Set<Restaurant> restaurants = service.getRestaurants();
        assertNotNull(restaurants);
        assertEquals(1, restaurants.size());
    }

    @Test
    void testWorkingCaseLogin() throws BadPasswordException, NonExistantRestaurantException {
        Restaurant restaurant = service.login("Chez pierre", "test");
        assertEquals("chez pierre", restaurant.getName());
        assertEquals(3, restaurant.getMenuItems().size());
        assertEquals(1, restaurant.getId());

        restaurant = service.login(" ChEz PiErRe ", "test");
        assertEquals("chez pierre", restaurant.getName());
        assertEquals(3, restaurant.getMenuItems().size());
        assertEquals(1, restaurant.getId());

        restaurant = service.login("chez pierre", "test");
        assertEquals("chez pierre", restaurant.getName());
        assertEquals(3, restaurant.getMenuItems().size());
        assertEquals(1, restaurant.getId());
    }

    @Test
    void testErrorCaseLogin() {
        assertThrows(NonExistantRestaurantException.class, () -> service.login("baba", "yaga"));

        assertThrows(BadPasswordException.class, () -> service.login("Chez pierre", "password"));

        assertThrows(IllegalArgumentException.class, () -> service.login(null, null));
        assertThrows(IllegalArgumentException.class, () -> service.login("", ""));
        assertThrows(IllegalArgumentException.class, () -> service.login("", "test"));
        assertThrows(IllegalArgumentException.class, () -> service.login(null, "test"));
        assertThrows(IllegalArgumentException.class, () -> service.login("Chez pierre", ""));
        assertThrows(IllegalArgumentException.class, () -> service.login("Chez pierre", null));
    }

    @Test
    void testAutoIncrement() {
        Restaurant restaurant = service.addRestaurant("Chez Pierrot", "test", "1 rue de la paix");
        assertEquals(2, restaurant.getId());
        restaurant = service.addRestaurant("Chez Karim", "test", "1 rue de la paix");
        assertEquals(3, restaurant.getId());
        restaurant = service.addRestaurant("Chez Bob", "test", "1 rue de la paix");
        assertEquals(4, restaurant.getId());
    }

    @Test
    void testAddRestaurant() {
        Restaurant newRestaurant = service.addRestaurant("New Restaurant", "password", "1 rue de la paix");
        assertNotNull(newRestaurant);
        assertEquals("new restaurant", newRestaurant.getName());
        assertEquals(2, newRestaurant.getId());
    }

    @Test
    void testAddRestaurantWithDuplicateName() {
        assertThrows(IllegalArgumentException.class, () -> service.addRestaurant("Chez Pierre", "password", "1 rue de la paix"));
    }

    @Test
    void testRemoveRestaurant() {
        assertTrue(service.removeRestaurant(1));
        assertNull(service.findRestaurantById(1));
    }

    @Test
    void testRemoveNonExistentRestaurant() {
        assertFalse(service.removeRestaurant(99));
        assertEquals(1, service.getRestaurants().size());
    }

    @Test
    void testAddDuplicateRestaurantNameDifferentCase() {
        assertThrows(IllegalArgumentException.class, () -> service.addRestaurant("chez PIERRE", "password", "1 rue de la paix"));
        assertEquals(1, service.getRestaurants().size());
    }

}
