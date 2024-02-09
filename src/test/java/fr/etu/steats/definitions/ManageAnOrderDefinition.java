package fr.etu.steats.definitions;

import fr.etu.steats.STEats;
import fr.etu.steats.enums.EOrderStatus;
import fr.etu.steats.exception.BadPasswordException;
import fr.etu.steats.exception.NoAccountFoundException;
import fr.etu.steats.exception.UnauthorizedModificationException;
import fr.etu.steats.exception.UnauthorizedOperationException;
import fr.etu.steats.order.GroupOrder;
import fr.etu.steats.order.OrderAbstract;
import fr.etu.steats.order.OrderItem;
import fr.etu.steats.order.SingleOrder;
import fr.etu.steats.registry.CustomerRegistry;
import fr.etu.steats.restaurant.Menu;
import fr.etu.steats.restaurant.Restaurant;
import fr.etu.steats.utils.LoggerUtils;
import fr.etu.steats.utils.Scheduler;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.mockito.Mockito;

import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ManageAnOrderDefinition {
    private CustomerRegistry stEats;
    private Restaurant restaurant;
    private String menuName;
    private DateTime deliveryDate;
    private String deliveryAddress;
    private OrderAbstract order;
    private String errorMessage;


    private Scheduler scheduler;

    @Given("The customer {string} {string} with the password {string}.")
    public void givenTheCustomer(String firstname, String lastname, String password) throws InterruptedException, BadPasswordException, NoAccountFoundException {
        this.scheduler = Mockito.mock(Scheduler.class);
        doNothing().when(scheduler).waitTenMinutes();

        if (stEats == null) {
            stEats = new STEats(scheduler).getCustomerRegistry();
        }
        stEats.customerLogin(firstname, lastname, password);
    }

    @When("He chooses the delivery date {string}.")
    public void whenHeChooseDeliveryDate(String deliveryDateAndTime) {
        DateTimeFormatter formatter = DateTimeFormat.forPattern("dd/MM/yyyy HH:mm:ss");
        this.deliveryDate = formatter.parseDateTime(deliveryDateAndTime);
    }

    @When("He chooses the delivery address {string}.")
    public void whenHeChooseDeliveryAddress(String deliveryAddress) {
        this.deliveryAddress = deliveryAddress;
    }

    @When("He chooses the restaurant with id {int}.")
    public void whenHeChooseRestaurant(int restaurantId) {
        this.restaurant = stEats.getRestaurantService().findRestaurantById(restaurantId);

    }

    @When("He chooses the menu {string}.")
    public void whenHeChooseMenu(String menuName) {
        this.menuName = menuName;
    }

    @When("He validates his order.")
    public void whenHeValidateTheOrder() {
        try {
            int restaurantId = restaurant.getId();
            assertTrue(stEats.placeSingleOrder(Map.of(restaurantId, List.of(menuName)), deliveryDate, deliveryAddress));
            this.order = stEats.getCustomerAccount().getOrders().get(0);
        } catch (UnauthorizedOperationException e) {
            errorMessage = e.getMessage();
            LoggerUtils.log(Level.SEVERE, e.getMessage());
        }
    }

    @Then("His order is registered in the system.")
    public void thenHisOrderIsRegistered() {
        List<OrderAbstract> orders = stEats.getCustomerAccount().getOrders();
        assertEquals(1, orders.size());

        SingleOrder order = (SingleOrder) orders.get(0);
        assertEquals(1, order.getItems().size());

        OrderItem item = order.getItems().get(0);
        Menu menu = restaurant.getMenuItem(menuName);

        assertEquals(menu, item.getMenu());
        assertEquals(restaurant, item.getRestaurant());
    }

    @When("He validates the group order.")
    public void whenHeValidateTheGroupOrder() {
        try {
            assertTrue(stEats.placeGroupOrder(deliveryDate, deliveryAddress));
        } catch (UnauthorizedOperationException e) {
            errorMessage = e.getMessage();
            LoggerUtils.log(Level.SEVERE, e.getMessage());
        }
    }

    @Then("The group order is registered in the system and we should have {int} order.")
    public void thenTheGroupOrderIsRegistered(int n) {
        List<OrderAbstract> orders = stEats.getOrderService().fetchAllOrder();
        assertEquals(n, orders.size());
        assertTrue(orders.get(0) instanceof GroupOrder);
        this.order = orders.get(0);
    }

    @When("He validates his suborder.")
    public void whenHeValidatesSubOrder() {
        try {
            int restaurantId = restaurant.getId();
            Restaurant restaurant = stEats.getRestaurantService().findRestaurantById(restaurantId);
            Menu menu = restaurant.getMenuItem(menuName);
            assertTrue(stEats.addToGroupOrder(List.of(new OrderItem(menu, restaurant)), this.order));
        } catch (UnauthorizedOperationException e) {
            errorMessage = e.getMessage();
            LoggerUtils.log(Level.SEVERE, e.getMessage());
        }
    }

    @Then("His order is registered in the group order.")
    public void thenHisOrderIsRegisteredInGroupOrder() {

        assertNotNull(this.order);

        List<OrderAbstract> orders = stEats
                .getCustomerAccount()
                .getOrders()
                .stream()
                .filter(order -> order instanceof SingleOrder)
                .toList();

        assertFalse(orders.isEmpty());
        assertTrue(((GroupOrder) order).getSubOrders().containsAll(orders));
    }

    @When("The restaurant prepares the order.")
    public void theRestaurantPreparesTheOrder() throws InterruptedException {
        restaurant.prepareOrder(1);
    }

    @When("The restaurant cancels the order.")
    public void theRestaurantCancelsTheOrder() {
        restaurant.cancelOrder(1);
    }

    @When("10 minutes have passed.")
    public void minutesHavePassed() throws InterruptedException {
        verify(scheduler, times(1)).waitTenMinutes();
    }

    @Then("The order status is {string}.")
    public void theOrderStatusIs(String status) {
        EOrderStatus statusEnum = EOrderStatus.valueOf(status);
        assertEquals(statusEnum, stEats.getOrderService().fetchAllOrder().get(0).getStatus());
    }

    @Then("The restaurant have {int} order.")
    public void theRestaurantHaveOrder(int arg0) {
        assertEquals(arg0, restaurant.getOrders().size());
    }

    @Then("An order error message should be printed.")
    public void thenAnErrorMessageShouldBePrinted() {
        assertNotNull(errorMessage);
    }

    @And("He can update his order.")
    public void heCanUpdateHisOrder() {
        assertTrue(stEats.getCustomerAccount().getOrders().get(0).statusAllowsUpdate());
    }

    @And("He pays his order.")
    public void hePaysHisOrder() throws UnauthorizedOperationException {
        SingleOrder order = (SingleOrder) stEats.getCustomerAccount().getOrders().get(0);
        order.getItems().forEach(item -> item.setStatus(EOrderStatus.WAITING_PAYMENT));
        order.setWaitingRestaurantAcceptance();
    }

    @When("He update his order with the menu {string}.")
    public void heUpdateHisOrderWithTheMenu(String arg0) {
        try {
            List<OrderItem> items = List.of(new OrderItem(restaurant.getMenuItem(arg0), restaurant));
            OrderAbstract order = stEats.getOrderService().fetchAllOrder().get(0);
            assertTrue(stEats.updateOrderItem(order.getId(), items));
        } catch (UnauthorizedOperationException | UnauthorizedModificationException e) {
            errorMessage = e.getMessage();
            LoggerUtils.log(Level.SEVERE, e.getMessage());
        }
    }

    @When("He update the delivery date to {string}.")
    public void heUpdateTheDeliveryDateTo(String deliveryDateAndTime) throws UnauthorizedOperationException, UnauthorizedModificationException {
        DateTimeFormatter formatter = DateTimeFormat.forPattern("dd/MM/yyyy HH:mm:ss");
        this.deliveryDate = formatter.parseDateTime(deliveryDateAndTime);
        assertTrue(stEats.updateOrderDeliveryDate(order.getId(), deliveryDate));
    }

    @When("He update the delivery address to {string}.")
    public void heUpdateTheDeliveryAddressTo(String newAddress) {
        try {
            assertTrue(stEats.updateOrderDeliveryAddress(1, newAddress));
        } catch (UnauthorizedOperationException | UnauthorizedModificationException e) {
            errorMessage = e.getMessage();
            LoggerUtils.log(Level.SEVERE, e.getMessage());
        }
    }

    @Then("His order is updated in the system with the menu {string}.")
    public void hisOrderIsUpdatedInTheSystemWithTheMenu(String menu) {
        SingleOrder order = (SingleOrder) stEats.getCustomerAccount().getOrders().get(0);
        assertEquals(menu, order.getItems().get(0).getMenu().getName());
    }

    @Then("His order is updated in the system with the good delivery date.")
    public void hisOrderIsUpdatedInTheSystemWithTheDeliveryDate() {
        assertTrue(deliveryDate.isEqual(this.order.getDeliveryDate()));
    }

    @Then("His order is updated in the system with the delivery address {string}.")
    public void hisOrderIsUpdatedInTheSystemWithTheDeliveryAddress(String arg0) {
        SingleOrder order = (SingleOrder) stEats.getCustomerAccount().getOrders().get(0);
        assertEquals(arg0, order.getDeliveryAddress());
    }

    @When("He update the delivery date of the group order to {string}.")
    public void heUpdateTheDeliveryDateOfTheGroupOrderTo(String deliveryDateAndTime) {
        DateTimeFormatter formatter = DateTimeFormat.forPattern("dd/MM/yyyy HH:mm:ss");
        this.deliveryDate = formatter.parseDateTime(deliveryDateAndTime);
        try {
            stEats.updateOrderDeliveryDate(this.order.getId(), deliveryDate);
        } catch (UnauthorizedOperationException | UnauthorizedModificationException e) {
            errorMessage = e.getMessage();
            LoggerUtils.log(Level.SEVERE, e.getMessage());
        }
    }

    @Then("The group order is not updated in the system.")
    public void theGroupOrderIsNotUpdatedInTheSystem() {
        assertThrows(UnauthorizedOperationException.class, () ->
                stEats.updateOrderDeliveryAddress(this.order.getId(), "new address"));

        assertNotEquals("new address", stEats.getOrderService().findOrderById(this.order.getId()).getDeliveryAddress());
    }

    @And("All suborders are updated with the new delivery date.")
    public void allSubordersAreUpdatedWithTheNewDeliveryDate() {
        for (OrderAbstract order : ((GroupOrder) order).getSubOrders()) {
            assertEquals(order.getDeliveryDate(), this.order.getDeliveryDate());
        }
    }

    @When("He update the delivery address of the group order to {string}.")
    public void heUpdateTheDeliveryAddressOfTheGroupOrderTo(String arg0) {
        try {
            stEats.updateOrderDeliveryAddress(this.order.getId(), arg0);
        } catch (UnauthorizedOperationException | UnauthorizedModificationException e) {
            errorMessage = e.getMessage();
            LoggerUtils.log(Level.SEVERE, e.getMessage());
        }
    }

    @Then("The group order is updated in the system with the good delivery date.")
    public void theGroupOrderIsUpdatedInTheSystemWithTheDeliveryDate() {
        assertEquals(deliveryDate, stEats.getOrderService().findOrderById(this.order.getId()).getDeliveryDate());
    }

    @Then("The group order is updated in the system with the delivery address {string}.")
    public void theGroupOrderIsUpdatedInTheSystemWithTheDeliveryAddress(String arg0) {
        assertEquals(arg0, stEats.getOrderService().findOrderById(this.order.getId()).getDeliveryAddress());
    }

    @And("All suborders are updated with the new delivery address.")
    public void allSubordersAreUpdatedWithTheNewDeliveryAddress() {
        for (OrderAbstract order : ((GroupOrder) this.order).getSubOrders()) {
            assertEquals(order.getDeliveryAddress(), this.order.getDeliveryAddress());
        }
    }

    @When("He cancels the group order.")
    public void heCancelsTheGroupOrder() {
        try {
            stEats.cancelOrder(this.order.getId());
        } catch (UnauthorizedOperationException | UnauthorizedModificationException e) {
            errorMessage = e.getMessage();
            LoggerUtils.log(Level.SEVERE, e.getMessage());
        }
    }

    @Then("The group order is not cancelled in the system.")
    public void theGroupOrderIsNotCancelledInTheSystem() {
        assertThrows(UnauthorizedModificationException.class, () -> stEats.cancelOrder(this.order.getId()));

        assertNotEquals(EOrderStatus.CANCELED, stEats.getOrderService().findOrderById(this.order.getId()).getStatus());
    }

    @Then("The group order is cancelled in the system.")
    public void theGroupOrderIsCancelledInTheSystem() {
        assertNotEquals(EOrderStatus.IN_PREPARATION, stEats.getOrderService().findOrderById(this.order.getId()).getStatus());
    }

    @And("The order is already in preparation.")
    public void theOrderIsAlreadyInPreparation() {
        for (OrderAbstract order : ((GroupOrder) this.order).getSubOrders()) {
            order.getItems().get(0).setStatus(EOrderStatus.IN_PREPARATION);
        }
    }
}
