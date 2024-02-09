package fr.etu.steats.order;

import fr.etu.steats.account.CustomerAccount;
import fr.etu.steats.enums.EOrderStatus;
import fr.etu.steats.exception.UnauthorizedOperationException;
import fr.etu.steats.service.DiscountService;
import fr.etu.steats.service.NotificationService;
import fr.etu.steats.utils.UserLevel;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;

import static fr.etu.steats.enums.EOrderStatus.*;

public class SingleOrder extends OrderAbstract {
    private List<OrderItem> items;

    protected SingleOrder(CustomerAccount customer, DateTime deliveryDate, String deliveryAddress, List<OrderItem> items) {
        super(customer, deliveryDate, deliveryAddress);
        this.items = items;

        if (items == null) {
            this.items = new ArrayList<>();
        }
    }

    @Override
    public List<OrderItem> getItems() {
        return items;
    }

    public void addItem(OrderItem item) {
        if (item == null || item.getMenu() == null || item.getRestaurant() == null) {
            throw new IllegalArgumentException("An order item can't be null or incomplete");
        }
        this.items.add(item);
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

    public EOrderStatus getStatus() {
        return computeStatus(this.items.stream().map(OrderItem::getStatus).toList());
    }

    public boolean setWaitingRestaurantAcceptance() throws UnauthorizedOperationException {
        if (this.getStatus() != WAITING_PAYMENT) {
            throw new UnauthorizedOperationException("You can't wait for restaurant acceptance if you didn't order yet...");
        }

        if (items == null || items.isEmpty()) {
            throw new UnauthorizedOperationException("You can't wait for restaurant acceptance if you didn't order yet...");
        }

        for (OrderItem item : this.items) {
            item.setStatus(WAITING_RESTAURANT_ACCEPTANCE);
        }

        return true;
    }

    @Override
    public boolean setInDelivery() throws UnauthorizedOperationException {
        if (this.deliveryMan == null) {
            throw new UnauthorizedOperationException("You need a delivery man to put an order in delivery");
        }

        if (this.getStatus() != WAITING_DELIVER_ACCEPTANCE) {
            throw new UnauthorizedOperationException("You can't deliver a non ready order...");
        }

        if (items == null || items.isEmpty()) {
            throw new UnauthorizedOperationException("You can't deliver an empty order...");
        }

        for (OrderItem item : this.items) {
            item.setStatus(IN_DELIVERY);
        }
        NotificationService.sendNotificationToUser(UserLevel.USER, "Your order is in delivery !", this.customer.getFullName());
        return true;
    }

    @Override
    public boolean setFinished() throws UnauthorizedOperationException {
        if (this.getStatus() != IN_DELIVERY) {
            throw new UnauthorizedOperationException("You can't close a non delivered order...");
        }

        if (items == null || items.isEmpty()) {
            throw new UnauthorizedOperationException("You can't give an empty order...");
        }

        for (OrderItem item : this.items) {
            item.setStatus(FINISH);
        }
        NotificationService.sendNotificationToUser(UserLevel.USER, "Your order is delivered !", this.customer.getFullName());
        return true;
    }

    @Override
    public boolean addCustomerCredit(int nbOrders, double discountPercentage) throws UnauthorizedOperationException {
        return DiscountService.computeCustomerCredit(List.of(this), nbOrders, discountPercentage);
    }

    @Override
    public boolean updateDeliveryDate(DateTime newDeliveryDate) {
        if (newDeliveryDate == null) {
            throw new IllegalArgumentException("Delivery date can't be null");
        }
        if (statusAllowsUpdate()) {
            this.deliveryDate = newDeliveryDate;
            return true;
        }
        return false;
    }

    @Override
    public boolean updateDeliveryAddress(String newDeliveryAddress) {
        if (newDeliveryAddress == null) {
            throw new IllegalArgumentException("Delivery address can't be null");
        }
        if (statusAllowsUpdate()) {
            this.deliveryAddress = newDeliveryAddress;
            return true;
        }
        return false;
    }

    public boolean updateItems(List<OrderItem> newItems) {
        if (newItems == null) {
            throw new IllegalArgumentException("Items can't be null");
        }
        if (statusAllowsUpdate()) {
            this.items = newItems;
            return true;
        }
        return false;
    }

    public boolean cancel() {
        if (statusAllowsUpdate()) {
            for (OrderItem item : this.items) {
                item.setStatus(CANCELED);
            }
            this.isCanceled = true;
            return true;
        }
        return false;
    }

    @Override
    public String toString() {
        return super.toString() +
                "items=" + items +
                '}';
    }
}
