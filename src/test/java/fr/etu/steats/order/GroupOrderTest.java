package fr.etu.steats.order;

import fr.etu.steats.account.CustomerAccount;
import fr.etu.steats.account.DeliveryAccount;
import fr.etu.steats.enums.EOrderStatus;
import fr.etu.steats.exception.UnauthorizedModificationException;
import fr.etu.steats.exception.UnauthorizedOperationException;
import fr.etu.steats.restaurant.Menu;
import fr.etu.steats.restaurant.Restaurant;
import fr.etu.steats.utils.LoggerUtils;
import org.joda.time.DateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GroupOrderTest {
    private GroupOrder groupOrder;
    private SingleOrder singleOrder;
    private CustomerAccount customerAccount;
    private DateTime deliveryDate;
    private String deliveryAddress;

    @BeforeEach
    void setup() throws UnauthorizedOperationException {
        customerAccount = new CustomerAccount("user1", "name1", "password1");
        deliveryDate = new DateTime().plusMinutes(1).plusMinutes(1);
        deliveryAddress = "930 Rte des Colles, 06410 Biot";
        groupOrder = (GroupOrder) new OrderBuilder(deliveryDate, customerAccount)
                .setDeliveryAddress(deliveryAddress)
                .build();

        singleOrder = new SingleOrder(customerAccount, deliveryDate, deliveryAddress, new ArrayList<>());
    }

    @Test
    void testAddOrder() throws UnauthorizedOperationException {
        SingleOrder order = new SingleOrder(customerAccount, deliveryDate, "test", null);
        assertEquals(0, groupOrder.getSubOrders().size());
        groupOrder.addSubOrder(order);
        assertEquals(1, groupOrder.getSubOrders().size());
    }

    @Test
    void testAddOrderWithIncorrectField() {
        // Null order
        assertThrows(IllegalArgumentException.class, () -> groupOrder.addSubOrder(null));
    }

    @Test
    void testGetSubOrders() throws UnauthorizedOperationException {
        assertNotNull(groupOrder.getSubOrders());
        assertEquals(0, groupOrder.getSubOrders().size());
        SingleOrder order = new SingleOrder(customerAccount, deliveryDate, "test", null);
        groupOrder.addSubOrder(order);
        assertEquals(1, groupOrder.getSubOrders().size());
        assertEquals(order, groupOrder.getSubOrders().get(0));
    }

    @Test
    void testGetStatusAfterCreation() {
        assertEquals(EOrderStatus.WAITING_PAYMENT, groupOrder.getStatus());
    }

    @Test
    void testIncorrectSetInDelivery() {
        Exception exception;
        // No delivery man
        exception = assertThrows(UnauthorizedOperationException.class, groupOrder::setInDelivery);
        groupOrder.assignDeliveryMan(new DeliveryAccount("test", "test", "test"));
        assertEquals("You need a delivery man to put an order in delivery", exception.getMessage());

        // Wrong status
        exception = assertThrows(UnauthorizedOperationException.class, groupOrder::setInDelivery);
        assertEquals("You can't deliver a non ready order...", exception.getMessage());

        // Empty order
        groupOrder = spy(groupOrder);
        when(groupOrder.getStatus()).thenReturn(EOrderStatus.WAITING_DELIVER_ACCEPTANCE);
        exception = assertThrows(UnauthorizedOperationException.class, groupOrder::setInDelivery);
        assertEquals("You can't deliver an empty order...", exception.getMessage());
    }

    @Test
    void testSuccessSetInDelivery() throws UnauthorizedOperationException {
        List<OrderItem> items = List.of(new OrderItem(new Menu("Kebab", 10), mock(Restaurant.class)));
        DeliveryAccount deliveryMan = new DeliveryAccount("test", "test", "test");
        OrderAbstract singleOrder = new SingleOrder(customerAccount, deliveryDate, "test", items);

        groupOrder = spy(groupOrder);
        singleOrder = spy(singleOrder);
        when(groupOrder.getStatus()).thenReturn(EOrderStatus.WAITING_DELIVER_ACCEPTANCE);
        when(singleOrder.getStatus()).thenReturn(EOrderStatus.WAITING_DELIVER_ACCEPTANCE);

        groupOrder.assignDeliveryMan(deliveryMan);
        singleOrder.assignDeliveryMan(deliveryMan);
        groupOrder.addSubOrder(singleOrder);

        assertTrue(groupOrder.setInDelivery());
    }

    @Test
    void testIncorrectSetFinished() {
        Exception exception;
        // Wrong status
        exception = assertThrows(UnauthorizedOperationException.class, groupOrder::setFinished);
        assertEquals("You can't close a non delivered order...", exception.getMessage());

        // Empty order
        groupOrder = spy(groupOrder);
        when(groupOrder.getStatus()).thenReturn(EOrderStatus.IN_DELIVERY);
        exception = assertThrows(UnauthorizedOperationException.class, groupOrder::setFinished);
        assertEquals("You can't give an empty order...", exception.getMessage());
    }

    @Test
    void testSetFinishedSuccess() throws UnauthorizedOperationException {
        List<OrderItem> items = List.of(new OrderItem(new Menu("Kebab", 10), mock(Restaurant.class)));
        DeliveryAccount deliveryMan = new DeliveryAccount("test", "test", "test");
        OrderAbstract singleOrder = new SingleOrder(customerAccount, deliveryDate, "test", items);

        groupOrder = spy(groupOrder);
        singleOrder = spy(singleOrder);
        when(groupOrder.getStatus()).thenReturn(EOrderStatus.WAITING_RESTAURANT_ACCEPTANCE);
        when(singleOrder.getStatus()).thenReturn(EOrderStatus.WAITING_RESTAURANT_ACCEPTANCE);

        groupOrder.assignDeliveryMan(deliveryMan);
        singleOrder.assignDeliveryMan(deliveryMan);
        groupOrder.addSubOrder(singleOrder);

        when(groupOrder.getStatus()).thenReturn(EOrderStatus.IN_DELIVERY);
        when(singleOrder.getStatus()).thenReturn(EOrderStatus.IN_DELIVERY);

        assertTrue(groupOrder.setFinished());
    }

    @Test
    void testGetTotalPrice() throws UnauthorizedOperationException {
        assertEquals(0, groupOrder.getTotalPrice());
        List<OrderItem> items = List.of(new OrderItem(new Menu("Kebab", 10), mock(Restaurant.class)));
        OrderAbstract singleOrder = new SingleOrder(customerAccount, deliveryDate, "test", items);

        groupOrder.addSubOrder(singleOrder);
        assertEquals(10, groupOrder.getTotalPrice());
    }

    @Test
    void testApplyDiscount() {
        assertThrows(UnauthorizedOperationException.class, () -> groupOrder.addCustomerCredit(-2, 0.2));
        assertThrows(UnauthorizedOperationException.class, () -> groupOrder.addCustomerCredit(0, -0.2));
        assertThrows(UnauthorizedOperationException.class, () -> groupOrder.addCustomerCredit(0, 2.2));

        try {
            assertFalse(groupOrder.addCustomerCredit(5, 0.1));

            assertFalse(groupOrder.addCustomerCredit(1, 0.1));

            Menu menu = new Menu("Pizza", 12);
            Restaurant restaurant = new Restaurant("La pizza della mama", 1, "test", "1 rue de la paix");
            restaurant.addMenuItem(menu);

            singleOrder.addItem(new OrderItem(menu, restaurant));
            groupOrder.addSubOrder(singleOrder);

            assertTrue(groupOrder.addCustomerCredit(1, 0.1));
            assertEquals(1.2, groupOrder.getCustomer().getCredit(), 0.001);

            for (int i = 0; i < 4; i++) {
                groupOrder.addSubOrder(
                        new SingleOrder(customerAccount, deliveryDate, deliveryAddress, List.of(new OrderItem(menu, restaurant)))
                );
            }
            assertTrue(groupOrder.addCustomerCredit(5, 0.1));

            for (OrderAbstract order : groupOrder.getSubOrders()) {
                assertEquals(7.2, order.getCustomer().getCredit(), 0.001);
            }

            groupOrder.addSubOrder(new SingleOrder(customerAccount, deliveryDate, deliveryAddress, new ArrayList<>()));
            assertFalse(groupOrder.addCustomerCredit(6, 0.1));

        } catch (UnauthorizedOperationException e) {
            throw new RuntimeException(e);
        }
    }


    @Test
    void testUpdateDeliveryAddress() {
        assertThrows(IllegalArgumentException.class, () -> groupOrder.updateDeliveryAddress(null));
        try {
            groupOrder.updateDeliveryAddress("930 Rte des Colles, 06410 Biot");
        } catch (UnauthorizedModificationException e) {
            LoggerUtils.log(Level.SEVERE, e.getMessage());
        }
        assertEquals("930 Rte des Colles, 06410 Biot", groupOrder.getDeliveryAddress());
    }

    @Test
    void testUpdateDeliveryDate() {
        assertThrows(IllegalArgumentException.class, () -> groupOrder.updateDeliveryDate(null));
        DateTime deliveryDate = new DateTime().plusMinutes(1);

        try {
            groupOrder.updateDeliveryDate(deliveryDate);
        } catch (UnauthorizedModificationException e) {
            LoggerUtils.log(Level.SEVERE, e.getMessage());
        }

        assertTrue(deliveryDate.isEqual(groupOrder.getDeliveryDate()));
    }

    @Test
    void testCancel() {
        try {
            groupOrder.cancel();
            for (OrderAbstract suborder : groupOrder.getSubOrders()) {
                for (OrderItem item : suborder.getItems())
                    assertEquals(EOrderStatus.CANCELED, item.getStatus());
            }
        } catch (UnauthorizedModificationException e) {
            LoggerUtils.log(Level.SEVERE, e.getMessage());
        }
    }

    @Test
    void testGetDeliveryDateOfSubOrder() throws UnauthorizedOperationException {
        OrderAbstract groupOrder = new OrderBuilder(deliveryDate, customerAccount)
                .setDeliveryAddress(deliveryAddress)
                .build();

        DateTime deliveryDate2 = new DateTime(deliveryDate).plusHours(2);

        SingleOrder subOrder = new SingleOrder(customerAccount, deliveryDate2, deliveryAddress, new ArrayList<>());

        groupOrder.addSubOrder(subOrder);

        assertTrue(deliveryDate.isEqual(subOrder.getDeliveryDate()));
    }

}

