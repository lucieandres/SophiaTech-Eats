package fr.etu.steats.registry;

import fr.etu.steats.account.CustomerAccount;
import fr.etu.steats.exception.*;
import fr.etu.steats.order.GroupOrder;
import fr.etu.steats.order.OrderAbstract;
import fr.etu.steats.order.OrderItem;
import fr.etu.steats.order.SingleOrder;
import fr.etu.steats.restaurant.Menu;
import fr.etu.steats.restaurant.Restaurant;
import fr.etu.steats.service.DeliveryLocationService;
import fr.etu.steats.service.OrderService;
import fr.etu.steats.service.RegistrationService;
import fr.etu.steats.service.RestaurantService;
import fr.etu.steats.utils.LoggerUtils;
import org.joda.time.DateTime;

import java.util.*;
import java.util.logging.Level;

/**
 * This class represents the customer registry and
 * contains all the methods that a customer can use
 */
public class CustomerRegistry {
    private static final String ERROR_UPDATE = "You can't update an order you doesn't own";
    private static final String ERROR_OWNER = "Your are not the owner of this order.";
    private CustomerAccount customerAccount;
    private final OrderService orderService;
    private final RegistrationService registrationService;
    private final DeliveryLocationService deliveryLocationService;
    private final RestaurantService restaurantService;

    public CustomerRegistry(OrderService orderService, RegistrationService registrationService, DeliveryLocationService deliveryLocationService, RestaurantService restaurantService) {
        this.orderService = orderService;
        this.registrationService = registrationService;
        this.deliveryLocationService = deliveryLocationService;
        this.restaurantService = restaurantService;
    }

    /**
     * This method allows the user to log in
     *
     * @param firstname The firstname of the user
     * @param lastname The lastname of the user
     * @param password The password of the user
     * @throws BadPasswordException if the password is incorrect
     * @throws NoAccountFoundException if the user doesn't exist in the system
     */
    public void customerLogin(String firstname, String lastname, String password) throws BadPasswordException, NoAccountFoundException {
        this.customerAccount = this.registrationService.loginCustomer(firstname, lastname, password);
    }

    public void customerRegister(String firstname, String lastname, String password) {
        try {
            this.customerAccount = this.registrationService.registerCustomer(firstname, lastname, password);
        } catch (AlreadyRegisteredUser e) {
            LoggerUtils.log(Level.SEVERE, e.getMessage());
        }
    }

    /**
     * This method allows the user to place a single order
     *
     * @param mapMenuNameByRestaurantId The map of menu by restaurant id
     * @param deliveryDate The date of the delivery
     * @param deliveryAddress The address of the delivery
     * @throws UnauthorizedOperationException if the user isn't logged in or if the delivery address is not in the delivery list
     */
    public boolean placeSingleOrder(Map<Integer, List<String>> mapMenuNameByRestaurantId, DateTime deliveryDate, String deliveryAddress) throws UnauthorizedOperationException {
        checkCustomerAccount();
        if (mapMenuNameByRestaurantId == null || mapMenuNameByRestaurantId.isEmpty()) {
            throw new IllegalArgumentException("You need to pass a map of menu by restaurant ant it can't be empty");
        }

        if (deliveryAddress == null || deliveryDate == null) {
            throw new UnauthorizedOperationException("You must specify delivery information.");
        }
        if (!this.deliveryLocationService.isDeliveryLocation(deliveryAddress)) {
            throw new UnauthorizedOperationException("The delivery address you provided is not in the delivery list.");
        }

        List<OrderItem> items = new ArrayList<>();

        for (Map.Entry<Integer, List<String>> entry : mapMenuNameByRestaurantId.entrySet()) {
            if (entry.getKey() <= 0) {
                throw new IllegalArgumentException("The restaurantId need to be superior or equal to one, please retry.");
            }
            if (entry.getValue() == null || !entry.getValue().stream().filter(Objects::isNull).toList().isEmpty() || entry.getValue().contains("")) {
                throw new IllegalArgumentException("The menuName can't be null or empty, please retry.");
            }

            Restaurant restaurantById = this.restaurantService.findRestaurantById(entry.getKey());
            if (restaurantById == null) {
                throw new UnauthorizedOperationException("One of the restaurant id you provided doesn't exist, please retry with a correct one.");
            }

            List<Menu> menuList = entry.getValue().stream().map(restaurantById::getMenuItem).toList();
            if (menuList.isEmpty() || menuList.contains(null)) {
                throw new UnauthorizedOperationException("The menuName you provided doesn't exist, please retry with a correct one.");
            }

            items.addAll(menuList.stream().map(menu -> new OrderItem(menu, restaurantById)).toList());
        }

        if (items.isEmpty()) {
            throw new UnauthorizedOperationException("You can't pass an empty order...");
        }

        return this.orderService.createSingleOrder(this.customerAccount, items, deliveryDate, deliveryAddress);
    }

    /**
     * This method allows the user to place a group order
     *
     * @param deliveryDate The date of the delivery
     * @param deliveryAddress The address of the delivery
     * @throws UnauthorizedOperationException if the user isn't logged in or if the delivery address is not in the delivery list
     */
    public boolean placeGroupOrder(DateTime deliveryDate, String deliveryAddress) throws UnauthorizedOperationException {
        checkCustomerAccount();
        if (deliveryAddress == null || deliveryDate == null) {
            throw new UnauthorizedOperationException("You must specify delivery information.");
        }
        if (!this.deliveryLocationService.isDeliveryLocation(deliveryAddress)) {
            throw new UnauthorizedOperationException("The delivery address you provided is not in the delivery list.");
        }
        return this.orderService.createGroupOrder(this.customerAccount, deliveryDate, deliveryAddress);
    }

    public void placeAfterWorkOrder(int restaurant, DateTime deliveryDate, int numberOfParticipant) throws UnauthorizedOperationException {
        checkCustomerAccount();
        if (numberOfParticipant <= 0) {
            throw new IllegalArgumentException("The number of participant need to be superior or equal to one, please retry.");
        }
        List<OrderItem> items = new ArrayList<>();
        Restaurant restaurantById = this.restaurantService.findRestaurantById(restaurant);
        if (restaurantById == null) {
            throw new UnauthorizedOperationException("The restaurant id you provided doesn't exist, please retry with a correct one.");
        }
        for (Menu menu : restaurantById.getAfterWorkMenuItems()) {
            items.add(new OrderItem(menu, restaurantById));
        }
        this.orderService.createAfterWorkOrder(this.customerAccount, items, deliveryDate, restaurantById.getAddress(), numberOfParticipant);
    }

    /**
     * This method allows the user to add an Order to a group order or any order that allow sub order
     *
     *
     * @param items This list is nullable or can be empty, if it is then a groupOrder will be inserted in the parent order.
     * Else it'll be a singleOrder containing all the items.
     * @param parentOrder This represents the parent order, it can't be null and need to allow sub order in order to not trigger any error.
     * @throws UnauthorizedOperationException This will happen if :
     * - The user isn't logged in
     * - The parent order is null or don't allow sub order
     * - The status of the parent order doesn't allow update
     */
    public boolean addToGroupOrder(List<OrderItem> items, OrderAbstract parentOrder) throws UnauthorizedOperationException {
        checkCustomerAccount();

        if (parentOrder == null || !parentOrder.allowSubOrder()) {
            throw new UnauthorizedOperationException("Invalid group order");
        }

        return orderService.addOrderToGroupOrder(this.customerAccount, items, parentOrder);
    }

    /**
     * This method allows an existing order to join a group order or any other that allow sub order
     *
     *
     * @param parentOrder  This represents the parent order, it can't be null and need to allow sub order in order to not trigger any error.
     * @param joiningOrder This represents the sub order, it can't be null and need to not already be part of a group order.
     * @throws UnauthorizedOperationException This will happen if :
     * - The user isn't logged in
     * - one of the two order is null
     * - one of the two order doesn't allow update
     * - the parent order don't allow suborder
     * - the suborder is already part of a group order
     */
    public boolean joinOrder(OrderAbstract parentOrder, OrderAbstract joiningOrder) throws UnauthorizedOperationException {
        checkCustomerAccount();
        if (parentOrder == null || joiningOrder == null) {
            throw new UnauthorizedOperationException("You can't join a null order or join an order with a null sub order.");
        }
        if (!parentOrder.allowSubOrder() || joiningOrder.canBeSubOrder() || joiningOrder.isPartOfGroupOrder() || !parentOrder.statusAllowsUpdate() || !joiningOrder.statusAllowsUpdate()) {
            throw new UnauthorizedOperationException("The current state of one of your order block the joining order operation.");
        }

        return orderService.joinOrder(parentOrder, joiningOrder);
    }

    /**
     * This method allows the user to update the items of an order if it's not already canceled or delivered
     *
     * @param orderId The id of the order to update
     * @param items The new items
     * @throws UnauthorizedOperationException if the user isn't logged in or if the order doesn't exist
     */
    public boolean updateOrderItem(int orderId, List<OrderItem> items) throws UnauthorizedOperationException, UnauthorizedModificationException {
        checkCustomerAccount();
        OrderAbstract order = this.orderService.findOrderById(orderId);
        if (customerAccount != order.getCustomer()) {
            throw new UnauthorizedOperationException(ERROR_UPDATE);
        } else if (order instanceof GroupOrder groupOrder) {
            return addToGroupOrder(items, groupOrder);
        } else if (order instanceof SingleOrder singleOrder) {
            return orderService.updateOrderItems(singleOrder, items);
        }
        return false;
    }

    /**
     * This method allows the user to update the delivery date of an order
     *
     * @param orderId The id of the order to update
     * @param deliveryDate The new delivery date
     * @throws UnauthorizedOperationException if the user isn't logged in or if the order doesn't exist
     * @throws UnauthorizedModificationException if the user isn't the owner of the order
     */
    public boolean updateOrderDeliveryDate(int orderId, DateTime deliveryDate) throws UnauthorizedOperationException, UnauthorizedModificationException {
        checkCustomerAccount();
        OrderAbstract order = this.orderService.findOrderById(orderId);
        if (customerAccount != order.globalOwner()) {
            throw new UnauthorizedOperationException(ERROR_UPDATE);
        }
        if (!order.getCustomer().equals(this.customerAccount)) {
            throw new UnauthorizedOperationException(ERROR_OWNER);
        }
        return orderService.updateOrderDeliveryDate(order, deliveryDate);
    }

    /**
     * This method allows the user to update the delivery address of an order
     *
     * @param orderId The id of the order to update
     * @param deliveryAddress The new delivery address
     * @throws UnauthorizedOperationException if the user isn't logged in or if the order doesn't exist
     * @throws UnauthorizedModificationException if the user isn't the owner of the order
     */
    public boolean updateOrderDeliveryAddress(int orderId, String deliveryAddress) throws UnauthorizedOperationException, UnauthorizedModificationException {
        checkCustomerAccount();
        OrderAbstract order = this.orderService.findOrderById(orderId);
        if (order == null) {
            throw new UnauthorizedOperationException("The order you want to update doesn't exist.");
        } else if (customerAccount != order.globalOwner()) {
            throw new UnauthorizedOperationException(ERROR_UPDATE);
        }
        if (!order.getCustomer().equals(this.customerAccount)) {
            throw new UnauthorizedOperationException(ERROR_OWNER);
        }
        return orderService.updateOrderDeliveryAddress(order, deliveryAddress);
    }

    /**
     * This method allows the user to cancel an order if it's not already canceled or delivered
     *
     * @param orderId The id of the order to cancel
     * @throws UnauthorizedOperationException if the user isn't logged in or if the order doesn't exist
     * @throws UnauthorizedModificationException if the user isn't the owner of the order
     */
    public boolean cancelOrder(int orderId) throws UnauthorizedOperationException, UnauthorizedModificationException {
        checkCustomerAccount();
        OrderAbstract order = this.orderService.findOrderById(orderId);
        if (order == null) {
            throw new UnauthorizedOperationException("The order you want to update doesn't exist.");
        } else if (!order.getCustomer().equals(this.customerAccount)) {
            throw new UnauthorizedModificationException(ERROR_OWNER);
        }
        return this.orderService.cancelOrder(order);
    }

    /**
     * This method allows the user to see all the menu of each restaurant
     *
     * @return A map containing all the menu by restaurant id
     */
    public Map<Integer, List<Menu>> getAllMenuOfEachRestaurant() {
        Map<Integer, List<Menu>> mapMenuByRestaurantId = new HashMap<>();
        for (Restaurant restaurant : this.restaurantService.getRestaurants()) {
            mapMenuByRestaurantId.put(restaurant.getId(), restaurant.getMenuItems());
        }
        return mapMenuByRestaurantId;
    }

    /**
     * This method allows the user to see all the menu of a specific restaurant
     *
     * @param restaurantId The id of the restaurant
     * @return A list of the restaurant menu
     */
    public List<Menu> getMenuOfRestaurant(int restaurantId) {
        return this.restaurantService.findRestaurantById(restaurantId).getMenuItems();
    }

    public CustomerAccount getCustomerAccount() {
        return this.customerAccount;
    }

    public OrderService getOrderService() {
        return this.orderService;
    }

    public RegistrationService getRegistrationService() {
        return this.registrationService;
    }

    public RestaurantService getRestaurantService() {
        return restaurantService;
    }

    private void checkCustomerAccount() throws UnauthorizedOperationException {
        if (this.customerAccount == null) {
            throw new UnauthorizedOperationException("You are not logged in. Please log in to continue.");
        }
    }
}
