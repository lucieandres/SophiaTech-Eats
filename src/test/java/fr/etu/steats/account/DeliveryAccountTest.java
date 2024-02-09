package fr.etu.steats.account;

import fr.etu.steats.exception.BadPasswordException;
import fr.etu.steats.order.OrderAbstract;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class DeliveryAccountTest {
    private DeliveryAccount deliveryMan;

    @BeforeEach
    void setup() {
        deliveryMan = new DeliveryAccount("John", "Doe", "delivery1");
    }

    @Test
    void testAssignedOrder() {
        OrderAbstract order = mock(OrderAbstract.class);
        deliveryMan.setAssignedOrder(order);

        assertEquals(order, deliveryMan.getAssignedOrder());
    }

    @Test
    void testDeliveryAccountCreationAndParameterProcessing() {
        DeliveryAccount deliveryAccount = new DeliveryAccount(" John", "Doe ", "Delivery1");
        assertNull(deliveryAccount.getAssignedOrder());
        assertEquals("john", deliveryAccount.getFirstName());
        assertEquals("doe", deliveryAccount.getLastName());
        assertEquals("Delivery1", deliveryAccount.getPassword());
    }

    @Test
    void testErrorCaseAtDeliveryAccountCreation() {
        assertThrows(IllegalArgumentException.class, () -> new DeliveryAccount(null, null, "delivery1"));
        assertThrows(IllegalArgumentException.class, () -> new DeliveryAccount("John", "Doe", null));
        assertThrows(IllegalArgumentException.class, () -> new DeliveryAccount("John", "", "delivery1"));
        assertThrows(IllegalArgumentException.class, () -> new DeliveryAccount("", "", "delivery1"));
        assertThrows(IllegalArgumentException.class, () -> new DeliveryAccount("John", "Doe", ""));
    }

    @Test
    void testPasswordCheck() throws BadPasswordException {
        assertThrows(BadPasswordException.class, () -> deliveryMan.checkPassword(null));
        assertThrows(BadPasswordException.class, () -> deliveryMan.checkPassword(""));
        assertThrows(BadPasswordException.class, () -> deliveryMan.checkPassword("abcd"));
        assertTrue(deliveryMan.checkPassword("delivery1"));
    }
}

