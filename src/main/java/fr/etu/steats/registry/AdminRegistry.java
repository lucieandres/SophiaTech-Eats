package fr.etu.steats.registry;

import fr.etu.steats.account.AdminAccount;
import fr.etu.steats.account.CustomerAccount;
import fr.etu.steats.account.DeliveryAccount;
import fr.etu.steats.exception.AlreadyRegisteredUser;
import fr.etu.steats.exception.BadPasswordException;
import fr.etu.steats.exception.NoAccountFoundException;
import fr.etu.steats.exception.UnauthorizedOperationException;
import fr.etu.steats.order.OrderItem;
import fr.etu.steats.restaurant.Restaurant;
import fr.etu.steats.restaurant.StatisticReport;
import fr.etu.steats.service.DeliveryLocationService;
import fr.etu.steats.service.OrderService;
import fr.etu.steats.service.RegistrationService;
import fr.etu.steats.service.RestaurantService;
import fr.etu.steats.utils.LoggerUtils;
import org.joda.time.DateTime;

import java.util.List;
import java.util.Set;
import java.util.logging.Level;

public class AdminRegistry {
    private AdminAccount adminAccount;
    private final DeliveryLocationService deliveryLocationService;
    private final RegistrationService registrationService;
    private final RestaurantService restaurantService;
    private final OrderService orderService;

    public AdminRegistry(DeliveryLocationService deliveryLocationService, RegistrationService registrationService, RestaurantService restaurantService, OrderService orderService) {
        this.deliveryLocationService = deliveryLocationService;
        this.registrationService = registrationService;
        this.restaurantService = restaurantService;
        this.orderService = orderService;
    }

    public AdminAccount getAdminAccount() {
        return adminAccount;
    }

    public void adminLogin(String firstname, String lastname, String password) {
        try {
            this.adminAccount = registrationService.loginAdmin(firstname, lastname, password);
        } catch (NoAccountFoundException | BadPasswordException e) {
            LoggerUtils.log(Level.SEVERE, e.getMessage());
        }
    }

    public boolean addRestaurant(String name, String password, String address) throws UnauthorizedOperationException {
        this.checkAdminAccount();
        try {
            if (this.restaurantService.addRestaurant(name, password, address) != null) {
                return true;
            }
        } catch (Exception e) {
            LoggerUtils.log(Level.SEVERE, e.getMessage());
        }
        return false;
    }

    public boolean removeRestaurant(int id) throws UnauthorizedOperationException {
        this.checkAdminAccount();
        try {
            return this.restaurantService.removeRestaurant(id);
        } catch (Exception e) {
            LoggerUtils.log(Level.SEVERE, e.getMessage());
            return false;
        }
    }

    public Set<Restaurant> getRestaurants() throws UnauthorizedOperationException {
        this.checkAdminAccount();
        return this.restaurantService.getRestaurants();
    }

    public StatisticReport getStatisticsReportFromAllRestaurants() throws UnauthorizedOperationException {
        this.checkAdminAccount();
        return new StatisticReport(this.restaurantService.getRestaurants());
    }

    public StatisticReport getStatisticsReportFromOneRestaurant(int restaurantId) throws UnauthorizedOperationException {
        this.checkAdminAccount();
        return new StatisticReport(this.restaurantService.findRestaurantById(restaurantId));
    }

    public boolean addDeliveryLocation(String location) throws UnauthorizedOperationException {
        this.checkAdminAccount();
        return this.deliveryLocationService.addDeliveryLocation(location);
    }

    public boolean removeDeliveryLocation(String location) throws UnauthorizedOperationException {
        this.checkAdminAccount();
        return this.deliveryLocationService.removeDeliveryLocation(location);
    }

    public boolean editDeliveryLocation(String oldLocation, String newLocation) throws UnauthorizedOperationException {
        this.checkAdminAccount();
        return this.deliveryLocationService.editDeliveryLocation(oldLocation, newLocation);
    }

    public DeliveryAccount addDeliveryAccount(String firstname, String lastname, String password) throws UnauthorizedOperationException, AlreadyRegisteredUser {
        this.checkAdminAccount();
        return this.registrationService.registerDelivery(firstname, lastname, password);
    }

    public boolean removeDeliveryAccount(DeliveryAccount deliveryAccount) throws UnauthorizedOperationException {
        this.checkAdminAccount();
        try {
            return this.registrationService.removeDeliveryAccount(deliveryAccount);
        } catch (Exception e) {
            LoggerUtils.log(Level.SEVERE, e.getMessage());
            return false;
        }
    }

    public boolean createBuffetOrder(CustomerAccount customer, DateTime deliveryDate, List<OrderItem> items, Restaurant restaurant) throws UnauthorizedOperationException, NoAccountFoundException {
        this.checkAdminAccount();
        if (!this.registrationService.getCustomerList().contains(customer)) {
            throw new NoAccountFoundException("We don't have any customer registered in our system named " + customer.getFullName());
        }
        if (!this.restaurantService.getRestaurants().contains(restaurant)) {
            throw new NoAccountFoundException("We don't have any restaurant registered in our system named " + restaurant.getName());
        }
        return this.orderService.createBuffetOrder(customer, deliveryDate, items, this.adminAccount, restaurant);
    }

    public boolean isDeliveryLocation(String location) throws UnauthorizedOperationException {
        this.checkAdminAccount();
        return this.deliveryLocationService.isDeliveryLocation(location);
    }

    private void checkAdminAccount() throws UnauthorizedOperationException {
        if (this.adminAccount == null) {
            throw new UnauthorizedOperationException("You need to be registered as a Admin to do this operation.\nPlease retry after a login.");
        }
    }

    public OrderService getOrderService() {
        return this.orderService;
    }

    public RestaurantService getRestaurantService() {
        return this.restaurantService;
    }
}
