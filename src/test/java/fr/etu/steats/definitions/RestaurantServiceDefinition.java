package fr.etu.steats.definitions;

import fr.etu.steats.restaurant.Menu;
import fr.etu.steats.restaurant.Restaurant;
import fr.etu.steats.service.RestaurantService;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class RestaurantServiceDefinition {
    private RestaurantService service;
    private List<Menu> menus;

    @Given("a restaurant service")
    public void givenARestaurantService() {
        service = new RestaurantService();
    }

    @When("I view the menu for restaurant with ID {int}")
    public void whenIViewTheMenuForRestaurantWithID(int restaurantId) {
        menus = service.viewMenu(restaurantId);
        assertNotNull(menus);
        assertEquals(3, menus.size());
    }

    @Then("I should see {int} menu items")
    public void thenIShouldSeeMenuItems(int expectedCount) {
        assertNotNull(menus);
        assertEquals(expectedCount, menus.size());
    }

    @When("I add a menu item {string} with price {double} to restaurant with ID {int}")
    public void iAddAMenuItemWithPriceToRestaurantWithID(String menuItemName, double menuItemPrice, int restaurantId) {
        Menu newItem = new Menu(menuItemName, menuItemPrice);
        Restaurant restaurant = service.findRestaurantById(restaurantId);
        restaurant.addMenuItem(newItem);
    }

    @Then("the menu for restaurant with ID {int} should include {string} menu item with price {double}")
    public void thenMenuShouldIncludeItem(int restaurantId, String menuItemName, double menuItemPrice) {
        List<Menu> menus = service.viewMenu(restaurantId);
        assertTrue(menus.stream().anyMatch(menu -> menu.getName().equals(menuItemName)));
        assertTrue(menus.stream().anyMatch(menu -> menu.getGlobalPrice() == menuItemPrice));
    }

    @When("I remove the menu item {string} from restaurant with ID {int}")
    public void whenIRemoveMenuItemFromRestaurant(String menuItemName, int restaurantId) {
        Restaurant restaurant = service.findRestaurantById(restaurantId);
        restaurant.removeMenuItem(menuItemName);
    }

    @Then("the menu for restaurant with ID {int} should not include {string}")
    public void thenMenuShouldNotIncludeItem(int restaurantId, String menuItemName) {
        List<Menu> menus = service.viewMenu(restaurantId);
        assertFalse(menus.stream().anyMatch(menu -> menu.getName().equals(menuItemName)));
    }
}
