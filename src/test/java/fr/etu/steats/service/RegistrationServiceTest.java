package fr.etu.steats.service;

import fr.etu.steats.account.AdminAccount;
import fr.etu.steats.account.CustomerAccount;
import fr.etu.steats.account.DeliveryAccount;
import fr.etu.steats.exception.AlreadyRegisteredUser;
import fr.etu.steats.exception.BadPasswordException;
import fr.etu.steats.exception.NoAccountFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RegistrationServiceTest {
    private RegistrationService registrationService;

    @BeforeEach
    void setup() {
        Set<CustomerAccount> customerAccounts = new HashSet<>();
        customerAccounts.add(new CustomerAccount("Karim", "Charleux", "test"));
        customerAccounts.add(new CustomerAccount("Axel", "Delille", "test2"));
        Set<DeliveryAccount> deliveryAccounts = new HashSet<>();
        deliveryAccounts.add(new DeliveryAccount("John", "Doe", "delivery1"));
        deliveryAccounts.add(new DeliveryAccount("Jane", "Smith", "delivery2"));
        Set<AdminAccount> adminAccounts = new HashSet<>();
        adminAccounts.add(new AdminAccount("admin", "admin", "admin"));

        this.registrationService = new RegistrationService(customerAccounts, deliveryAccounts, adminAccounts);
    }

    @Test
    void testWorkingCaseLogin() throws BadPasswordException, NoAccountFoundException {
        CustomerAccount account = registrationService.loginCustomer(" AxEl ", " dElIlLe ", "test2");
        assertEquals("axel", account.getFirstName());
        assertEquals("delille", account.getLastName());
        assertEquals("test2", account.getPassword());

        account = registrationService.loginCustomer("Axel", "Delille", "test2");
        assertEquals("axel", account.getFirstName());
        assertEquals("delille", account.getLastName());
        assertEquals("test2", account.getPassword());

        account = registrationService.loginCustomer("axel", "delille", "test2");
        assertEquals("axel", account.getFirstName());
        assertEquals("delille", account.getLastName());
        assertEquals("test2", account.getPassword());
    }

    @Test
    void testErrorCaseLogin() {
        assertThrows(NoAccountFoundException.class, () -> registrationService.loginCustomer("baba", "yaga", "test"));
        assertThrows(NoAccountFoundException.class, () -> registrationService.loginCustomer("baba", "yaga", "test2"));
        assertThrows(NoAccountFoundException.class, () -> registrationService.loginCustomer("Axel", "yaga", "test2"));
        assertThrows(NoAccountFoundException.class, () -> registrationService.loginCustomer("baba", "Delille", "test2"));

        assertThrows(BadPasswordException.class, () -> registrationService.loginCustomer("Axel", "Delille", "badPassword"));
        assertThrows(BadPasswordException.class, () -> registrationService.loginCustomer("Axel", "Delille", "test2 "));
        assertThrows(BadPasswordException.class, () -> registrationService.loginCustomer("Axel", "Delille", " test2"));
        assertThrows(BadPasswordException.class, () -> registrationService.loginCustomer("Axel", "Delille", "Test2"));

        assertThrows(IllegalArgumentException.class, () -> registrationService.loginCustomer(null, "Delille", "badPassword"));
        assertThrows(IllegalArgumentException.class, () -> registrationService.loginCustomer("Axel", null, "badPassword"));
        assertThrows(IllegalArgumentException.class, () -> registrationService.loginCustomer("Axel", "Delille", null));
        assertThrows(IllegalArgumentException.class, () -> registrationService.loginCustomer("", "Delille", "badPassword"));
        assertThrows(IllegalArgumentException.class, () -> registrationService.loginCustomer("Axel", "", "badPassword"));
        assertThrows(IllegalArgumentException.class, () -> registrationService.loginCustomer("Axel", "Delille", ""));
    }

    @Test
    void testWorkingCaseRegister() throws AlreadyRegisteredUser, BadPasswordException, NoAccountFoundException {
        CustomerAccount customer = registrationService.registerCustomer("Nina", "Boulton", "test3");

        assertEquals("nina", customer.getFirstName());
        assertEquals("boulton", customer.getLastName());
        assertEquals("test3", customer.getPassword());

        customer = registrationService.loginCustomer("nina", "boulton", "test3");
        assertEquals("nina", customer.getFirstName());
        assertEquals("boulton", customer.getLastName());
        assertEquals("test3", customer.getPassword());
    }

    @Test
    void testErrorCaseRegister() {
        assertThrows(IllegalArgumentException.class, () -> registrationService.registerCustomer(null, "Boulton", "password"));
        assertThrows(IllegalArgumentException.class, () -> registrationService.registerCustomer("Nina", null, "password"));
        assertThrows(IllegalArgumentException.class, () -> registrationService.registerCustomer("Nina", "Boulton", null));
        assertThrows(IllegalArgumentException.class, () -> registrationService.registerCustomer("", "Boulton", "password"));
        assertThrows(IllegalArgumentException.class, () -> registrationService.registerCustomer("Nina", "", "password"));
        assertThrows(IllegalArgumentException.class, () -> registrationService.registerCustomer("Nina", "Boulton", ""));

        assertThrows(AlreadyRegisteredUser.class, () -> registrationService.registerCustomer("Axel", "Delille", "test3"));
        assertThrows(AlreadyRegisteredUser.class, () -> registrationService.registerCustomer("axel", "delille", "test3"));
        assertThrows(AlreadyRegisteredUser.class, () -> registrationService.registerCustomer("AxEl", "dElIlLe", "test3"));
        assertThrows(AlreadyRegisteredUser.class, () -> registrationService.registerCustomer(" AxEl ", " dElIlLe ", "test3"));
    }

    @Test
    void testWorkingCaseLoginDeliveryAccount() throws NoAccountFoundException, BadPasswordException {
        DeliveryAccount deliver = registrationService.loginDelivery(" John ", " Doe ", "delivery1");
        assertEquals("john", deliver.getFirstName());
        assertEquals("doe", deliver.getLastName());
        assertEquals("delivery1", deliver.getPassword());

        deliver = registrationService.loginDelivery("Jane", "Smith", "delivery2");
        assertEquals("jane", deliver.getFirstName());
        assertEquals("smith", deliver.getLastName());
        assertEquals("delivery2", deliver.getPassword());
    }

    @Test
    void testErrorCaseLoginDeliveryAccount() {
        assertThrows(NoAccountFoundException.class, () -> registrationService.loginDelivery("baba", "yaga", "delivery1"));
        assertThrows(NoAccountFoundException.class, () -> registrationService.loginDelivery("John", "yaga", "delivery2"));
        assertThrows(BadPasswordException.class, () -> registrationService.loginDelivery("Jane", "Smith", "wrongPassword"));

        assertThrows(IllegalArgumentException.class, () -> registrationService.loginDelivery(null, "Smith", "delivery2"));
        assertThrows(IllegalArgumentException.class, () -> registrationService.loginDelivery("Jane", null, "delivery2"));
        assertThrows(IllegalArgumentException.class, () -> registrationService.loginDelivery("Jane", "Smith", null));
        assertThrows(IllegalArgumentException.class, () -> registrationService.loginDelivery("", "Smith", "delivery2"));
    }

    @Test
    void testWorkingCaseRegisterDeliveryAccount() throws AlreadyRegisteredUser {
        DeliveryAccount deliver = registrationService.registerDelivery("Mike", "Tyson", "delivery3");
        assertEquals("mike", deliver.getFirstName());
        assertEquals("tyson", deliver.getLastName());
        assertEquals("delivery3", deliver.getPassword());
    }

    @Test
    void testErrorCaseRegisterDeliveryAccount() {
        assertThrows(AlreadyRegisteredUser.class, () -> registrationService.registerDelivery("John", "Doe", "delivery3"));
        assertThrows(IllegalArgumentException.class, () -> registrationService.registerDelivery(null, "Tyson", "delivery3"));
        assertThrows(IllegalArgumentException.class, () -> registrationService.registerDelivery("Mike", "", "delivery3"));
    }

    @Test
    void testAdminLogin() throws BadPasswordException, NoAccountFoundException {
        AdminAccount admin = registrationService.loginAdmin("admin", "admin", "admin");
        assertEquals("admin", admin.getFirstName());
        assertEquals("admin", admin.getLastName());
        assertEquals("admin", admin.getPassword());
    }

    @Test
    void testAdminLoginError() {
        assertThrows(NoAccountFoundException.class, () -> registrationService.loginAdmin("baba", "yaga", "admin"));
        assertThrows(NoAccountFoundException.class, () -> registrationService.loginAdmin("admin", "yaga", "admin"));
        assertThrows(BadPasswordException.class, () -> registrationService.loginAdmin("admin", "admin", "wrongPassword"));

        assertThrows(IllegalArgumentException.class, () -> registrationService.loginAdmin(null, "admin", "admin"));
        assertThrows(IllegalArgumentException.class, () -> registrationService.loginAdmin("admin", null, "admin"));
        assertThrows(IllegalArgumentException.class, () -> registrationService.loginAdmin("admin", "admin", null));
        assertThrows(IllegalArgumentException.class, () -> registrationService.loginAdmin("", "admin", "admin"));
    }
}
