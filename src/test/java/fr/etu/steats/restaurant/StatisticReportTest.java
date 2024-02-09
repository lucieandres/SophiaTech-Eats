package fr.etu.steats.restaurant;

import fr.etu.steats.enums.EOrderStatus;
import fr.etu.steats.order.OrderItem;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class StatisticReportTest {

    @Test
    public void testConstructorWithMultipleRestaurants() {
        Set<Restaurant> restaurants = new HashSet<>();
        restaurants.add(createSampleRestaurant("Restaurant 1", 5, 1));
        restaurants.add(createSampleRestaurant("Restaurant 2", 3, 2));

        StatisticReport report = new StatisticReport(restaurants);

        assertEquals(8, report.getNbOrders());
        assertEquals(8, report.getNbOrdersWaitingPayment());
        assertEquals(0, report.getNbOrdersWaitingRestaurantAcceptance());
        assertEquals(0, report.getNbOrdersInPreparation());
        assertEquals(0, report.getNbOrdersWaitingDeliverAcceptance());
        assertEquals(0, report.getNbOrdersInDelivery());
        assertEquals(0, report.getNbOrdersFinish());
        assertEquals(0, report.getNbOrdersCancelled());
        assertEquals(2, report.getNbRestaurants());
        assertNotNull(report.getDateCreated());
        assertFalse(report.toString().isEmpty());
    }

    @Test
    public void testConstructorWithSingleRestaurant() {
        Restaurant restaurant = createSampleRestaurant("Restaurant 1", 5, 1);

        StatisticReport report = new StatisticReport(restaurant);

        assertEquals(5, report.getNbOrders()); // 5 orders from Restaurant 1
        assertEquals(5, report.getNbOrdersWaitingPayment()); // 5 order waiting for payment
        assertEquals(0, report.getNbOrdersWaitingRestaurantAcceptance());
        assertEquals(0, report.getNbOrdersInPreparation());
        assertEquals(0, report.getNbOrdersWaitingDeliverAcceptance());
        assertEquals(0, report.getNbOrdersInDelivery());
        assertEquals(0, report.getNbOrdersFinish());
        assertEquals(0, report.getNbOrdersCancelled());
        assertEquals(1, report.getNbRestaurants());
        assertNotNull(report.getDateCreated());
        assertFalse(report.toString().isEmpty());
    }

    @Test
    public void testConstructorWithDifferentOrderStatus() {
        Restaurant restaurant = createSampleRestaurantWithDifferentStatus();

        StatisticReport report = new StatisticReport(restaurant);

        assertEquals(7, report.getNbOrders()); // 5 orders from Restaurant 1
        assertEquals(1, report.getNbOrdersWaitingPayment()); // 2 orders waiting for payment
        assertEquals(1, report.getNbOrdersWaitingRestaurantAcceptance()); // 1 order waiting for restaurant acceptance
        assertEquals(1, report.getNbOrdersInPreparation()); // 1 order in preparation
        assertEquals(1, report.getNbOrdersWaitingDeliverAcceptance()); // 1 order waiting for delivery acceptance
        assertEquals(1, report.getNbOrdersInDelivery()); // 0 orders in delivery
        assertEquals(1, report.getNbOrdersFinish()); // 0 finished orders
        assertEquals(1, report.getNbOrdersCancelled()); // 0 cancelled orders
        assertEquals(1, report.getNbRestaurants());
        assertNotNull(report.getDateCreated());
        assertFalse(report.toString().isEmpty());
    }

    private Restaurant createSampleRestaurant(String name, int numOrders, int id) {
        Restaurant restaurant = new Restaurant(name, id, "test", "1 rue de la paix");
        for (int i = 0; i < numOrders; i++) {
            OrderItem orderItem = new OrderItem(new Menu("Menu " + i, 10.0), restaurant);
            restaurant.addOrder(orderItem);
        }
        return restaurant;
    }


    private Restaurant createSampleRestaurantWithDifferentStatus() {
        Restaurant restaurant = new Restaurant("Restaurant 1", 1, "password", "1 rue de la paix");
        for (int i = 0; i < 7; i++) {
            OrderItem order = new OrderItem(new Menu("Menu " + i, 10.0), restaurant);
            switch (i) {
                case 0:
                    order.setStatus(EOrderStatus.WAITING_PAYMENT);
                    break;
                case 1:
                    order.setStatus(EOrderStatus.WAITING_RESTAURANT_ACCEPTANCE);
                    break;
                case 2:
                    order.setStatus(EOrderStatus.IN_PREPARATION);
                    break;
                case 3:
                    order.setStatus(EOrderStatus.WAITING_DELIVER_ACCEPTANCE);
                    break;
                case 4:
                    order.setStatus(EOrderStatus.IN_DELIVERY);
                    break;
                case 5:
                    order.setStatus(EOrderStatus.FINISH);
                    break;
                case 6:
                    order.setStatus(EOrderStatus.CANCELED);
                    break;
            }
            restaurant.addOrder(order);
        }
        return restaurant;
    }
}
