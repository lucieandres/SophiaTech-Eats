package fr.etu.steats.definitions;

import fr.etu.steats.restaurant.Menu;
import fr.etu.steats.restaurant.Restaurant;
import fr.etu.steats.service.RestaurantService;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.joda.time.LocalDateTime;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class RestaurantDefinition {
    private final RestaurantService service = new RestaurantService(Set.of(new Restaurant("Chez Pierre", 1, "test", "1 rue de la paix")));
    private Restaurant restaurant;
    private String errorMessage;

    @Given("a restaurant")
    public void givenARestaurant() {
        restaurant = service.findRestaurantById(1);
        restaurant.addMenuItem(new Menu("Steak au poivre", 10.99));
    }

    @When("I update the menu item {string} to have a price of {double}")
    public void whenIUpdateMenuItemInRestaurant(String menuItemName, double newPrice) {
        Menu updatedItem = new Menu(menuItemName, newPrice);
        restaurant.updateMenuItem(menuItemName, updatedItem);
    }

    @Then("the price of {string} in the menu should be {double}")
    public void thenPriceShouldBeUpdated(String menuItemName, double expectedPrice) {
        Menu menuItem = restaurant.getMenuItem(menuItemName);
        assertEquals(expectedPrice, menuItem.getGlobalPrice());
    }

    @When("I update an invalid menu item {string} to have a price of {double}")
    public void whenIUpdateInvalidMenuItemInRestaurant(String menuItemName, double newPrice) {
        try {
            Menu newItem = new Menu(menuItemName, newPrice);
            restaurant.updateMenuItem(menuItemName, newItem);
        } catch (Exception e) {
            errorMessage = e.getMessage();
        }
    }

    @Then("an error message should be printed")
    public void thenAnErrorMessageShouldBePrinted() {
        assertNotNull(errorMessage);
    }

    @When("the restaurant manager updates the restaurant operating hours to {int}:{int} to {int}:{int}")
    public void whenTheRestaurantManagerUpdateTheRestaurantOperatingHoursToTo(int newOpeningHour, int newOpeningMinutes, int newClosingHour, int newClosingMinutes) {
        LocalDateTime currentDateTime = LocalDateTime.now();
        LocalDateTime newOpeningTime = currentDateTime.withTime(newOpeningHour, newOpeningMinutes, 0, 0);
        LocalDateTime newClosingTime = currentDateTime.withTime(newClosingHour, newClosingMinutes, 0, 0);

        restaurant.getTimeSlotManager().setOpeningTime(newOpeningTime.toLocalTime());
        restaurant.getTimeSlotManager().setClosingTime(newClosingTime.toLocalTime());
    }

    @Then("the restaurant operating hours are {int}:{int} to {int}:{int}")
    public void thenTheRestaurantOperatingHoursToTo(int expectedOpeningHour, int expectedOpeningMinutes, int expectedClosingHour, int expectedClosingMinutes) {
        LocalDateTime currentDateTime = LocalDateTime.now();
        LocalDateTime expectedOpeningTime = currentDateTime.withTime(expectedOpeningHour, expectedOpeningMinutes, 0, 0);
        LocalDateTime expectedClosingTime = currentDateTime.withTime(expectedClosingHour, expectedClosingMinutes, 0, 0);

        assertEquals(expectedOpeningTime.toLocalTime(), restaurant.getTimeSlotManager().getOpeningTime());
        assertEquals(expectedClosingTime.toLocalTime(), restaurant.getTimeSlotManager().getClosingTime());
    }

}
