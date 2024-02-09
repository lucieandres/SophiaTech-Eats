package fr.etu.steats.order;

import fr.etu.steats.account.CustomerAccount;
import fr.etu.steats.account.DeliveryAccount;
import fr.etu.steats.enums.EOrderStatus;
import fr.etu.steats.exception.UnauthorizedOperationException;
import fr.etu.steats.restaurant.Menu;
import fr.etu.steats.restaurant.Restaurant;
import org.joda.time.DateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SingleOrderTest {
    private SingleOrder order;
    private CustomerAccount customerAccount;
    private Restaurant restaurant;
    private Menu menu;
    private DateTime deliveryDate;
    private String deliveryAddress;

    @BeforeEach
    void setup() {
        customerAccount = new CustomerAccount("Karim", "Charleux", "test");
        deliveryDate = new DateTime();
        deliveryAddress = "930 Rte des Colles, 06410 Biot";
        order = new SingleOrder(customerAccount, deliveryDate, deliveryAddress, new ArrayList<>());
        menu = new Menu("Pizza", 12);
        restaurant = new Restaurant("La pizza della mama", 1, "test", "1 rue de la paix");
        restaurant.addMenuItem(menu);
    }

    @Test
    void testOrderCreation() {
        List<OrderItem> items = new ArrayList<>();
        assertThrows(IllegalArgumentException.class, () -> new SingleOrder(null, deliveryDate, deliveryAddress, items));
        assertThrows(IllegalArgumentException.class, () -> new SingleOrder(customerAccount, null, deliveryAddress, items));
        assertThrows(IllegalArgumentException.class, () -> new SingleOrder(null, null, deliveryAddress, null));

        SingleOrder singleOrder = new SingleOrder(customerAccount, deliveryDate, deliveryAddress, items);
        assertEquals(customerAccount, singleOrder.getCustomer());
        assertTrue(singleOrder.getItems().isEmpty());

        SingleOrder singleOrder2 = new SingleOrder(customerAccount, deliveryDate, deliveryAddress, List.of(new OrderItem(menu, restaurant)));
        assertEquals(1, singleOrder2.getItems().size());
        assertEquals(customerAccount, singleOrder2.getCustomer());
    }

    @Test
    void testOrderItemAddition() {
        assertThrows(IllegalArgumentException.class, () -> order.addItem(null));

        assertTrue(order.getItems().isEmpty());
        order.addItem(new OrderItem(menu, restaurant));
        assertEquals(1, order.getItems().size());
    }

    @Test
    void testGetTotalPrice() {
        assertEquals(0, order.getTotalPrice(), 0.01);
        order.addItem(new OrderItem(menu, restaurant));
        assertEquals(12, order.getTotalPrice(), 0.01);
        order.addItem(new OrderItem(menu, restaurant));
        assertEquals(24, order.getTotalPrice(), 0.01);
    }

    @Test
    void testCalculatePriceWithCreditDeduction() {
        customerAccount.setCredit(14.0);
        order.addItem(new OrderItem(menu, restaurant));
        assertEquals(12, order.getItems().stream()
                .mapToDouble(item -> item.getPrice(order.getCustomer().getType()))
                .sum(), 0.01);
        assertEquals(0.0, order.calculatePriceWithCreditDeduction(menu.getGlobalPrice()), 0.01);
        assertEquals(2.0, customerAccount.getCredit());

        customerAccount.setCredit(14.0);
        order.addItem(new OrderItem(menu, restaurant));
        double totalPrice = order.getItems().stream()
                .mapToDouble(item -> item.getPrice(order.getCustomer().getType()))
                .sum();
        assertEquals(24, totalPrice, 0.01);
        assertEquals(10, order.calculatePriceWithCreditDeduction(totalPrice), 0.01);
        assertEquals(0.0, customerAccount.getCredit());
    }

    @Test
    void testOrderIdIncrement() {
        SingleOrder order2 = new SingleOrder(customerAccount, deliveryDate, deliveryAddress, new ArrayList<>());
        assertEquals(order.getId() + 1, order2.getId());
    }

    @Test
    void testUpdateDeliveryDate() {
        assertThrows(IllegalArgumentException.class, () -> order.updateDeliveryDate(null));
        DateTime newDeliveryDate = new DateTime();
        order.updateDeliveryDate(newDeliveryDate);
        assertTrue(newDeliveryDate.isEqual(order.getDeliveryDate()));
    }

    @Test
    void testUpdateDeliveryAddress() {
        assertThrows(IllegalArgumentException.class, () -> order.updateDeliveryAddress(null));
        String newDeliveryAddress = "930 Rte des Colles, 06410 Biot";
        order.updateDeliveryAddress(newDeliveryAddress);
        assertEquals(newDeliveryAddress, order.getDeliveryAddress());
    }

    @Test
    void testUpdateItems() {
        assertThrows(IllegalArgumentException.class, () -> order.updateItems(null));
        List<OrderItem> newOrderItems = new ArrayList<>();
        newOrderItems.add(new OrderItem(menu, restaurant));
        order.updateItems(newOrderItems);
        assertEquals(newOrderItems, order.getItems());
    }

    @Test
    void testCancel() {
        order.cancel();
        order.getItems().forEach(item -> assertEquals(EOrderStatus.CANCELED, item.getStatus()));
    }

    @Test
    void testIncorrectSetInDelivery() {
        Exception exception;
        // No delivery man
        exception = assertThrows(UnauthorizedOperationException.class, order::setInDelivery);
        order.assignDeliveryMan(new DeliveryAccount("test", "test", "test"));
        assertEquals("You need a delivery man to put an order in delivery", exception.getMessage());

        // Wrong status
        exception = assertThrows(UnauthorizedOperationException.class, order::setInDelivery);
        assertEquals("You can't deliver a non ready order...", exception.getMessage());

        // Empty order
        order = spy(order);
        when(order.getStatus()).thenReturn(EOrderStatus.WAITING_DELIVER_ACCEPTANCE);
        exception = assertThrows(UnauthorizedOperationException.class, order::setInDelivery);
        assertEquals("You can't deliver an empty order...", exception.getMessage());
    }

    @Test
    void testSuccessSetInDelivery() throws UnauthorizedOperationException {
        List<OrderItem> items = List.of(new OrderItem(new Menu("Kebab", 10), mock(Restaurant.class)));
        DeliveryAccount deliveryMan = new DeliveryAccount("test", "test", "test");

        order = new SingleOrder(customerAccount, deliveryDate, deliveryAddress, items);

        order = spy(order);
        when(order.getStatus()).thenReturn(EOrderStatus.WAITING_DELIVER_ACCEPTANCE);

        order.assignDeliveryMan(deliveryMan);

        assertTrue(order.setInDelivery());
    }

    @Test
    void testIncorrectSetFinished() {
        Exception exception;
        // Wrong status
        exception = assertThrows(UnauthorizedOperationException.class, order::setFinished);
        assertEquals("You can't close a non delivered order...", exception.getMessage());

        // Empty order
        order = spy(order);
        when(order.getStatus()).thenReturn(EOrderStatus.IN_DELIVERY);
        exception = assertThrows(UnauthorizedOperationException.class, order::setFinished);
        assertEquals("You can't give an empty order...", exception.getMessage());
    }

    @Test
    void testSetFinishedSuccess() throws UnauthorizedOperationException {
        List<OrderItem> items = List.of(new OrderItem(new Menu("Kebab", 10), mock(Restaurant.class)));
        DeliveryAccount deliveryMan = new DeliveryAccount("test", "test", "test");
        order = new SingleOrder(customerAccount, deliveryDate, "test", items);

        order = spy(order);
        when(order.getStatus()).thenReturn(EOrderStatus.IN_DELIVERY);

        order.assignDeliveryMan(deliveryMan);

        assertTrue(order.setFinished());
    }

    @Test
    void testApplyDiscount() {
        assertThrows(UnauthorizedOperationException.class, () -> order.addCustomerCredit(-2, 0.2));
        assertThrows(UnauthorizedOperationException.class, () -> order.addCustomerCredit(0, -0.2));
        assertThrows(UnauthorizedOperationException.class, () -> order.addCustomerCredit(0, 2.2));

        try {
            assertFalse(order.addCustomerCredit(5, 0.1));

            order.addItem(new OrderItem(menu, restaurant));
            assertTrue(order.addCustomerCredit(1, 0.1));
            assertEquals(1.2, order.getCustomer().getCredit(), 0.01);

            for (int i = 0; i < 4; i++) {
                order.addItem(new OrderItem(menu, restaurant));
            }
            assertTrue(order.addCustomerCredit(5, 0.1));
            assertEquals(7.2, order.getCustomer().getCredit(), 0.01);

        } catch (UnauthorizedOperationException e) {
            throw new RuntimeException(e);
        }
    }

}
