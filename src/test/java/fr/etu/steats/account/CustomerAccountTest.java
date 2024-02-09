package fr.etu.steats.account;

import fr.etu.steats.exception.BadPasswordException;
import fr.etu.steats.exception.UnauthorizedOperationException;
import fr.etu.steats.order.OrderAbstract;
import fr.etu.steats.order.OrderBuilder;
import fr.etu.steats.order.OrderItem;
import fr.etu.steats.restaurant.Menu;
import fr.etu.steats.restaurant.Restaurant;
import fr.etu.steats.utils.Scheduler;
import org.joda.time.DateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CustomerAccountTest {
    private CustomerAccount customer;

    @BeforeEach
    void setup() {
        customer = new CustomerAccount("Karim", "Charleux", "test");
    }

    @Test
    void testCustomerCreationAndParameterProcessing() {
        CustomerAccount customerAccount = new CustomerAccount(" Karim", " Charleux ", "Test");
        assertTrue(customerAccount.getOrders().isEmpty());
        assertEquals("karim", customerAccount.getFirstName());
        assertEquals("charleux", customerAccount.getLastName());
        assertEquals("Test", customerAccount.getPassword());
        assertEquals(0.0, customerAccount.getCredit());
    }

    @Test
    void testErrorCaseAtCustomerCreation() {
        assertThrows(IllegalArgumentException.class, () -> new CustomerAccount(null, null, "test"));
        assertThrows(IllegalArgumentException.class, () -> new CustomerAccount("Karim", "Charleux", null));
        assertThrows(IllegalArgumentException.class, () -> new CustomerAccount("Karim", null, "test"));
        assertThrows(IllegalArgumentException.class, () -> new CustomerAccount("", "", "test"));
        assertThrows(IllegalArgumentException.class, () -> new CustomerAccount("Karim", "Charleux", ""));
    }

    @Test
    void testPasswordCheck() throws BadPasswordException {
        assertThrows(BadPasswordException.class, () -> customer.checkPassword(null));
        assertThrows(BadPasswordException.class, () -> customer.checkPassword(""));
        assertThrows(BadPasswordException.class, () -> customer.checkPassword("abcd"));
        assertTrue(customer.checkPassword("test"));
    }

    @Test
    void testOrderAddition() throws UnauthorizedOperationException {
        assertTrue(customer.getOrders().isEmpty());

        assertThrows(IllegalArgumentException.class, () -> customer.addOrder(null));

        DateTime deliveryDate = new DateTime().plusMinutes(1);
        String deliveryAddress = "930 Rte des Colles, 06410 Biot";

        Menu menu = new Menu("burger", 10);
        Restaurant restaurant = new Restaurant("resto", 1, "azertyuiop", new Scheduler(), "1 rue de la paix");
        restaurant.addMenuItem(menu);

        OrderAbstract order = new OrderBuilder(deliveryDate, customer)
                .setDeliveryAddress(deliveryAddress)
                .addMenuItem(new OrderItem(menu, restaurant), 1)
                .build();

        assertTrue(customer.addOrder(order));
        assertEquals(1, customer.getOrders().size());
    }
}
