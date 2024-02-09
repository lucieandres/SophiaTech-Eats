package fr.etu.steats.definitions;

import fr.etu.steats.account.DeliveryAccount;
import fr.etu.steats.service.RegistrationService;
import fr.etu.steats.utils.LoggerUtils;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import java.util.logging.Level;

public class DeliveryRegistrationDefinition {
    private RegistrationService registrationService;
    private DeliveryAccount deliveryAccount;
    private String firstname;
    private String lastname;
    private String password;

    @Given("there is an existing deliveryman account")
    public void thereIsAnExistingDeliverymanAccount() {
        this.registrationService = new RegistrationService();
    }

    @When("the deliveryman try to login with name {string} {string} and password {string}")
    public void theDeliverymanTryToLoginWithNameAndPassword(String firstname, String lastname, String password) {
        this.firstname = firstname;
        this.lastname = lastname;
        this.password = password;
    }

    @Then("the deliveryman should be logged in successfully")
    public void theDeliverymanShouldBeLoggedInSuccessfully() {
        try {
            this.deliveryAccount = this.registrationService.loginDelivery(this.firstname, this.lastname, this.password);
        } catch (Exception e) {
            LoggerUtils.log(Level.SEVERE, e.getMessage());
        }
        assert this.deliveryAccount != null;
    }

}
