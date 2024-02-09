package fr.etu.steats.definitions;

import fr.etu.steats.account.AdminAccount;
import fr.etu.steats.service.RegistrationService;
import fr.etu.steats.utils.LoggerUtils;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import java.util.logging.Level;

public class AdminLoginDefinition {
    private RegistrationService registrationService;
    private AdminAccount adminAccount;
    private String firstname;
    private String lastname;
    private String password;

    @Given("there is an existing admin account")
    public void thereIsAnExistingAdminAccount() {
        this.registrationService = new RegistrationService();
    }

    @When("the admin try to login with name {string} {string} and password {string}")
    public void theAdminTryToLoginWithNameAndPassword(String firstname, String lastname, String password) {
        this.firstname = firstname;
        this.lastname = lastname;
        this.password = password;
    }

    @Then("the admin should be logged in successfully")
    public void theAdminShouldBeLoggedInSuccessfully() {
        try {
            this.adminAccount = this.registrationService.loginAdmin(this.firstname, this.lastname, this.password);
        } catch (Exception e) {
            LoggerUtils.log(Level.SEVERE, e.getMessage());
        }
        assert this.adminAccount != null;
    }

    @Then("the registration should fail with an error")
    public void theRegistrationShouldFailWithAnError() {
        try {
            this.adminAccount = this.registrationService.loginAdmin(this.firstname, this.lastname, this.password);
        } catch (Exception e) {
            LoggerUtils.log(Level.SEVERE, "Error with admin login with name " + this.firstname + " " + this.lastname + " and password : " + this.password);
        }
        assert this.adminAccount == null;
    }

}
