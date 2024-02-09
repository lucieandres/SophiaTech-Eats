package fr.etu.steats.order;

import fr.etu.steats.account.CustomerAccount;
import fr.etu.steats.enums.EOrderStatus;
import fr.etu.steats.exception.UnauthorizedModificationException;
import fr.etu.steats.exception.UnauthorizedOperationException;
import fr.etu.steats.restaurant.Restaurant;
import fr.etu.steats.service.DiscountService;
import fr.etu.steats.service.NotificationService;
import fr.etu.steats.utils.UserLevel;
import org.joda.time.DateTime;

import java.util.List;

import static fr.etu.steats.enums.EOrderStatus.*;

/**
 * Class representing a buffet order
 * A buffet order is an order with list of items for only one restaurant
 * It can be delivered or not and paid or not, the admin can choose
 */
public class BuffetOrder extends OrderAbstract {

    /**
     * Flag to know if the order needs to be delivered by a delivery man
     */
    private final boolean needToBeDelivered;

    /**
     * Flag to know if the order needs to be paid by the customer
     */
    private final boolean needToBePaid;

    /**
     * Associated restaurant to the buffet order
     */
    private final Restaurant restaurant;

    /**
     * The items ordered for the buffet order
     */
    private final List<OrderItem> items;

    /**
     * @param customer     The customer referent of the order assigned by the admin
     * @param deliveryDate The delivery date of the buffet order
     * @param items        The items ordered for the buffet order
     * @param restaurant   The restaurant associated to the buffet order
     */
    protected BuffetOrder(CustomerAccount customer, DateTime deliveryDate, List<OrderItem> items, Restaurant restaurant) {
        super(customer, deliveryDate, null);
        if (items == null || items.isEmpty() || restaurant == null) {
            throw new IllegalArgumentException("You can't create an buffet order without items or restaurant");
        }
        this.items = items;
        this.checkRestaurant(restaurant);
        this.restaurant = restaurant;
        this.needToBeDelivered = false;
        this.needToBePaid = false;
        this.setWaitingRestaurantAcceptance();
    }

    /**
     * Constructor for a buffet order for testing the two flags (only used in tests)
     *
     * @param customer          The customer referent of the order assigned by the admin
     * @param deliveryDate      The delivery date of the order
     * @param items             The items ordered for the buffet order
     * @param deliveryAddress   The delivery address of the order
     * @param restaurant        The restaurant associated to the buffet order
     * @param needToBeDelivered Flag to know if the order needs to be delivered by a delivery man
     * @param needToBePaid      Flag to know if the order needs to be paid by the customer
     */
    protected BuffetOrder(CustomerAccount customer, DateTime deliveryDate, List<OrderItem> items, String deliveryAddress, Restaurant restaurant, boolean needToBeDelivered, boolean needToBePaid) throws UnauthorizedOperationException {
        super(customer, deliveryDate, deliveryAddress);
        this.items = items;
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("You can't create an buffet order without items");
        }
        this.checkRestaurant(restaurant);
        if (needToBeDelivered) {
            if (deliveryDate.isBeforeNow() || deliveryAddress.isEmpty()) {
                throw new IllegalArgumentException("Delivery date can't be in the past and delivery address can't be empty");
            }
        } else {
            this.deliveryAddress = "";
            this.setWaitingRestaurantAcceptance();
        }
        this.needToBeDelivered = needToBeDelivered;
        this.needToBePaid = needToBePaid;
        this.restaurant = restaurant;

        if (this.needToBePaid) {
            this.addCustomerCredit(10, 0.1);
        }
    }

    private void checkRestaurant(Restaurant theRestaurant) {
        if (theRestaurant == null) {
            throw new IllegalArgumentException("You can't create an buffet order without a associated restaurant");
        }
        if (!this.items.stream().allMatch(item -> item.getRestaurant().equals(theRestaurant))) {
            throw new IllegalArgumentException("You can't create an buffet order with items from different restaurants");
        }
    }

    @Override
    public boolean addSubOrder(OrderAbstract order) throws UnauthorizedOperationException {
        throw new UnauthorizedOperationException("You can't add a suborder to a buffet order");
    }

    @Override
    public List<OrderItem> getItems() {
        return this.items;
    }

    /**
     * Caution, this method consume the current credit of the user, we have a side effect, if you want to know the global price without any discount
     * please use getNonReducedTotalPrice()
     *
     * @return The price after discount and after consuming the credit of the customer
     */
    @Override
    public double getTotalPrice() {
        if (needToBePaid()) {
            return calculatePriceWithCreditDeduction(DiscountService.computePriceAfterDiscount(this));
        }
        return 0;
    }

    public double calculatePriceWithCreditDeduction(double totalPrice) {
        double priceAfterDiscount;
        if (totalPrice >= customer.getCredit()) {
            priceAfterDiscount = totalPrice - customer.getCredit();
            customer.setCredit(0.0);
        } else {
            priceAfterDiscount = 0.0;
            customer.setCredit(customer.getCredit() - totalPrice);
        }
        return priceAfterDiscount;
    }

    @Override
    public EOrderStatus getStatus() {
        return computeStatus(this.items.stream().map(OrderItem::getStatus).toList());
    }

    @Override
    public boolean setWaitingRestaurantAcceptance() {
        for (OrderItem item : this.items) {
            item.setStatus(WAITING_RESTAURANT_ACCEPTANCE);
        }
        return true;
    }

    @Override
    public boolean setInDelivery() throws UnauthorizedOperationException {
        if (this.needToBeDelivered) {
            if (this.getStatus() != IN_PREPARATION) {
                throw new UnauthorizedOperationException("You can't set an order in delivery if it's not in preparation");
            }
            for (OrderItem item : this.items) {
                item.setStatus(IN_DELIVERY);
            }
            NotificationService.sendNotificationToUser(UserLevel.USER, "Your buffet order is in delivery !", this.getCustomer().getFullName());
            return true;
        }
        throw new UnauthorizedOperationException("You can't set an order in delivery if it doesn't need to be delivered");
    }

    @Override
    public boolean setFinished() throws UnauthorizedOperationException {
        if (this.getStatus() != IN_DELIVERY && this.needToBeDelivered) {
            throw new UnauthorizedOperationException("You can't finish your order if it's not in delivery");
        }
        if (this.getStatus() != IN_PREPARATION && !this.needToBeDelivered) {
            throw new UnauthorizedOperationException("You can't finish your order if it's not in preparation");
        }
        for (OrderItem item : this.items) {
            item.setStatus(FINISH);
        }
        NotificationService.sendNotificationToUser(UserLevel.USER, "Your buffet order is finished !", this.getCustomer().getFullName());
        return true;
    }

    @Override
    public boolean addCustomerCredit(int nbOrders, double discountPercentage) throws UnauthorizedOperationException {
        return DiscountService.computeCustomerCredit(List.of(this), nbOrders, discountPercentage);
    }

    @Override
    public boolean updateDeliveryDate(DateTime newDeliveryDate) throws UnauthorizedModificationException {
        if (this.needToBeDelivered) {
            if (newDeliveryDate == null || newDeliveryDate.isBeforeNow()) {
                throw new IllegalArgumentException("Delivery date can't be null or in the past");
            }
            if (this.getStatus() != WAITING_RESTAURANT_ACCEPTANCE) {
                throw new UnauthorizedModificationException("You can't update the delivery date of an order that is not waiting for restaurant acceptance");
            }
            this.deliveryDate = newDeliveryDate;
            return true;
        }
        throw new UnauthorizedModificationException("You can't update the delivery date of an order that doesn't need to be delivered");
    }

    @Override
    public boolean updateDeliveryAddress(String newDeliveryAddress) throws UnauthorizedModificationException {
        if (this.needToBeDelivered) {
            if (this.getStatus() != WAITING_RESTAURANT_ACCEPTANCE && this.getStatus() != WAITING_PAYMENT) {
                throw new UnauthorizedModificationException("You can't update the delivery date of an order that is not waiting for restaurant acceptance");
            }
            if (newDeliveryAddress == null || newDeliveryAddress.isEmpty()) {
                throw new IllegalArgumentException("Delivery address can't be null or empty");
            }
            this.deliveryAddress = newDeliveryAddress;
            return true;
        }
        throw new UnauthorizedModificationException("You can't update the delivery address of an order that doesn't need to be delivered");
    }

    @Override
    public boolean cancel() {
        if (this.getStatus() == WAITING_RESTAURANT_ACCEPTANCE || this.getStatus() == WAITING_PAYMENT) {
            for (OrderItem item : this.items) {
                item.setStatus(CANCELED);
            }
            this.isCanceled = true;
            return true;
        }
        return false;
    }

    public Restaurant getRestaurant() {
        return this.restaurant;
    }

    @Override
    public boolean needToBeDelivered() {
        return this.needToBeDelivered;
    }

    @Override
    public boolean needToBePaid() {
        return this.needToBePaid;
    }
}
