package fr.etu.steats.definitions;

import fr.etu.steats.STEats;
import fr.etu.steats.account.CustomerAccount;
import fr.etu.steats.account.DeliveryAccount;
import fr.etu.steats.exception.AlreadyRegisteredUser;
import fr.etu.steats.exception.BadPasswordException;
import fr.etu.steats.exception.NoAccountFoundException;
import fr.etu.steats.exception.UnauthorizedOperationException;
import fr.etu.steats.order.OrderAbstract;
import fr.etu.steats.order.OrderBuilder;
import fr.etu.steats.order.OrderItem;
import fr.etu.steats.registry.DeliveryRegistry;
import fr.etu.steats.restaurant.Menu;
import fr.etu.steats.restaurant.Restaurant;
import fr.etu.steats.service.NotificationService;
import fr.etu.steats.service.OrderService;
import fr.etu.steats.utils.LoggerUtils;
import fr.etu.steats.utils.Scheduler;
import fr.etu.steats.utils.UserLevel;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.joda.time.DateTime;
import org.joda.time.LocalTime;
import org.mockito.MockedStatic;

import java.util.List;
import java.util.logging.Level;

import static fr.etu.steats.enums.EOrderStatus.FINISH;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class DeliveryDefinition {

    private DeliveryAccount deliveryAccount;
    private Scheduler mockScheduler;
    private OrderService orderService;
    private List<OrderAbstract> orders;
    private DeliveryRegistry deliveryRegistry;
    private OrderAbstract order;
    private boolean isDeliveryNotificationReceived = false;

    @Given("a delivery account {string} {string} with password {string}")
    public void aDeliveryAccount(String firstname, String lastname, String password) throws InterruptedException, BadPasswordException, NoAccountFoundException {
        mockScheduler = mock(Scheduler.class);
        doNothing().when(mockScheduler).waitTenMinutes();

        deliveryRegistry = new STEats(mockScheduler).getDeliveryRegistry();
        deliveryRegistry.deliveryLogin(firstname, lastname, password);

        this.deliveryAccount = deliveryRegistry.getDeliveryAccount();
        this.orderService = deliveryRegistry.getOrderService();
    }

    @When("The system possess {int} order ready to deliver")
    public void setupXOrder(int nbOrder) throws AlreadyRegisteredUser, InterruptedException, UnauthorizedOperationException {
        Restaurant restaurant = new Restaurant("Pizza Hut", 1, "test", mockScheduler, "1 rue de la paix");
        Menu menu = new Menu("Pizza", 12.0);
        restaurant.addMenuItem(menu);

        CustomerAccount tmp = deliveryRegistry.getRegistrationService().registerCustomer("Axel", "Delille", "test");

        for (int i = 0; i < nbOrder; i++) {
            DateTime deliveryDate = new DateTime().plusDays(1).withTime(new LocalTime(12, 0, 0));
            orderService.createSingleOrder(tmp, List.of(new OrderItem(menu, restaurant)), deliveryDate, "123 route des étoiles");
        }

        restaurant.prepareAllOrderOfNearestTimeSlot();
    }

    @When("I view the list of deliverable orders")
    public void iViewTheListOfDeliverableOrders() {
        orders = orderService.getOrderReadyToDeliver();
    }

    @Then("I should see {int} orders")
    public void iShouldSeeOrders(int expectedOrders) {
        assertEquals(expectedOrders, orders.size());
    }

    @When("I am not connected")
    public void iAmNotConnected() {
        deliveryAccount = null;
    }

    @Then("I can't access the list of deliverable orders")
    public void iCanTAccessTheListOfDeliverableOrders() {
        assertThrows(UnauthorizedOperationException.class, () -> deliveryRegistry.getOrderReadyToDeliver());
    }

    @When("I assign myself to an order")
    public void iAssignMyselfToAnOrder() throws UnauthorizedOperationException {
        Menu menu = new Menu("burger", 10);
        Restaurant restaurant = new Restaurant("resto", 1, "azertyuiop", mockScheduler, "1 rue de la paix");
        restaurant.addMenuItem(menu);

        OrderAbstract order = new OrderBuilder(new DateTime().plusMinutes(1), new CustomerAccount("John", "Doe", "password"))
                .setDeliveryAddress("123 route des étoiles")
                .addMenuItem(new OrderItem(menu, restaurant), 1)
                .build();

        try (MockedStatic<NotificationService> mockedStatic = mockStatic(NotificationService.class)) {
            deliveryAccount.setAssignedOrder(order);
            mockedStatic.verify(() -> NotificationService.sendNotificationToUser(eq(UserLevel.DELIVERY_MAN), anyString(), anyString()), times(1));
            isDeliveryNotificationReceived = true;
        } catch (Exception e) {
            LoggerUtils.log(Level.SEVERE, e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @Then("I should be able to view my assigned order")
    public void iShouldBeAbleToViewMyAssignedOrder() {
        assertNotNull(deliveryAccount.getAssignedOrder());
    }

    @When("I'm not assigned to any order")
    public void imNotAssignedToAnyOrder() {
        assertNull(deliveryAccount.getAssignedOrder());
    }

    @Then("I should not be able to view my assigned order")
    public void iShouldNotBeAbleToViewMyAssignedOrder() {
        assertNull(deliveryAccount.getAssignedOrder());
    }

    @When("I'm doing a delivery")
    public void imDoingADelivery() throws InterruptedException, UnauthorizedOperationException {
        Restaurant restaurant = new Restaurant("Pizza Hut", 1, "test", mockScheduler, "1 rue de la paix");
        Menu menu = new Menu("Pizza", 12.0);
        restaurant.addMenuItem(menu);

        DateTime deliveryDate = new DateTime().plusDays(1).withTime(new LocalTime(12, 0, 0));
        orderService.createSingleOrder(new CustomerAccount("John", "Doe", "password"), List.of(new OrderItem(menu, restaurant)), deliveryDate, "321 route des étoiles");
        restaurant.prepareAllOrderOfNearestTimeSlot();

        order = orderService.fetchAllOrder().get(0);

        deliveryAccount.setAssignedOrder(order);
        deliveryAccount.putOrderInDelivery();
    }

    @Then("I should take 10 minutes to finish it")
    public void iShouldTake10MinutesToFinishIt() throws InterruptedException {
        verify(mockScheduler, times(2)).waitTenMinutes();
        assertEquals(FINISH, order.getStatus());
    }

    @Given("a non registered delivery man")
    public void aNonRegisteredDeliveryMan() {
        deliveryRegistry = new STEats().getDeliveryRegistry();
    }

    @And("I receive a notification with the order details")
    public void iReceiveANotificationWithTheOrderDetails() {
        assertTrue(isDeliveryNotificationReceived);
    }

}
