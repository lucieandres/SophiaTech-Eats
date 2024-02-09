package fr.etu.steats.order;

import fr.etu.steats.account.CustomerAccount;
import fr.etu.steats.enums.EOrderStatus;
import fr.etu.steats.exception.UnauthorizedModificationException;
import fr.etu.steats.exception.UnauthorizedOperationException;
import fr.etu.steats.restaurant.Menu;
import fr.etu.steats.restaurant.Restaurant;
import org.joda.time.DateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

class BuffetOrderTest {

    private CustomerAccount customer;
    private DateTime deliveryDate;
    private List<OrderItem> items;
    private BuffetOrder buffetOrder;
    private Restaurant restaurant;

    @BeforeEach
    void setUp() {
        customer = new CustomerAccount("John", "Doe", "password");
        deliveryDate = new DateTime().plusDays(1);
        restaurant = new Restaurant("McDonald's", 1, "password", "1 rue de la paix");
        items = new ArrayList<>(List.of(new OrderItem(new Menu("Kebab", 10), restaurant)));
        buffetOrder = spy(new BuffetOrder(customer, deliveryDate, items, restaurant));
        when(buffetOrder.needToBePaid()).thenReturn(false);
    }

    @Test
    void testErrorInConstructor() {
        assertThrows(IllegalArgumentException.class, () -> new BuffetOrder(null, null, null, null));
        assertThrows(IllegalArgumentException.class, () -> new BuffetOrder(customer, null, null, null));
        assertThrows(IllegalArgumentException.class, () -> new BuffetOrder(customer, deliveryDate, null, null));
        assertThrows(IllegalArgumentException.class, () -> new BuffetOrder(customer, deliveryDate, null, null));
        List<OrderItem> emptyList = Collections.emptyList();
        assertThrows(IllegalArgumentException.class, () -> new BuffetOrder(customer, deliveryDate, emptyList, null));
        assertThrows(IllegalArgumentException.class, () -> new BuffetOrder(customer, deliveryDate, items, null));
        // Add a menu from another restaurant
        Restaurant otherRestaurant = new Restaurant("KFC", 5, "password", "1 rue de la paix");
        OrderItem otherItem = new OrderItem(new Menu("Poulet", 5), otherRestaurant);
        items.add(otherItem);
        assertThrows(IllegalArgumentException.class, () -> new BuffetOrder(customer, deliveryDate, items, restaurant));
    }

    @Test
    void testImpossibilityToAddSubOrder() {
        assertThrows(UnauthorizedOperationException.class, () -> buffetOrder.addSubOrder(new SingleOrder(customer, deliveryDate, "deliveryAddress", Collections.emptyList())));
    }

    @Test
    void testGetItems() {
        assertEquals(items, buffetOrder.getItems());
    }

    @Test
    void testGetTotalPrice() {
        when(buffetOrder.needToBePaid()).thenReturn(true);
        assertEquals(10, buffetOrder.getTotalPrice());
    }

    @Test
    void testGetStatus() {
        assertEquals(EOrderStatus.WAITING_RESTAURANT_ACCEPTANCE, buffetOrder.getStatus());
    }

    @Test
    void setFinishWithIncorrectStatus() {
        assertThrows(UnauthorizedOperationException.class, () -> buffetOrder.setFinished());

        BuffetOrder spyBuffet = spy(buffetOrder);
        when(spyBuffet.getStatus()).thenReturn(EOrderStatus.FINISH);
        assertThrows(UnauthorizedOperationException.class, () -> buffetOrder.setFinished());
    }

    @Test
    void setFinish() throws UnauthorizedOperationException {
        BuffetOrder spyBuffet = spy(buffetOrder);
        when(spyBuffet.getStatus()).thenReturn(EOrderStatus.IN_PREPARATION).thenReturn(EOrderStatus.IN_PREPARATION).thenReturn(EOrderStatus.FINISH);
        assertTrue(spyBuffet.setFinished());
        assertEquals(EOrderStatus.FINISH, spyBuffet.getStatus());
    }

    @Test
    void testApplyDiscountErrorCases() {
        assertThrows(UnauthorizedOperationException.class, () -> buffetOrder.addCustomerCredit(-1, 0));
        assertThrows(UnauthorizedOperationException.class, () -> buffetOrder.addCustomerCredit(10, -1));
        assertThrows(UnauthorizedOperationException.class, () -> buffetOrder.addCustomerCredit(10, 2));
    }

    @Test
    void testApplyDiscount() throws UnauthorizedOperationException {
        when(buffetOrder.needToBePaid()).thenReturn(true);
        assertFalse(buffetOrder.addCustomerCredit(10, 0.5));
        assertEquals(10, buffetOrder.getTotalPrice()); // No discount because just one item

        assertTrue(buffetOrder.addCustomerCredit(1, 0.5));
        assertEquals(5, buffetOrder.getTotalPrice()); // Discount applied
    }

    @Test
    void testGetRestaurant() {
        assertEquals(restaurant, buffetOrder.getRestaurant());
    }

    @Test
    void testSetInDeliveryError() {
        // Error because this buffet order don't need to be delivered
        assertThrows(UnauthorizedOperationException.class, () -> buffetOrder.setInDelivery());
    }


    @Test
    void testUpdateDeliveryDateError() {
        DateTime newDeliveryDate = new DateTime().plusDays(2);
        // Error because this buffet order don't need to be delivered
        assertThrows(UnauthorizedModificationException.class, () -> buffetOrder.updateDeliveryDate(newDeliveryDate));
    }

    @Test
    void testUpdateDeliveryAddressError() {
        // Error because this buffet order don't need to be delivered
        assertThrows(UnauthorizedModificationException.class, () -> buffetOrder.updateDeliveryAddress("456 Campus Avenue"));
    }

    @Test
    void cancelSuccess() {
        assertTrue(buffetOrder.cancel());
        assertEquals(EOrderStatus.CANCELED, buffetOrder.getStatus());
    }

    @Test
    void cancelError() {
        BuffetOrder spyBuffet = spy(buffetOrder);
        when(spyBuffet.getStatus()).thenReturn(EOrderStatus.IN_DELIVERY);
        assertFalse(spyBuffet.cancel());
        assertEquals(EOrderStatus.IN_DELIVERY, spyBuffet.getStatus());
    }

    @Test
    void needToBePaid() {
        assertFalse(buffetOrder.needToBePaid());
    }

    @Test
    void needToBeDelivered() {
        assertFalse(buffetOrder.needToBeDelivered());
    }

    @Nested
    class DeliveredBuffetOrderTests {
        private BuffetOrder deliveredBuffetOrder;

        @BeforeEach
        void setUpDelivered() throws UnauthorizedOperationException {
            String deliveryAddress = "123 Campus Lane";
            deliveredBuffetOrder = new BuffetOrder(customer, deliveryDate, items, deliveryAddress, restaurant, true, false);
        }

        @Test
        void testConstructorError() {
            assertThrows(IllegalArgumentException.class, () -> new BuffetOrder(customer, deliveryDate, items, "", restaurant, true, false));
            assertThrows(IllegalArgumentException.class, () -> new BuffetOrder(customer, deliveryDate, null, "", restaurant, true, false));
            DateTime oldDeliveryDate = new DateTime().minusYears(1);
            assertThrows(IllegalArgumentException.class, () -> new BuffetOrder(customer, oldDeliveryDate, items, "123 rue hess", restaurant, true, false));
            assertThrows(IllegalArgumentException.class, () -> new BuffetOrder(customer, deliveryDate, items, "123 rue hess", null, true, false));
            // Add a menu from another restaurant
            Restaurant otherRestaurant = new Restaurant("KFC", 5, "password", "1 rue de la paix");
            OrderItem otherItem = new OrderItem(new Menu("Poulet", 5), otherRestaurant);
            items.add(otherItem);
            assertThrows(IllegalArgumentException.class, () -> new BuffetOrder(customer, deliveryDate, items, "123 rue hess", restaurant, true, false));
        }

        @Test
        void setInDelivery() throws UnauthorizedOperationException {
            BuffetOrder spyBuffet = spy(deliveredBuffetOrder);
            when(spyBuffet.getStatus()).thenReturn(EOrderStatus.IN_PREPARATION).thenReturn(EOrderStatus.IN_DELIVERY);
            assertTrue(spyBuffet.setInDelivery());
            assertEquals(EOrderStatus.IN_DELIVERY, spyBuffet.getStatus());
        }

        @Test
        void testSetInDeliveryError() {
            BuffetOrder spyBuffet = spy(deliveredBuffetOrder);
            when(spyBuffet.getStatus()).thenReturn(EOrderStatus.FINISH);
            assertThrows(UnauthorizedOperationException.class, spyBuffet::setInDelivery);
        }

        @Test
        void setFinish() throws UnauthorizedOperationException {
            BuffetOrder spyBuffet = spy(deliveredBuffetOrder);
            when(spyBuffet.getStatus()).thenReturn(EOrderStatus.IN_DELIVERY).thenReturn(EOrderStatus.IN_DELIVERY).thenReturn(EOrderStatus.FINISH);
            assertTrue(spyBuffet.setFinished());
            assertEquals(EOrderStatus.FINISH, spyBuffet.getStatus());
        }

        @Test
        void testSetFinishError() {
            BuffetOrder spyBuffet = spy(deliveredBuffetOrder);
            when(spyBuffet.getStatus()).thenReturn(EOrderStatus.FINISH);
            assertThrows(UnauthorizedOperationException.class, spyBuffet::setFinished);
        }

        @Test
        void testUpdateDeliveryAddress() throws UnauthorizedModificationException {
            assertTrue(deliveredBuffetOrder.updateDeliveryAddress("456 Campus Avenue"));
            assertEquals("456 Campus Avenue", deliveredBuffetOrder.getDeliveryAddress());
        }

        @Test
        void testUpdateDeliveryAddressError() {
            assertThrows(IllegalArgumentException.class, () -> deliveredBuffetOrder.updateDeliveryAddress(null));
            assertThrows(IllegalArgumentException.class, () -> deliveredBuffetOrder.updateDeliveryAddress(""));

            BuffetOrder spyBuffet = spy(deliveredBuffetOrder);
            when(spyBuffet.getStatus()).thenReturn(EOrderStatus.IN_DELIVERY);
            assertThrows(UnauthorizedModificationException.class, () -> spyBuffet.updateDeliveryAddress("456 Campus Avenue"));
        }

        @Test
        void testUpdateDeliveryDate() throws UnauthorizedModificationException {
            DateTime newDeliveryDate = new DateTime().plusDays(2);
            assertTrue(deliveredBuffetOrder.updateDeliveryDate(newDeliveryDate));
            assertEquals(newDeliveryDate, deliveredBuffetOrder.getDeliveryDate());
        }

        @Test
        void testUpdateDeliveryDateError() {
            BuffetOrder spyBuffet = spy(deliveredBuffetOrder);
            when(spyBuffet.getStatus()).thenReturn(EOrderStatus.IN_DELIVERY);
            assertThrows(UnauthorizedModificationException.class, () -> spyBuffet.updateDeliveryDate(new DateTime().plusDays(2)));

            assertThrows(IllegalArgumentException.class, () -> deliveredBuffetOrder.updateDeliveryDate(null));

            DateTime oldDeliveryDate = new DateTime().minusYears(1);
            assertThrows(IllegalArgumentException.class, () -> deliveredBuffetOrder.updateDeliveryDate(oldDeliveryDate));
        }
    }

    @Nested
    class PaidBuffetOrderTests {
        private BuffetOrder paidBuffetOrder;

        @BeforeEach
        void setUpPaid() throws UnauthorizedOperationException {
            List<OrderItem> itemList = new ArrayList<>();
            for (int itemIndex = 0; itemIndex < 10; itemIndex++) {
                itemList.add(new OrderItem(new Menu("Kebab", 10), restaurant));
            }
            paidBuffetOrder = new BuffetOrder(customer, deliveryDate, itemList, "", restaurant, false, true);
        }

        @Test
        void testDiscountIsAppliedAtCreation() {
            // 10% discount applied because 10 items in the order
            assertEquals(90, paidBuffetOrder.getTotalPrice());
        }

        @Test
        void testUpdateDeliveryDateError() {
            DateTime newDeliveryDate = new DateTime().plusDays(2);
            // Error because this buffet order don't need to be delivered
            assertThrows(UnauthorizedModificationException.class, () -> buffetOrder.updateDeliveryDate(newDeliveryDate));
        }

        @Test
        void testSetInDeliveryError() {
            // Error because this buffet order don't need to be delivered
            assertThrows(UnauthorizedOperationException.class, () -> buffetOrder.setInDelivery());
        }

        @Test
        void testUpdateDeliveryAddressError() {
            // Error because this buffet order don't need to be delivered
            assertThrows(UnauthorizedModificationException.class, () -> buffetOrder.updateDeliveryAddress("456 Campus Avenue"));
        }

        @Test
        void setFinish() throws UnauthorizedOperationException {
            BuffetOrder spyBuffet = spy(buffetOrder);
            when(spyBuffet.getStatus()).thenReturn(EOrderStatus.IN_PREPARATION).thenReturn(EOrderStatus.IN_PREPARATION).thenReturn(EOrderStatus.FINISH);
            assertTrue(spyBuffet.setFinished());
            assertEquals(EOrderStatus.FINISH, spyBuffet.getStatus());
        }

        @Test
        void testCancelError() {
            BuffetOrder spyBuffet = spy(buffetOrder);
            when(spyBuffet.getStatus()).thenReturn(EOrderStatus.IN_DELIVERY);
            assertFalse(spyBuffet.cancel());
            assertEquals(EOrderStatus.IN_DELIVERY, spyBuffet.getStatus());
        }
    }
}