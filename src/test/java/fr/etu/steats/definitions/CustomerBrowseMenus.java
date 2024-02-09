package fr.etu.steats.definitions;

import fr.etu.steats.STEats;
import fr.etu.steats.account.CustomerAccount;
import fr.etu.steats.exception.BadPasswordException;
import fr.etu.steats.exception.NoAccountFoundException;
import fr.etu.steats.registry.CustomerRegistry;
import fr.etu.steats.restaurant.Menu;
import fr.etu.steats.restaurant.Restaurant;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class CustomerBrowseMenus {
    private STEats steats;
    private CustomerRegistry customerRegistry;
    private Map<Integer, List<Menu>> menus;
    private Restaurant restaurant;

    @Given("A user not logged in")
    public void aUserNotLoggedIn() {
        steats = new STEats();
        customerRegistry = steats.getCustomerRegistry();
    }

    @Given("A user logged in with firstname {string}, lastname {string} and password {string}")
    public void aUserLoggedInWithFirstnameLastnameAndPassword(String firstname, String lastname, String password) throws BadPasswordException, NoAccountFoundException {
        steats = new STEats();
        customerRegistry = steats.getCustomerRegistry();
        customerRegistry.customerLogin(firstname, lastname, password);
        CustomerAccount customer = customerRegistry.getCustomerAccount();
        assertNotNull(customer);
    }

    @When("He wants to see the menu of every restaurant")
    public void heWantToSeeAllRestaurantsMenu() {
        menus = customerRegistry.getAllMenuOfEachRestaurant();
    }

    @Then("He should see all restaurant menus")
    public void heShouldSeeAllRestaurantsMenu() {
        assertNotNull(menus);
        assertFalse(menus.isEmpty());
    }

    @And("A restaurant {string} with id {int} and with a menu")
    public void aRestaurantWithIdAndWithMenu(String restaurantName, int restId) {
        restaurant = customerRegistry.getRestaurantService().findRestaurantById(restId);
        assertNotNull(restaurant);
        assertEquals(restaurantName.toLowerCase(), restaurant.getName());

    }

    @When("He wants to see the menu of {string} with id {int}")
    public void heWantToSeeTheMenuOf(String restaurantName, int id) {
        assertEquals(restaurantName.toLowerCase(), restaurant.getName());
        menus = new HashMap<>();
        menus.put(id, customerRegistry.getMenuOfRestaurant(id));
    }

    @Then("He should see the menu of {string}")
    public void heShouldSeeTheMenuOf(String restaurantName) {
        assertNotNull(menus);
        assertFalse(menus.isEmpty());
        assertEquals(restaurantName.toLowerCase(), restaurant.getName());
    }
}
