package fr.etu.steats.definitions;

import fr.etu.steats.STEats;
import fr.etu.steats.account.CustomerAccount;
import fr.etu.steats.exception.BadPasswordException;
import fr.etu.steats.exception.NoAccountFoundException;
import fr.etu.steats.exception.UnauthorizedOperationException;
import fr.etu.steats.registry.CustomerRegistry;
import fr.etu.steats.restaurant.Restaurant;
import fr.etu.steats.utils.LoggerUtils;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.joda.time.DateTime;

import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import static org.junit.jupiter.api.Assertions.*;

public class CustomerRegistrationDefinition {
    private CustomerRegistry customerRegistry;
    private int restaurantId;
    private String menuName;
    private String firstname;
    private String lastname;
    private String password;

    @Given("A non registered user")
    public void aNonRegisteredUser() {
        this.customerRegistry = new STEats().getCustomerRegistry();
    }

    @When("He tries to pass an order")
    public void heTryToPassAnOrder() {
        Restaurant tmp = customerRegistry.getRestaurantService().getRestaurants().stream().toList().get(0);
        restaurantId = tmp.getId();
        menuName = tmp.getMenuItems().get(0).getName();
    }

    @Then("He get an error message and the order didn't succeed")
    public void heGetAnError() {

        DateTime deliveryDate = new DateTime();
        String deliveryAddress = "930 Rte des Colles, 06410 Biot";

        //Check the error
        assertThrows(UnauthorizedOperationException.class, () -> customerRegistry.placeSingleOrder(Map.of(restaurantId, List.of(menuName)), deliveryDate, deliveryAddress));

        //Display the error message
        try {
            customerRegistry.placeSingleOrder(Map.of(restaurantId, List.of(menuName)), deliveryDate, deliveryAddress);
        } catch (UnauthorizedOperationException e) {
            LoggerUtils.log(Level.SEVERE, e.getMessage());
        }
    }

    @When("He tries to register himself with name {string} {string} and password {string}")
    public void heTryToRegisterHimselfWithNameAndPassword(String firstname, String lastname, String password) {
        this.firstname = firstname;
        this.lastname = lastname;
        this.password = password;
        customerRegistry.customerRegister(this.firstname, this.lastname, this.password);
    }

    @Then("He's successfully registered and logged into the system")
    public void heSSuccessfullyRegisteredAndLoggedIntoTheSystem() throws BadPasswordException, NoAccountFoundException {
        // The customer is already logged in with the registration
        CustomerAccount customer = customerRegistry.getCustomerAccount();
        assertNotNull(customer);
        assertEquals(firstname.trim().toLowerCase(), customer.getFirstName());
        assertEquals(lastname.trim().toLowerCase(), customer.getLastName());
        assertEquals(password, customer.getPassword());

        // The user is registered into the system
        customer = customerRegistry.getRegistrationService().loginCustomer(customer.getFirstName(), customer.getLastName(), customer.getPassword());
        assertNotNull(customer);
    }

    @When("He tries to login with name {string} {string} and password {string}")
    public void heTryToLoginWithNameAndPassword(String firstname, String lastname, String password) {
        this.firstname = firstname;
        this.lastname = lastname;
        this.password = password;
    }

    @Then("He's successfully logged into the system")
    public void heSSuccessfullyLoggedIntoTheSystem() throws BadPasswordException, NoAccountFoundException {
        // The user is registered into the system
        CustomerAccount customer = customerRegistry.getRegistrationService().loginCustomer(firstname, lastname, password);
        assertNotNull(customer);
        assertEquals(firstname.trim().toLowerCase(), customer.getFirstName());
        assertEquals(lastname.trim().toLowerCase(), customer.getLastName());
        assertEquals(password, customer.getPassword());
    }

    @Then("The login didn't succeed")
    public void theLoginDidntSucceed() {
        // The user is not registered into the system
        assertThrows(Exception.class, () -> customerRegistry.getRegistrationService().loginCustomer(firstname, lastname, password));
        assertNull(customerRegistry.getCustomerAccount());
    }
}