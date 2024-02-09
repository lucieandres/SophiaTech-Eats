package fr.etu.steats.order;

import fr.etu.steats.account.CustomerAccount;
import fr.etu.steats.enums.EOrderStatus;
import fr.etu.steats.exception.UnauthorizedModificationException;
import fr.etu.steats.exception.UnauthorizedOperationException;
import fr.etu.steats.restaurant.Menu;
import fr.etu.steats.restaurant.Restaurant;
import org.joda.time.DateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class AfterWorkOrderTest {
    private AfterWorkOrder order;
    private DateTime deliveryDate;
    private List<OrderItem> items;
    private int numberOfParticipant;
    private Menu menu;
    private Restaurant restaurant;

    @BeforeEach
    void setUp() {
        CustomerAccount customer = Mockito.mock(CustomerAccount.class);
        deliveryDate = new DateTime();
        String deliveryAddress = "123 Test Street";
        items = new ArrayList<>();
        numberOfParticipant = 5;
        menu = new Menu("Pizza", 12);
        restaurant = new Restaurant("La pizza della mama", 1, "test", "1 rue de la paix");
        restaurant.addMenuItem(menu);
        order = new AfterWorkOrder(customer, deliveryDate, deliveryAddress, items, numberOfParticipant);
    }

    @Test
    void constructor_initializesFieldsCorrectly() {
        assertNotNull(order.getItems());
        assertEquals(numberOfParticipant, order.getNumberOfParticipant());
        // Add more assertions to check other fields initialized in the constructor
    }

    @Test
    void needToBePayed_returnsFalse() {
        assertFalse(order.needToBePaid());
    }

    @Test
    void needToBeDelivered_returnsFalse() {
        assertFalse(order.needToBeDelivered());
    }

    @Test
    void getItems_returnsItemList() {
        assertEquals(items, order.getItems());
    }

    @Test
    void getTotalPrice_returnsZero() {
        assertEquals(0, order.getTotalPrice());
    }

    @Test
    void getNumberOfParticipant_returnsCorrectNumber() {
        assertEquals(numberOfParticipant, order.getNumberOfParticipant());
    }

    @Test
    void setNumberOfParticipant_updatesNumberSuccessfully() {
        int newNumber = 10;
        order.setNumberOfParticipant(newNumber);
        assertEquals(newNumber, order.getNumberOfParticipant());
    }

    @Test
    void setNumberOfParticipant_throwsExceptionAfterDeliveryDate() throws UnauthorizedModificationException {
        items.add(new OrderItem(menu, restaurant));
        order.updateDeliveryDate(deliveryDate.plusDays(1));
        assertThrows(IllegalArgumentException.class, () -> order.setNumberOfParticipant(10));
    }

    @Test
    void setFinished_setsItemsToFinishedStatus() throws UnauthorizedOperationException {
        OrderItem item = Mockito.mock(OrderItem.class);
        Mockito.when(item.getStatus()).thenReturn(EOrderStatus.WAITING_DELIVER_ACCEPTANCE);
        items.add(item);
        assertTrue(order.setFinished());
        Mockito.verify(item).setStatus(EOrderStatus.FINISH);
    }

    @Test
    void setFinished_throwsExceptionIfAlreadyFinished() {
        OrderItem item = Mockito.mock(OrderItem.class);
        Mockito.when(item.getStatus()).thenReturn(EOrderStatus.FINISH);
        items.add(item);
        assertThrows(UnauthorizedOperationException.class, () -> order.setFinished());
    }

    @Test
    void setFinished_throwsExceptionIfCanceled() {
        OrderItem item = Mockito.mock(OrderItem.class);
        Mockito.when(item.getStatus()).thenReturn(EOrderStatus.CANCELED);
        items.add(item);
        assertThrows(UnauthorizedOperationException.class, () -> order.setFinished());
    }

    @Test
    void updateDeliveryDate_updatesDateWhenAllowed() throws UnauthorizedModificationException {
        DateTime newDate = deliveryDate.plusDays(5);
        OrderItem item = Mockito.mock(OrderItem.class);
        Mockito.when(item.getStatus()).thenReturn(EOrderStatus.WAITING_DELIVER_ACCEPTANCE);
        items.add(item);
        assertTrue(order.updateDeliveryDate(newDate));
        assertEquals(newDate, order.getDeliveryDate());
    }

    @Test
    void updateDeliveryDate_throwsExceptionWhenCanceledOrFinished() {
        DateTime newDate = deliveryDate.plusDays(5);
        OrderItem item = Mockito.mock(OrderItem.class);
        Mockito.when(item.getStatus()).thenReturn(EOrderStatus.CANCELED);
        items.add(item);
        assertThrows(UnauthorizedModificationException.class, () -> order.updateDeliveryDate(newDate));
    }

    @Test
    void cancel_cancelsOrderWhenAllowed() throws UnauthorizedModificationException {
        OrderItem item = Mockito.mock(OrderItem.class);
        Mockito.when(item.getStatus()).thenReturn(EOrderStatus.WAITING_RESTAURANT_ACCEPTANCE);
        items.add(item);
        assertTrue(order.cancel());
    }

    @Test
    void cancel_FalseWhenNotAllowed() throws UnauthorizedModificationException {
        OrderItem item = Mockito.mock(OrderItem.class);
        Mockito.when(item.getStatus()).thenReturn(EOrderStatus.FINISH);
        items.add(item);
        assertFalse(order.cancel());
    }

    @Test
    void setWaitingRestaurantAcceptance_throwsUnauthorizedOperationException() {
        assertThrows(UnauthorizedOperationException.class, () -> order.setWaitingRestaurantAcceptance());
    }

    @Test
    void setInDelivery_throwsUnauthorizedOperationException() {
        assertThrows(UnauthorizedOperationException.class, () -> order.setInDelivery());
    }

    @Test
    void updateDeliveryAddress_throwsUnauthorizedOperationException() {
        assertThrows(UnauthorizedModificationException.class, () -> order.updateDeliveryAddress("New Address"));
    }

    @Test
    void getStatus_returnsCorrectStatus() {
        OrderItem item1 = Mockito.mock(OrderItem.class);
        Mockito.when(item1.getStatus()).thenReturn(EOrderStatus.WAITING_RESTAURANT_ACCEPTANCE);
        items.add(item1);

        OrderItem item2 = Mockito.mock(OrderItem.class);
        Mockito.when(item2.getStatus()).thenReturn(EOrderStatus.FINISH);
        items.add(item2);

        // Assuming computeStatus() is a method that derives status based on item statuses.
        EOrderStatus expectedStatus = order.computeStatus(items.stream().map(OrderItem::getStatus).collect(Collectors.toList()));
        assertEquals(expectedStatus, order.getStatus());
    }
}

