package fr.etu.steats.definitions;

import fr.etu.steats.STEats;
import fr.etu.steats.account.CustomerAccount;
import fr.etu.steats.order.BuffetOrder;
import fr.etu.steats.order.OrderItem;
import fr.etu.steats.registry.AdminRegistry;
import fr.etu.steats.restaurant.Menu;
import fr.etu.steats.restaurant.Restaurant;
import fr.etu.steats.service.NotificationService;
import fr.etu.steats.utils.UserLevel;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.joda.time.DateTime;
import org.mockito.MockedStatic;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;

public class AdminOrderDefinition {

    private STEats steats;
    private AdminRegistry adminRegistry;
    private CustomerAccount customerAccount;
    private Restaurant restaurant;
    private Menu menu;
    private Exception exception;
    private boolean creationSuccess = false;
    private boolean notificationSent = false;

    @Given("I am logged in as an university staff")
    public void iAmLoggedInAsAnUniversityStaff() {
        this.steats = new STEats();
        this.adminRegistry = this.steats.getAdminRegistry();
        this.adminRegistry.adminLogin("admin", "admin", "admin");
    }

    @Given("I am not logged in as an university staff")
    public void iAmLoggedNotInAsAnUniversityStaff() {
        this.steats = new STEats();
        this.adminRegistry = this.steats.getAdminRegistry();
    }

    @And("there is a customer account with firstname {string}, lastname {string}, and password {string}")
    public void thereIsACustomerAccountWithFirstnameLastnameAndPassword(String firstname, String lastname, String password) {
        this.steats.getCustomerRegistry().customerRegister(firstname, lastname, password);
        this.customerAccount = this.steats.getCustomerRegistry().getCustomerAccount();
    }

    @And("there is a restaurant with the name {string} with id {int}")
    public void thereIsARestaurantWithTheName(String restaurantName, int id) {
        this.restaurant = this.adminRegistry.getRestaurantService().findRestaurantById(id);
        assertEquals(this.restaurant.getName(), restaurantName.toLowerCase().trim());
    }

    @And("there is a menu {string} with price {int} in the restaurant {string}")
    public void thereIsAMenuWithPriceInTheRestaurant(String menuName, int price, String restaurantName) {
        this.menu = this.restaurant.getMenuItem(menuName);
        assertEquals(this.menu.getPrice(this.customerAccount.getType()), price);
        assertEquals(this.restaurant.getName(), restaurantName.toLowerCase().trim());
    }

    @When("I attempt to create a buffet order for the recipient customer and {int} items from the restaurant")
    public void iAttemptToCreateABuffetOrderForTheCustomerAndItemsFromTheRestaurant(int quantity) {
        List<OrderItem> itemList = new ArrayList<>();
        for (int index = 0; index < quantity; index++) {
            itemList.add(new OrderItem(this.menu, this.restaurant));
        }
        DateTime deliveryDate = new DateTime().withHourOfDay(12).plusDays(1);
        try (MockedStatic<NotificationService> mockedStatic = mockStatic(NotificationService.class)) {
            this.creationSuccess = this.adminRegistry.createBuffetOrder(this.customerAccount, deliveryDate, itemList, this.restaurant);

            mockedStatic.verify(() -> NotificationService.sendNotificationToUser(eq(UserLevel.USER), anyString(), anyString()), times(1));
            mockedStatic.verify(() -> NotificationService.sendNotificationToUser(eq(UserLevel.RESTAURANT_MANAGER), anyString(), anyString()), times(1));
            notificationSent = true;
        } catch (Exception e) {
            this.exception = e;
        }
    }

    @Then("the buffet order should be created successfully")
    public void theBuffetOrderShouldBeCreatedSuccessfully() {
        assertTrue(this.creationSuccess);
        assertEquals(1, this.adminRegistry.getOrderService().fetchAllOrder().size());
        assertNull(this.exception);
    }

    @And("the buffet order should be associated with the recipient customer")
    public void theBuffetOrderShouldBeAssociatedWithTheRecipientCustomer() {
        assertEquals(1, this.customerAccount.getOrders().size());
    }

    @And("the buffet order don't need to be payed and delivered")
    public void theBuffetOrderDonTNeedToBePayedAndDelivered() {
        assertFalse(this.customerAccount.getOrders().get(0).needToBeDelivered());
        assertFalse(this.customerAccount.getOrders().get(0).needToBePaid());
    }

    @And("the buffet order should be associated with the restaurant")
    public void theBuffetOrderShouldBeAssociatedWithTheRestaurant() {
        BuffetOrder buffetOrder = (BuffetOrder) this.customerAccount.getOrders().get(0);
        assertEquals(this.restaurant, buffetOrder.getRestaurant());
    }

    @Then("I should get an error message")
    public void iShouldGetAnErrorMessage() {
        assertFalse(this.creationSuccess);
        assertNotNull(this.exception);
    }

    @And("the restaurant and customer should receive a notification")
    public void theRestaurantAndCustomerShouldReceiveANotification() {
        assertTrue(this.notificationSent);
    }
}
