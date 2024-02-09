package fr.etu.steats.definitions;

import fr.etu.steats.STEats;
import fr.etu.steats.exception.UnauthorizedOperationException;
import fr.etu.steats.registry.RestaurantRegistry;
import fr.etu.steats.restaurant.Menu;
import fr.etu.steats.utils.LoggerUtils;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import java.util.logging.Level;

import static org.junit.jupiter.api.Assertions.*;

public class EditRestaurantDefinition {
    private RestaurantRegistry restaurantRegistry;
    private String newMenuName;
    private String oldMenuName;
    private double newPrice;

    @Given("a restaurant named {string} with a password {string}")
    public void aRestaurantNamedWithAPassword(String restaurantName, String password) {
        this.restaurantRegistry = new STEats().getRestaurantRegistry();
        restaurantRegistry.restaurantLogin(restaurantName, password);
        assertNotNull(restaurantRegistry.getRestaurant());
    }

    @When("this restaurant want to edit the menu {string} with a new price of {int} and a new name of {string}")
    public void thisRestaurantWantChangeThePriceOfWithANewPriceOf(String oldMenuName, int newPrice, String newMenuName) throws UnauthorizedOperationException {
        this.newMenuName = newMenuName;
        this.newPrice = newPrice;

        Menu menu = restaurantRegistry.getRestaurant().getMenuItem(oldMenuName);
        assertNotNull(menu);

        this.oldMenuName = menu.getName();

        restaurantRegistry.editExistingMenuItem(oldMenuName, newMenuName, newPrice);
    }

    @Then("the menu is modified in the system")
    public void theMenuIsModifiedInTheSystem() {
        Menu menu = restaurantRegistry.getRestaurant().getMenuItem(newMenuName);
        assertEquals(newPrice, menu.getPrice(null));
        assertEquals(newMenuName, menu.getName());

        assertNull(restaurantRegistry.getRestaurant().getMenuItem(oldMenuName));
    }

    @When("this restaurant want to create the new menu {string} with a price of {int}")
    public void thisRestaurantWantToCreateTheNewMenuWithAPriceOf(String newMenuName, int newPrice) throws UnauthorizedOperationException {
        this.newMenuName = newMenuName;
        this.newPrice = newPrice;

        this.restaurantRegistry.addMenuItem(newMenuName, newPrice);
    }

    @Then("the menu is added in the system")
    public void theMenuIsAddedInTheSystem() {
        Menu menu = restaurantRegistry.getRestaurant().getMenuItem(newMenuName);
        assertNotNull(menu);

        assertEquals(newMenuName, menu.getName());
        assertEquals(newPrice, menu.getGlobalPrice());
    }

    @When("this restaurant want to delete the menu {string}")
    public void thisRestaurantWantToDeleteTheMenu(String menuName) throws UnauthorizedOperationException {
        this.oldMenuName = menuName;
        this.restaurantRegistry.removeMenuItem(menuName);
    }

    @Then("the menu is not present in the system anymore")
    public void theMenuIsNotPresentInTheSystemAnymore() {
        assertNull(restaurantRegistry.getRestaurant().getMenuItem(oldMenuName));
    }

    @Given("a non registered restaurant")
    public void aNonRegisteredRestaurant() {
        this.restaurantRegistry = new STEats().getRestaurantRegistry();
    }

    @When("he want to delete the menu {string}")
    public void heWantToDeleteTheMenu(String menuItem) {
        this.oldMenuName = menuItem;
    }

    @Then("he get an error")
    public void heGetAnError() {
        assertThrows(UnauthorizedOperationException.class, () -> restaurantRegistry.removeMenuItem(oldMenuName));
        try {
            restaurantRegistry.removeMenuItem(oldMenuName);
        } catch (UnauthorizedOperationException e) {
            LoggerUtils.log(Level.SEVERE, e.getMessage());
        }
    }
}
