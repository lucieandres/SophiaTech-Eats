package fr.etu.steats.definitions;

import fr.etu.steats.STEats;
import fr.etu.steats.account.CustomerAccount;
import fr.etu.steats.enums.ECustomerStatus;
import fr.etu.steats.enums.EOrderStatus;
import fr.etu.steats.exception.BadPasswordException;
import fr.etu.steats.exception.NoAccountFoundException;
import fr.etu.steats.exception.UnauthorizedModificationException;
import fr.etu.steats.exception.UnauthorizedOperationException;
import fr.etu.steats.order.*;
import fr.etu.steats.registry.CustomerRegistry;
import fr.etu.steats.restaurant.Menu;
import fr.etu.steats.restaurant.Restaurant;
import fr.etu.steats.service.DiscountService;
import fr.etu.steats.service.NotificationService;
import fr.etu.steats.service.OrderService;
import fr.etu.steats.service.PaymentService;
import fr.etu.steats.utils.LoggerUtils;
import fr.etu.steats.utils.Scheduler;
import fr.etu.steats.utils.UserLevel;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import static fr.etu.steats.enums.EOrderStatus.FINISH;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class PassAnOrderDefinition {
    private final Map<String, Integer> menus = new HashMap<>();
    private CustomerRegistry customerRegistry;
    private Restaurant restaurant;
    private DateTime deliveryDate;
    private String deliveryAddress;
    private GroupOrder groupOrder;
    private Scheduler scheduler;
    private PaymentService paymentService;
    private String errorMessage;
    private Boolean isUserNotificationReceived = false;
    private int numberOfParticipant;
    private int numberOfAddedOrderForDiscount = 0;

    @Given("the customer {string} {string} with the password {string}")
    public void givenTheCustomer(String firstname, String lastname, String password) throws InterruptedException, BadPasswordException, NoAccountFoundException {
        this.scheduler = Mockito.mock(Scheduler.class);
        this.paymentService = spy(new PaymentService());
        doNothing().when(scheduler).waitTenMinutes();
        doReturn(true).when(paymentService).pay(anyDouble());
        customerRegistry = new STEats(this.paymentService, scheduler).getCustomerRegistry();
        customerRegistry.customerLogin(firstname, lastname, password);
    }

    @When("he chooses the delivery date {string}")
    public void whenHeChooseDeliveryDate(String deliveryDateAndTime) {
        DateTimeFormatter formatter = DateTimeFormat.forPattern("dd/MM/yyyy HH:mm:ss");
        this.deliveryDate = formatter.parseDateTime(deliveryDateAndTime);
    }

    @When("he chooses the delivery address {string}")
    public void whenHeChooseDeliveryAddress(String deliveryAddress) {
        this.deliveryAddress = deliveryAddress;
    }

    @When("he chooses the restaurant with id {int}")
    public void whenHeChooseRestaurant(int restaurantId) {
        this.restaurant = customerRegistry.getRestaurantService().findRestaurantById(restaurantId);

    }

    @When("he chooses the menu {string}")
    public void whenHeChooseMenu(String menuName) {
        for (String menu : menuName.split(" / ")) {
            if (!menus.containsKey(menu)) {
                this.menus.put(menu, 1);
            } else {
                this.menus.put(menu, this.menus.get(menu) + 1);
            }
        }
    }

    @When("he validates his order")
    public void whenHeValidateTheOrder() {
        try (MockedStatic<NotificationService> mockedStatic = mockStatic(NotificationService.class)) {
            Map<Integer, List<String>> items = new HashMap<>();
            menus.forEach((menuName, quantity) -> {
                for (int i = 0; i < quantity; i++) {
                    if (!items.containsKey(restaurant.getId())) {
                        items.put(restaurant.getId(), new ArrayList<>(List.of(menuName)));
                    } else {
                        items.get(restaurant.getId()).add(menuName);
                    }
                }
            });
            customerRegistry.placeSingleOrder(items, deliveryDate, deliveryAddress);
            mockedStatic.verify(() -> NotificationService.sendNotificationToUser(eq(UserLevel.USER), anyString(), anyString()), times(1));
            mockedStatic.verify(() -> NotificationService.sendNotificationToUser(eq(UserLevel.RESTAURANT_MANAGER), anyString(), anyString()), times(1));
            isUserNotificationReceived = true;
        } catch (UnauthorizedOperationException e) {
            errorMessage = e.getMessage();
            LoggerUtils.log(Level.SEVERE, e.getMessage());
        }
    }

    @When("he validates the after work order")
    public void whenHeValidateTheAfterWorkOrder() {
        try (MockedStatic<NotificationService> mockedStatic = mockStatic(NotificationService.class)) {
            customerRegistry.placeAfterWorkOrder(restaurant.getId(), deliveryDate, numberOfParticipant);
            mockedStatic.verify(() -> NotificationService.sendNotificationToUser(eq(UserLevel.USER), anyString(), anyString()), times(1));
            mockedStatic.verify(() -> NotificationService.sendNotificationToUser(eq(UserLevel.RESTAURANT_MANAGER), anyString(), anyString()), times(1));
            isUserNotificationReceived = true;
        } catch (UnauthorizedOperationException | IllegalArgumentException | NullPointerException e) {
            errorMessage = e.getMessage();
            LoggerUtils.log(Level.SEVERE, e.getMessage());
        }

    }

    @Then("his order is registered with {int} orders in the system")
    public void thenHisOrderIsRegistered(int nbOrders) {
        List<OrderAbstract> orders = customerRegistry.getCustomerAccount().getOrders();
        assertEquals(1 + numberOfAddedOrderForDiscount, orders.size());

        SingleOrder order = (SingleOrder) orders.get(0);
        assertEquals(nbOrders, order.getItems().size());

        OrderItem item = order.getItems().get(0);
        Menu menu = restaurant.getMenuItem(item.getMenu().getName());
        assertEquals(menu, item.getMenu());
        assertTrue(restaurant.getMenuItems().contains(item.getMenu()));
    }

    @Then("his after work order is registered in the system")
    public void thenHisAfterWorkOrderIsRegistered() {
        List<OrderAbstract> orders = customerRegistry.getCustomerAccount().getOrders();
        assertEquals(1, orders.size());
    }

    @Then("his single order with multiple item is registered in the system")
    public void thenHisSingleOrderWithMultipleItemIsRegistered() {
        List<OrderAbstract> orders = customerRegistry.getCustomerAccount().getOrders();
        assertEquals(1, orders.size());

        SingleOrder order = (SingleOrder) orders.get(0);

        for (OrderItem item : order.getItems()) {
            Menu menu = restaurant.getMenuItem(item.getMenu().getName());
            assertEquals(menu, item.getMenu());
            assertTrue(restaurant.getMenuItems().contains(item.getMenu()));
        }
    }

    @When("he validates the group order")
    public void whenHeValidateTheGroupOrder() {
        try {
            customerRegistry.placeGroupOrder(deliveryDate, deliveryAddress);
        } catch (UnauthorizedOperationException e) {
            errorMessage = e.getMessage();
            LoggerUtils.log(Level.SEVERE, e.getMessage());
        }
    }

    @Then("the group order is registered in the system")
    public void thenTheGroupOrderIsRegistered() {
        List<OrderAbstract> orders = customerRegistry.getOrderService().fetchAllOrder();
        assertEquals(1, orders.size());
        assertTrue(orders.get(0) instanceof GroupOrder);
        this.groupOrder = (GroupOrder) orders.get(0);
    }

    @When("he validates his suborder")
    public void whenHeValidatesSubOrder() {
        try {
            List<OrderItem> items = new ArrayList<>();
            menus.forEach((menuName, quantity) -> {
                Menu menu = restaurant.getMenuItem(menuName);
                for (int i = 0; i < quantity; i++) {
                    items.add(new OrderItem(menu, restaurant));
                }
            });
            customerRegistry.addToGroupOrder(items, this.groupOrder);
        } catch (UnauthorizedOperationException e) {
            errorMessage = e.getMessage();
            LoggerUtils.log(Level.SEVERE, e.getMessage());
        }
    }

    @Then("his order is registered in the group order")
    public void thenHisOrderIsRegisteredInGroupOrder() {

        assertNotNull(this.groupOrder);

        List<OrderAbstract> orders = customerRegistry
                .getCustomerAccount()
                .getOrders()
                .stream()
                .filter(order -> order instanceof SingleOrder)
                .toList();

        assertFalse(orders.isEmpty());
        assertTrue(groupOrder.getSubOrders().containsAll(orders));
    }

    @When("the restaurant prepares the order")
    public void theRestaurantPreparesTheOrder() throws InterruptedException, UnauthorizedOperationException {
        restaurant.prepareAllOrderOfNearestTimeSlot();
    }

    @When("the restaurant cancels the order")
    public void theRestaurantCancelsTheOrder() {
        restaurant.cancelOrder(1);
    }

    @When("10 minutes have passed")
    public void minutesHavePassed() throws InterruptedException {
        verify(scheduler, times(1)).waitTenMinutes();
    }

    @Then("the order status is {string}")
    public void theOrderStatusIs(String status) {
        EOrderStatus statusEnum = EOrderStatus.valueOf(status);
        assertEquals(statusEnum, customerRegistry.getOrderService().fetchAllOrder().get(0).getStatus());
    }

    @Then("the restaurant have {int} order")
    public void theRestaurantHaveOrder(int nbOrders) {
        assertEquals(nbOrders, restaurant.getOrders().size());
    }

    @When("his order is finished")
    public void hisOrderIsFinished() {
        customerRegistry.getOrderService().fetchAllOrder().get(0).getItems().forEach(orderItem -> orderItem.setStatus(FINISH));
    }

    @And("he gets a credit on future order")
    public void heGetsACreditOnFutureOrder() {
        assertEquals(10, customerRegistry.getOrderService().fetchAllOrder().get(0).getItems().size());
        try {
            assertTrue(DiscountService.computeCustomerCredit(List.of(customerRegistry.getOrderService().fetchAllOrder().get(0)), OrderService.MIN_NUMBER_OF_ORDERS, OrderService.DISCOUNT));
            assertEquals(9.4, customerRegistry.getCustomerAccount().getCredit());
        } catch (UnauthorizedOperationException e) {
            throw new RuntimeException(e);
        }
    }

    @Given("customer has credit of {double} euros from previous order")
    public void customerHasCreditOfEurosFromPreviousOrder(double credit) {
        customerRegistry.getCustomerAccount().setCredit(credit);
    }

    @Then("he can use his credit to pay part of the order")
    public void heCanUseHisCreditToPayPartOfTheOrder() {
        SingleOrder order = (SingleOrder) customerRegistry.getCustomerAccount().getOrders().get(0);
        double price = order.getItems().stream().mapToDouble(item -> item.getPrice(null)).sum() - customerRegistry.getCustomerAccount().getCredit();
        assertEquals(order.getTotalPrice(), price);
    }

    @And("he has credit left")
    public void heHasCreditLeft() {
        assertTrue(customerRegistry.getCustomerAccount().getCredit() >= 0.0);
    }

    @When("the customer {string} logs back in with the password {string}")
    public void theCustomerLogsBackInWithThePassword(String name, String password) throws BadPasswordException, NoAccountFoundException {
        customerRegistry = new STEats(scheduler).getCustomerRegistry();
        String[] nameElement = name.split(" ");
        customerRegistry.customerLogin(nameElement[0], nameElement[1], password);
    }

    @When("he chooses {int} of the menu {string}")
    public void whenHeChooseMenu(int quantity, String menuName) {
        if (!menus.containsKey(menuName)) {
            this.menus.put(menuName, quantity);
        } else {
            this.menus.put(menuName, this.menus.get(menuName) + quantity);
        }
    }

    @When("he chooses the number of participant {int}")
    public void whenHeChooseNumberOfParticipant(int numberOfParticipant) {
        this.numberOfParticipant = numberOfParticipant;
    }

    @Given("{int} connected users, each having added their suborder")
    public void connectedUsersEachHavingChosenAMenuAndValidatedTheirSuborder(int nbUsers) throws BadPasswordException, NoAccountFoundException {
        customerRegistry = new STEats(scheduler).getCustomerRegistry();
        assertNotNull(groupOrder);
        Restaurant restaurant = customerRegistry.getRestaurantService().findRestaurantById(1);
        Menu menu = restaurant.getMenuItem("Salade César");

        for (int i = 1; i <= nbUsers; i++) {
            customerRegistry.customerLogin("user" + i, "name" + i, "password" + i);
            try {
                customerRegistry.addToGroupOrder(List.of(new OrderItem(menu, restaurant)), groupOrder);
            } catch (UnauthorizedOperationException e) {
                throw new RuntimeException(e);
            }
        }

        assertEquals(10, groupOrder.getSubOrders().size());
    }

    @Then("all users get a credit on future order")
    public void allUsersGetACreditOnFutureOrder() {
        List<CustomerAccount> users = customerRegistry.getOrderService().fetchCustomersInOrder(groupOrder);
        assertEquals(10, users.size());

        try {
            assertTrue(groupOrder.addCustomerCredit(10, 0.1));
        } catch (UnauthorizedOperationException e) {
            throw new RuntimeException(e);
        }

        for (CustomerAccount user : users) {
            assertTrue(user.getCredit() >= 0.0);
        }
    }

    @Then("an order error message should be printed")
    public void thenAnErrorMessageShouldBePrinted() {
        assertNotNull(errorMessage);
    }

    @And("he receives a confirmation notification")
    public void heReceivesAConfirmationNotification() {
        assertTrue(isUserNotificationReceived);
    }

    @And("his payment is accepted")
    public void hisPaymentIsAccepted() {
        doReturn(true).when(paymentService).pay(anyDouble());
    }

    @And("his payment is refused")
    public void hisPaymentIsRefused() {
        doReturn(false).when(paymentService).pay(anyDouble());
    }

    @Given("the customer is a {string}")
    public void theCustomerIsA(String type) {
        ECustomerStatus status = ECustomerStatus.valueOf(type);
        customerRegistry.getCustomerAccount().setType(status);
    }

    @When("the restaurant create a student discount for this item")
    public void theRestaurantCreateAStudentDiscountForThisItem() {
        for (String menuName : menus.keySet()) {
            Menu menu = restaurant.getMenuItem(menuName);
            menu.setStudentPrice(0.5 * menu.getGlobalPrice());
        }
    }

    @And("his order is at student price")
    public void hisOrderIsAtStudentPrice() {
        List<OrderAbstract> orders = customerRegistry.getCustomerAccount().getOrders();
        OrderAbstract order = orders.get(0);

        assertEquals(0.5 * order.getItems().stream().mapToDouble(item -> item.getPrice(null)).sum(), order.getNonReducedTotalPrice());
    }

    @When("it's his {int} order at this restaurant")
    public void itSHisOrderAtThisRestaurant(int numberOfOrder) throws UnauthorizedOperationException {
        OrderAbstract order = new OrderBuilder(DateTime.now().plusMinutes(1), customerRegistry.getCustomerAccount())
                .setDeliveryAddress("930 Rte des Colles, 06410 Biot")
                .addMenuItem(new OrderItem(restaurant.getMenuItems().get(0), restaurant), 1)
                .build();

        order.getItems().get(0).setStatus(FINISH);

        for (int i = 0; i < numberOfOrder; i++) {
            customerRegistry.getCustomerAccount().addOrder(order);
        }

        numberOfAddedOrderForDiscount += numberOfOrder;
    }

    @And("his order is at a reduced price")
    public void hisOrderIsAtAReducedPrice() {
        List<OrderAbstract> orders = customerRegistry.getCustomerAccount().getOrders();
        OrderAbstract order = orders.get(0);

        double price = 0.95 * order.getItems().stream()
                .mapToDouble(item -> item.getPrice(customerRegistry.getCustomerAccount().getType()))
                .sum();

        double credit = customerRegistry.getCustomerAccount().getCredit();
        assertEquals(price - Math.min(price, credit), order.getTotalPrice());
    }

    @Then("I have {int} order in my history")
    public void iHaveOrderInMyHistory(int arg0) {
        assertEquals(arg0, customerRegistry.getCustomerAccount().getOrders().size());
    }

    @When("his order is finalized")
    public void hisOrderIsFinalized() throws UnauthorizedOperationException {
        customerRegistry.getOrderService().fetchAllOrder().get(0).setFinished();
    }

    @And("he cancels the order")
    public void heCancelsTheOrder() throws UnauthorizedOperationException, UnauthorizedModificationException {
        isUserNotificationReceived = false;
        try (MockedStatic<NotificationService> mockedStatic = mockStatic(NotificationService.class)) {
            assertTrue(customerRegistry.cancelOrder(customerRegistry.getOrderService().fetchAllOrder().get(0).getId()));
            mockedStatic.verify(() -> NotificationService.sendNotificationToUser(eq(UserLevel.USER), anyString(), anyString()), times(1));
            isUserNotificationReceived = true;
        }
    }

    @And("the refund is initiated")
    public void theRefundIsInitiated() {
        verify(paymentService, times(1)).refund(anyDouble());
    }

    @And("the order is sent to the restaurant with {int} items")
    public void theOrderIsSentToTheRestaurant(int nbItems) {
        assertFalse(restaurant.getOrders().isEmpty());
        assertEquals(nbItems, restaurant.getOrders().size());
    }

    @And("the order is in customer's order history")
    public void theOrderIsInCustomerSOrderHistory() {
        assertFalse(customerRegistry.getCustomerAccount().getOrders().isEmpty());
    }
}
