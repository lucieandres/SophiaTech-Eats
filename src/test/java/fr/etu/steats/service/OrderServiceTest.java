package fr.etu.steats.service;

import fr.etu.steats.account.AdminAccount;
import fr.etu.steats.account.CustomerAccount;
import fr.etu.steats.enums.EOrderStatus;
import fr.etu.steats.exception.UnauthorizedModificationException;
import fr.etu.steats.exception.UnauthorizedOperationException;
import fr.etu.steats.order.*;
import fr.etu.steats.restaurant.Menu;
import fr.etu.steats.restaurant.Restaurant;
import fr.etu.steats.utils.LoggerUtils;
import org.joda.time.DateTime;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class OrderServiceTest {
    private OrderService orderService;
    private CustomerAccount customer;
    private SingleOrder mockedSingleOrder;
    private SingleOrder mockedSingleOrder2;
    private CustomerAccount mockedCustomer;

    @BeforeEach
    void setup() throws UnauthorizedOperationException {
        this.customer = new CustomerAccount("Karim", "Charleux", "test");
        this.orderService = new OrderService();
        Menu menu = new Menu("Pizza", 12);
        Restaurant restaurant = new Restaurant("Pizza della mama", 1, "test", "1 rue de la paix");
        restaurant.addMenuItem(menu);
        LocalTime noon = new LocalTime(12, 0, 0);
        LocalTime now = new LocalTime();
        DateTime deliveryDate = noon.toDateTimeToday().plusDays(1);
        String deliveryAddress = "930 Rte des Colles, 06410 Biot";
        mockedSingleOrder = Mockito.mock(SingleOrder.class);
        mockedSingleOrder2 = Mockito.mock(SingleOrder.class);
        when(mockedSingleOrder2.getStatus()).thenReturn(EOrderStatus.WAITING_RESTAURANT_ACCEPTANCE);
        when(mockedSingleOrder2.getDeliveryAddress()).thenReturn("Test Address2");
        when(mockedSingleOrder2.getDeliveryDate()).thenReturn(now.plusMinutes(30).toDateTimeToday());
        when(mockedSingleOrder2.getCustomer()).thenReturn(customer);
        mockedCustomer = Mockito.mock(CustomerAccount.class);
        when(mockedSingleOrder.getStatus()).thenReturn(EOrderStatus.WAITING_RESTAURANT_ACCEPTANCE);
        when(mockedSingleOrder.getDeliveryAddress()).thenReturn("Test Address");
        when(mockedSingleOrder.getDeliveryDate()).thenReturn(now.plusMinutes(40).toDateTimeToday());
        when(mockedSingleOrder.getCustomer()).thenReturn(mockedCustomer);
        orderService.createSingleOrder(customer, List.of(new OrderItem(menu, restaurant)), deliveryDate, deliveryAddress);

        GroupOrder mockedGroupOrder = Mockito.mock(GroupOrder.class);
        mockedCustomer = Mockito.mock(CustomerAccount.class);
        when(mockedGroupOrder.getStatus()).thenReturn(EOrderStatus.WAITING_RESTAURANT_ACCEPTANCE);
        when(mockedGroupOrder.getDeliveryAddress()).thenReturn("Test Address");
        when(mockedGroupOrder.getDeliveryDate()).thenReturn(now.plusMinutes(40).toDateTimeToday());
        when(mockedGroupOrder.getCustomer()).thenReturn(mockedCustomer);
    }

    @Test
    void testFetchAllOrder() throws UnauthorizedOperationException {
        assertNotNull(orderService.fetchAllOrder());
        assertNotNull(customer.getOrders());
        assertEquals(1, orderService.fetchAllOrder().size());
        assertEquals(1, customer.getOrders().size());

        Menu menu = new Menu("Big Mac", 9.99);
        Restaurant restaurant = new Restaurant("Mc Donald", 2, "test", "1 rue de la paix");
        restaurant.addMenuItem(menu);
        DateTime deliveryDate = new DateTime().plusDays(1).withTime(new LocalTime(12, 0, 0));
        String deliveryAddress = "930 Rte des Colles, 06410 Biot";

        orderService.createSingleOrder(customer, List.of(new OrderItem(menu, restaurant)), deliveryDate, deliveryAddress);
        assertEquals(2, orderService.fetchAllOrder().size());
        assertEquals(2, customer.getOrders().size());

        CustomerAccount customer2 = new CustomerAccount("Axel", "Delille", "test");
        orderService.createSingleOrder(customer2, List.of(new OrderItem(menu, restaurant)), deliveryDate, deliveryAddress);
        assertEquals(3, orderService.fetchAllOrder().size());
        assertEquals(2, customer.getOrders().size());
        assertEquals(1, customer2.getOrders().size());
    }

    @Test
    void testCreateGroupOrder() throws UnauthorizedOperationException {
        DateTime deliveryDate = new DateTime().plusDays(1).withTime(new LocalTime(12, 0, 0));
        String deliveryAddress = "930 Rte des Colles, 06410 Biot";

        assertTrue(orderService.createGroupOrder(customer, deliveryDate, deliveryAddress));

        List<OrderAbstract> orders = orderService.fetchAllOrder();
        assertEquals(2, orders.size());

        assertTrue(orders.get(1) instanceof GroupOrder);

        GroupOrder groupOrder = (GroupOrder) orders.get(1);
        assertEquals(customer, groupOrder.getCustomer());
        assertTrue(deliveryDate.isEqual(groupOrder.getDeliveryDate()));
        assertEquals(deliveryAddress, groupOrder.getDeliveryAddress());

        assertFalse(orderService.createGroupOrder(null, deliveryDate, deliveryAddress));
    }

    @Test
    void testAddSubOrder() throws UnauthorizedOperationException {
        DateTime groupOrderDeliveryDate = new DateTime().plusDays(1).withTime(new LocalTime(12, 0, 0));
        String groupOrderDeliveryAddress = "930 Rte des Colles, 06410 Biot";
        OrderAbstract groupOrder = new OrderBuilder(groupOrderDeliveryDate, customer)
                .setDeliveryAddress(groupOrderDeliveryAddress)
                .build();

        Menu menu = new Menu("Pizza", 12);
        Restaurant restaurant = new Restaurant("Pizza della mama", 1, "test", "1 rue de la paix");
        restaurant.addMenuItem(menu);

        assertTrue(orderService.addOrderToGroupOrder(customer, List.of(new OrderItem(menu, restaurant)), groupOrder));

        List<OrderAbstract> subOrders = ((GroupOrder) groupOrder).getSubOrders();
        assertEquals(1, subOrders.size());

        OrderAbstract subOrder = subOrders.get(0);
        assertEquals(groupOrder.getDeliveryAddress(), subOrder.getDeliveryAddress());
        assertEquals(groupOrder.getDeliveryDate(), subOrder.getDeliveryDate());
    }

    @Test
    void testFetchAllOrdersByCustomer() throws UnauthorizedOperationException {
        CustomerAccount customer2 = new CustomerAccount("user1", "name1", "password1");
        DateTime deliveryDate = new DateTime().plusDays(1).withTime(new LocalTime(12, 0, 0));
        String deliveryAddress = "930 Rte des Colles, 06410 Biot";
        Menu menu = new Menu("Pizza", 12);
        Restaurant restaurant = new Restaurant("Pizza della mama", 1, "test", "1 rue de la paix");
        restaurant.addMenuItem(menu);

        orderService.createSingleOrder(customer, List.of(new OrderItem(menu, restaurant)), deliveryDate, deliveryAddress);
        orderService.createSingleOrder(customer2, List.of(new OrderItem(menu, restaurant)), deliveryDate, deliveryAddress);

        List<OrderAbstract> orders = orderService.fetchAllOrdersByCustomer(customer);
        assertEquals(2, orders.size());
        assertTrue(orders.get(0) instanceof SingleOrder);
        assertTrue(orders.get(1) instanceof SingleOrder);
        assertEquals(customer, orders.get(0).getCustomer());
        assertEquals(customer, orders.get(1).getCustomer());
    }

    @Test
    void testGetGroupOrderById() throws UnauthorizedOperationException {
        CustomerAccount customer2 = new CustomerAccount("user2", "name2", "password2");
        DateTime deliveryDate = new DateTime().plusDays(1).withTime(new LocalTime(12, 0, 0));
        String deliveryAddress = "930 Rte des Colles, 06410 Biot";

        OrderAbstract groupOrder = new OrderBuilder(deliveryDate, customer)
                .setDeliveryAddress(deliveryAddress)
                .build();

        int groupId = groupOrder.getId();

        assertTrue(orderService.createGroupOrder(customer2, deliveryDate, deliveryAddress));
        orderService.fetchAllOrder().add(groupOrder);

        assertNull(orderService.getGroupOrderById(123456));

        GroupOrder fetchedGroupOrder = orderService.getGroupOrderById(groupId);
        assertNotNull(fetchedGroupOrder);
        assertEquals(groupId, fetchedGroupOrder.getId());
        assertEquals(customer, fetchedGroupOrder.getCustomer());
        assertTrue(deliveryDate.isEqual(fetchedGroupOrder.getDeliveryDate()));
        assertEquals(deliveryAddress, fetchedGroupOrder.getDeliveryAddress());
    }

    @Test
    void testJoinSingleOrderWithSingleOrder() throws UnauthorizedOperationException {
        OrderItem orderItem = new OrderItem(new Menu("Pizza", 12), new Restaurant("Pizza della mama", 1, "test", "1 rue de la paix"));
        orderItem.setStatus(EOrderStatus.WAITING_RESTAURANT_ACCEPTANCE);

        OrderAbstract joinOrder = new OrderBuilder(mockedSingleOrder.getDeliveryDate(), mockedCustomer)
                .setDeliveryAddress(mockedSingleOrder.getDeliveryAddress())
                .addMenuItems(List.of(orderItem))
                .build();

        joinOrder.getItems().forEach(item -> item.setStatus(EOrderStatus.WAITING_RESTAURANT_ACCEPTANCE));

        orderService.fetchAllOrder().add(joinOrder);
        when(mockedCustomer.getOrders()).thenReturn(List.of(mockedSingleOrder));
        when(mockedSingleOrder.getStatus()).thenReturn(EOrderStatus.WAITING_DELIVER_ACCEPTANCE);
        assertTrue(orderService.joinOrder(mockedSingleOrder, joinOrder));
        OrderAbstract result = orderService.fetchAllOrder().get(orderService.fetchAllOrder().size() - 1);
        assertNotNull(result);
        assertEquals(2, ((GroupOrder) result).getSubOrders().size());
        assertTrue(orderService.fetchAllOrder().contains(result));
        assertTrue(orderService.fetchAllOrder().contains(joinOrder));
        assertTrue(((GroupOrder) result).getSubOrders().stream().anyMatch(order -> order.getCustomer().equals(mockedCustomer)));
    }

    @Test
    void testJoinOrderThrowsExceptionWhenOrderInDelivery() {
        when(mockedSingleOrder.getStatus()).thenReturn(EOrderStatus.IN_DELIVERY);

        assertThrows(UnauthorizedOperationException.class, () -> orderService.joinOrder(mockedSingleOrder, mockedSingleOrder2));
    }


    @Test
    void testAddGroupOrderToGroupOrder() throws UnauthorizedOperationException {
        DateTime deliveryDate = new DateTime().plusMinutes(1);
        String deliveryAddress = "930 Rte des Colles, 06410 Biot";

        OrderAbstract parentGroupOrder = new OrderBuilder(deliveryDate, customer)
                .setDeliveryAddress(deliveryAddress)
                .build();

        assertTrue(orderService.addOrderToGroupOrder(customer, new ArrayList<>(), parentGroupOrder));

        List<OrderAbstract> subOrders = ((GroupOrder) parentGroupOrder).getSubOrders();
        assertEquals(1, subOrders.size());

        OrderAbstract subOrder = subOrders.get(0);
        assertEquals(parentGroupOrder.getDeliveryAddress(), subOrder.getDeliveryAddress());
        assertEquals(parentGroupOrder.getDeliveryDate(), subOrder.getDeliveryDate());
    }

    @Test
    void testUpdateOrderItems() throws UnauthorizedOperationException, UnauthorizedModificationException {
        DateTime deliveryDate = LocalDateTime.now().plusDays(1).withHourOfDay(12).withMinuteOfHour(0).withSecondOfMinute(0).toDateTime();
        String deliveryAddress = "930 Rte des Colles, 06410 Biot";
        Menu menu = new Menu("Pizza", 12);
        Restaurant restaurant = new Restaurant("Pizza della mama", 1, "test", "1 rue de la paix");
        restaurant.addMenuItem(menu);

        orderService.createSingleOrder(customer, List.of(new OrderItem(menu, restaurant)), deliveryDate, deliveryAddress);
        SingleOrder order = (SingleOrder) orderService.fetchAllOrder().get(0);
        assertEquals(1, order.getItems().size());

        assertTrue(orderService.updateOrderItems(order, List.of(new OrderItem(new Menu("Pizza 4 fromagi", 11), restaurant))));

        assertEquals(1, order.getItems().size());
        assertEquals("Pizza 4 fromagi", order.getItems().get(0).getMenu().getName());
    }

    @Test
    void testUpdateOrderDeliveryDate() throws UnauthorizedOperationException {
        DateTime deliveryDate = LocalDateTime.now().plusDays(1).withHourOfDay(12).withMinuteOfHour(0).withSecondOfMinute(0).toDateTime();
        String deliveryAddress = "930 Rte des Colles, 06410 Biot";
        Menu menu = new Menu("Pizza", 12);
        Restaurant restaurant = new Restaurant("Pizza della mama", 1, "test", "1 rue de la paix");
        restaurant.addMenuItem(menu);

        orderService.createSingleOrder(customer, List.of(new OrderItem(menu, restaurant)), deliveryDate, deliveryAddress);
        SingleOrder order = (SingleOrder) orderService.fetchAllOrder().get(1);
        assertTrue(deliveryDate.isEqual(order.getDeliveryDate()));

        DateTime newDeliveryDate = new DateTime();
        try {
            orderService.updateOrderDeliveryDate(order, newDeliveryDate);
        } catch (UnauthorizedModificationException e) {
            LoggerUtils.log(Level.SEVERE, e.getMessage());
        }
        assertTrue(newDeliveryDate.isEqual(order.getDeliveryDate()));
    }

    @Test
    void testUpdateOrderDeliveryAddress() throws UnauthorizedOperationException {
        DateTime deliveryDate = LocalDateTime.now().plusDays(1).withHourOfDay(12).withMinuteOfHour(0).withSecondOfMinute(0).toDateTime();
        String deliveryAddress = "930 Rte des Colles, 06410 Biot";
        Menu menu = new Menu("Pizza", 12);
        Restaurant restaurant = new Restaurant("Pizza della mama", 1, "test", "1 rue de la paix");
        restaurant.addMenuItem(menu);

        orderService.createSingleOrder(customer, List.of(new OrderItem(menu, restaurant)), deliveryDate, deliveryAddress);
        SingleOrder order = (SingleOrder) orderService.fetchAllOrder().get(1);
        assertTrue(deliveryDate.isEqual(order.getDeliveryDate()));

        String newDeliveryAddress = "1 Place J. Bermond,  06560 Valbonne";
        try {
            orderService.updateOrderDeliveryAddress(order, newDeliveryAddress);
        } catch (UnauthorizedModificationException e) {
            LoggerUtils.log(Level.SEVERE, e.getMessage());
        }
        assertEquals(newDeliveryAddress, order.getDeliveryAddress());
    }

    @Test
    void testCancelOrder() {
        OrderAbstract order = orderService.fetchAllOrder().get(0);
        try {
            assertTrue(orderService.cancelOrder(order));
            assertEquals(EOrderStatus.CANCELED, order.getStatus());

        } catch (UnauthorizedModificationException e) {
            LoggerUtils.log(Level.SEVERE, e.getMessage());
        }
    }

    @Test
    void testCreateBuffetOrder() throws UnauthorizedOperationException {
        AdminAccount admin = new AdminAccount("admin", "admin", "admin");
        DateTime deliveryDate = new DateTime().plusDays(1).withTime(new LocalTime(12, 0, 0));
        Menu menu = new Menu("Pizza", 12);
        Restaurant restaurant = new Restaurant("Pizza della mama", 1, "test", "1 rue de la paix");
        restaurant.addMenuItem(menu);

        assertEquals(1, orderService.fetchAllOrder().size());
        assertEquals(1, customer.getOrders().size());

        assertTrue(orderService.createBuffetOrder(customer, deliveryDate, List.of(new OrderItem(menu, restaurant)), admin, restaurant));

        assertEquals(2, orderService.fetchAllOrder().size());
        assertEquals(2, customer.getOrders().size());
    }

    @Test
    void testCreateBufferOrderWithIncorrectField() throws UnauthorizedOperationException {
        AdminAccount admin = new AdminAccount("admin", "admin", "admin");
        DateTime deliveryDate = new DateTime().plusDays(1).withTime(new LocalTime(12, 0, 0));
        Menu menu = new Menu("Pizza", 12);
        Restaurant restaurant = new Restaurant("Pizza della mama", 1, "test", "1 rue de la paix");
        restaurant.addMenuItem(menu);

        assertFalse(orderService.createBuffetOrder(null, null, null, null, null));
        assertFalse(orderService.createBuffetOrder(customer, null, null, null, null));
        assertFalse(orderService.createBuffetOrder(customer, deliveryDate, null, null, null));
        assertFalse(orderService.createBuffetOrder(customer, deliveryDate, List.of(new OrderItem(menu, restaurant)), null, null));
        assertFalse(orderService.createBuffetOrder(customer, deliveryDate, List.of(new OrderItem(menu, restaurant)), admin, null));
    }
}
