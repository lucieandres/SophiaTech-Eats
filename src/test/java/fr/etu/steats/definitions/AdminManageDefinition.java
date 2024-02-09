package fr.etu.steats.definitions;

import fr.etu.steats.STEats;
import fr.etu.steats.account.AdminAccount;
import fr.etu.steats.account.DeliveryAccount;
import fr.etu.steats.exception.UnauthorizedOperationException;
import fr.etu.steats.registry.AdminRegistry;
import fr.etu.steats.restaurant.Restaurant;
import fr.etu.steats.restaurant.StatisticReport;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import static org.junit.Assert.*;

public class AdminManageDefinition {
    private DeliveryAccount deliveryAccount;
    private StatisticReport statisticReport;
    private AdminAccount adminAccount;
    private AdminRegistry adminRegistry;
    private Exception exception;
    private Boolean result;

    @Given("I am logged in as an admin")
    public void iAmLoggedInAsAnAdmin() {
        this.adminRegistry = new STEats().getAdminRegistry();
        this.adminRegistry.adminLogin("admin", "admin", "admin");
        this.adminAccount = this.adminRegistry.getAdminAccount();
        assert this.adminAccount != null;
    }

    @Given("I not logged in as an admin")
    public void iNotLoggedInAsAnAdmin() {
        this.adminRegistry = new STEats().getAdminRegistry();
        this.adminAccount = this.adminRegistry.getAdminAccount();
        assert this.adminAccount == null;
    }

    @When("I attempt to add a restaurant with name {string} and password {string}")
    public void whenIAttemptToAddARestaurantWithNameIdAndPassword(String name, String password) {
        try {
            this.adminRegistry.addRestaurant(name, password, "1 rue de la paix");
        } catch (Exception e) {
            this.exception = e;
        }
    }

    @Then("the restaurant {string} should be added successfully")
    public void theRestaurantShouldBeAddedSuccessfully(String name) throws Exception {
        Restaurant restaurantFound = null;
        for (Restaurant restaurant : this.adminRegistry.getRestaurants()) {
            if (restaurant.getName().equals(name.toLowerCase())) {
                restaurantFound = restaurant;
            }
        }
        assertNotNull(restaurantFound);
    }

    @Then("I should receive an unauthorized operation message")
    public void iShouldReceiveAnUnauthorizedOperationMessage() {
        assert this.exception instanceof UnauthorizedOperationException;
    }

    @And("there is a restaurant with id {int}")
    public void thereIsARestaurantWithId(int id) throws Exception {
        for (Restaurant restaurant : this.adminRegistry.getRestaurants()) {
            if (restaurant.getId() == id) {
                return;
            }
        }
        throw new Exception("Restaurant with id " + id + " not found");
    }

    @When("I attempt to remove the restaurant with id {int}")
    public void iAttemptToRemoveTheRestaurantWithId(int id) {
        try {
            this.adminRegistry.removeRestaurant(id);
        } catch (Exception e) {
            this.exception = e;
        }
    }

    @Then("the restaurant with id {int} should be removed successfully")
    public void theRestaurantWithIdShouldBeRemovedSuccessfully(int id) throws Exception {
        for (Restaurant restaurant : this.adminRegistry.getRestaurants()) {
            if (restaurant.getId() == id) {
                throw new Exception("Restaurant with id " + id + " found");
            }
        }
    }

    @And("there is a delivery account with firstname {string}, lastname {string}, and password {string}")
    public void thereIsADeliveryAccountWithFirstnameLastnameAndPassword(String firstname, String lastname, String password) {
        try {
            this.deliveryAccount = this.adminRegistry.addDeliveryAccount(firstname, lastname, password);
        } catch (Exception e) {
            this.exception = e;
        }
    }

    @When("I attempt to add a delivery account with firstname {string}, lastname {string}, and password {string}")
    public void iAttemptToAddADeliveryAccountWithFirstnameLastnameAndPassword(String firstname, String lastname, String password) {
        try {
            this.deliveryAccount = this.adminRegistry.addDeliveryAccount(firstname, lastname, password);
        } catch (Exception e) {
            this.exception = e;
        }
    }

    @When("I attempt to remove the delivery account")
    public void iAttemptToRemoveTheDeliveryAccount() {
        try {
            this.adminRegistry.removeDeliveryAccount(this.deliveryAccount);
        } catch (Exception e) {
            this.exception = e;
        }
    }

    @Then("the delivery account should be removed successfully")
    public void theDeliveryAccountShouldBeRemovedSuccessfully() {
        assertNull(this.exception);
    }

    @Then("the delivery account should be added successfully")
    public void theDeliveryAccountShouldBeAddedSuccessfully() {
        assertNull(this.exception);
    }

    @When("I request statistics report from all restaurants")
    public void iRequestStatisticsReportFromAllRestaurants() {
        try {
            this.statisticReport = this.adminRegistry.getStatisticsReportFromAllRestaurants();
        } catch (Exception e) {
            this.exception = e;
        }
    }

    @Then("I should receive a statistics report")
    public void iShouldReceiveAStatisticsReport() {
        assertNull(this.exception);
        assertNotNull(this.statisticReport);
    }

    @And("there is a restaurant with name {string}, id {int}")
    public void andThereIsARestaurantWithNameIdAndPassword(String name, int id) throws Exception {
        for (Restaurant restaurant : this.adminRegistry.getRestaurants()) {
            if (restaurant.getName().equals(name.toLowerCase()) && restaurant.getId() == id) {
                return;
            }
        }
        throw new Exception("Restaurant with id " + id + " not found");
    }

    @When("I request statistics report from restaurant with id {int}")
    public void iRequestStatisticsReportFromRestaurantWithId(int id) {
        try {
            this.statisticReport = this.adminRegistry.getStatisticsReportFromOneRestaurant(id);
        } catch (Exception e) {
            this.exception = e;
        }
    }

    @When("I attempt to add a delivery location with the name {string}")
    public void iAttemptToAddADeliveryLocationWithTheName(String location) {
        try {
            this.result = this.adminRegistry.addDeliveryLocation(location);
        } catch (Exception e) {
            this.exception = e;
        }
    }

    @Then("The delivery location should exist")
    public void theDeliveryLocationShouldExist() {
        assertNull(this.exception);
        assert this.result;
    }

    @And("I have a delivery location with the name {string}")
    public void iHaveADeliveryLocationWithTheName(String location) throws UnauthorizedOperationException {
        assertTrue(this.adminRegistry.isDeliveryLocation(location));
    }


    @When("I attempt to edit the delivery location with the name {string} to {string}")
    public void iAttemptToEditTheDeliveryLocationWithTheNameTo(String oldName, String newName) {
        try {
            this.result = this.adminRegistry.editDeliveryLocation(oldName, newName);
        } catch (Exception e) {
            this.exception = e;
        }
    }

    @Then("The delivery location should exist with the name {string}")
    public void theDeliveryLocationShouldExistWithTheName(String location) throws UnauthorizedOperationException {
        assertNull(this.exception);
        assert this.result;
        assertTrue(this.adminRegistry.isDeliveryLocation(location));
    }

    @When("I attempt to delete the delivery location with the name {string}")
    public void iAttemptToDeleteTheDeliveryLocationWithTheName(String location) {
        try {
            this.result = this.adminRegistry.removeDeliveryLocation(location);
        } catch (Exception e) {
            this.exception = e;
        }
    }

    @Then("The delivery location {string} should not exist")
    public void theDeliveryLocationShouldNotExist(String location) throws UnauthorizedOperationException {
        assertNull(this.exception);
        assert this.result;
        assertFalse(this.adminRegistry.isDeliveryLocation(location));
    }
}