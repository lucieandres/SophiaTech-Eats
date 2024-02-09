package fr.etu.steats.service;

import fr.etu.steats.account.CustomerAccount;
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

import java.util.ArrayList;
import java.util.List;

import static fr.etu.steats.service.DiscountService.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

class DiscountServiceTest {
    private CustomerAccount customer;
    private Restaurant restaurant;
    private OrderAbstract order;

    @BeforeEach
    void setup() throws UnauthorizedOperationException {
        restaurant = new Restaurant("Mcdo", 1, "One's Lovin It", new Scheduler(), "1 rue de la paix");
        Menu menu = new Menu("Big mac", 12.0);

        restaurant.addMenuItem(menu);

        customer = spy(new CustomerAccount("Axel", "Delille", "aReallyStrongPassword ;)"));
        order = new OrderBuilder(DateTime.now().plusDays(1), customer)
                .setDeliveryAddress("930 route des colles")
                .addMenuItem(new OrderItem(menu, restaurant), 1)
                .build();
    }

    @Test
    void testComputeCustomerCredit() throws UnauthorizedOperationException {
        assertThrows(UnauthorizedOperationException.class, () -> computeCustomerCredit(new ArrayList<>(), -1, 0.8));
        assertThrows(UnauthorizedOperationException.class, () -> computeCustomerCredit(new ArrayList<>(), 1, -1));
        assertThrows(UnauthorizedOperationException.class, () -> computeCustomerCredit(new ArrayList<>(), 1, 1.1));

        assertFalse(computeCustomerCredit(new ArrayList<>(), 1, 0.1));

        assertEquals(0, customer.getCredit());
        assertTrue(computeCustomerCredit(List.of(order), 1, 0.1));
        assertEquals(1.2, customer.getCredit(), 0.001);

        customer.setCredit(0);

        assertEquals(0, customer.getCredit());
        assertTrue(computeCustomerCredit(List.of(order, order), 1, 0.1));
        assertEquals(2.4, customer.getCredit(), 0.001);
    }

    @Test
    void testIsUnderCumulatedOrder() {
        //There is no old order and no old discount
        when(customer.getPreviousOrder()).thenReturn(new ArrayList<>());
        assertFalse(isUnderCumulatedOrderDiscount(customer, restaurant));

        //There is old order but no old discount, so it'll create one
        when(customer.getPreviousOrder())
                .thenReturn(List.of(
                        order, order, order, order, order, order, order, order, order, order
                ));
        assertTrue(isUnderCumulatedOrderDiscount(customer, restaurant));

        //There is no old order but an old discount is active
        when(customer.getPreviousOrder()).thenReturn(new ArrayList<>());
        assertTrue(isUnderCumulatedOrderDiscount(customer, restaurant));
    }

    @Test
    void testComputePriceAfterDiscount() {
        //When there is no discount
        assertEquals(12, computePriceAfterDiscount(order), 0.001);

        //When there is a discount because we have a lot of old order
        when(customer.getPreviousOrder())
                .thenReturn(List.of(
                        order, order, order, order, order, order, order, order, order, order
                ));

        assertEquals(11.4, computePriceAfterDiscount(order), 0.001);
    }
}
