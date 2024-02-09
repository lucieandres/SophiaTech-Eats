package fr.etu.steats.definitions;

import fr.etu.steats.STEats;
import fr.etu.steats.account.DeliveryAccount;
import fr.etu.steats.exception.BadPasswordException;
import fr.etu.steats.exception.NoAccountFoundException;
import fr.etu.steats.exception.UnauthorizedOperationException;
import fr.etu.steats.order.OrderAbstract;
import fr.etu.steats.order.OrderItem;
import fr.etu.steats.registry.CustomerRegistry;
import fr.etu.steats.registry.DeliveryRegistry;
import fr.etu.steats.restaurant.Menu;
import fr.etu.steats.restaurant.Restaurant;
import fr.etu.steats.service.NotificationService;
import fr.etu.steats.service.OrderService;
import fr.etu.steats.service.PaymentService;
import fr.etu.steats.utils.LoggerUtils;
import fr.etu.steats.utils.Scheduler;
import fr.etu.steats.utils.UserLevel;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.joda.time.DateTime;
import org.joda.time.LocalTime;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.List;
import java.util.logging.Level;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.Mockito.*;

public class UserDeliveryNotificationDefinition {

    private DeliveryRegistry deliveryRegistry;
    private OrderService orderService;
    private Scheduler mockScheduler;
    private Boolean isUserNotificationReceived = false;

    @Given("the customer {string} {string} with the password {string} have made an order")
    public void givenTheCustomerHaveMadeAnOrder(String firstname, String lastname, String password) throws InterruptedException, UnauthorizedOperationException, BadPasswordException, NoAccountFoundException {
        this.mockScheduler = mock(Scheduler.class);
        PaymentService paymentService = Mockito.mock(PaymentService.class);
        doReturn(true).when(paymentService).pay(anyDouble());
        doNothing().when(mockScheduler).waitTenMinutes();

        STEats stEats = new STEats(paymentService, this.mockScheduler);
        CustomerRegistry customerRegistry = stEats.getCustomerRegistry();
        customerRegistry.customerLogin(firstname, lastname, password);

        this.deliveryRegistry = stEats.getDeliveryRegistry();
        this.deliveryRegistry.deliveryLogin("user1", "name1", "password1");

        Restaurant restaurant = new Restaurant("Pizza Hut", 1, "test", mockScheduler, "5 rue la paix");
        Menu menu = new Menu("Pizza", 12.0);
        restaurant.addMenuItem(menu);

        DateTime deliveryDate = new DateTime().plusDays(1).withTime(new LocalTime(12, 0, 0));
        this.orderService = customerRegistry.getOrderService();
        this.orderService.createSingleOrder(customerRegistry.getCustomerAccount(), List.of(new OrderItem(menu, restaurant)), deliveryDate, "321 route des étoiles");
        restaurant.prepareAllOrderOfNearestTimeSlot();
    }


    @When("his order is on the way")
    public void hisOrderIsOnTheWay() throws InterruptedException {
        doNothing().when(mockScheduler).waitTenMinutes();

        DeliveryAccount deliveryAccount = deliveryRegistry.getDeliveryAccount();

        OrderAbstract order = orderService.fetchAllOrder().get(0);

        deliveryAccount.setAssignedOrder(order);
        try (MockedStatic<NotificationService> mockedStatic = mockStatic(NotificationService.class)) {
            deliveryAccount.setAssignedOrder(order);
            mockedStatic.verify(() -> NotificationService.sendNotificationToUser(eq(UserLevel.DELIVERY_MAN), anyString(), anyString()), times(1));
            isUserNotificationReceived = true;
        } catch (Exception e) {
            LoggerUtils.log(Level.SEVERE, e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @Then("he receives a notification")
    public void heReceivesANotification() {
        assertTrue(isUserNotificationReceived);
    }
}
