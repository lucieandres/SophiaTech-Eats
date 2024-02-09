package fr.etu.steats.order;

import fr.etu.steats.account.CustomerAccount;
import fr.etu.steats.enums.EOrderStatus;
import fr.etu.steats.exception.UnauthorizedModificationException;
import fr.etu.steats.exception.UnauthorizedOperationException;
import fr.etu.steats.service.DiscountService;
import fr.etu.steats.service.NotificationService;
import fr.etu.steats.utils.UserLevel;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;

import static fr.etu.steats.enums.EOrderStatus.IN_DELIVERY;
import static fr.etu.steats.enums.EOrderStatus.WAITING_DELIVER_ACCEPTANCE;

public class GroupOrder extends OrderAbstract {
    private final List<OrderAbstract> orders;

    protected GroupOrder(CustomerAccount owner, DateTime deliveryDate, String deliveryAddress) {
        super(owner, deliveryDate, deliveryAddress);
        this.orders = new ArrayList<>();
    }

    @Override
    public boolean addSubOrder(OrderAbstract order) throws UnauthorizedOperationException {
        if (order == null) {
            throw new IllegalArgumentException("You can't add a null order inside a group order");
        }
        if (order.isPartOfGroupOrder()) {
            throw new UnauthorizedOperationException("This order is already part of a group order");
        }
        if (!order.statusAllowsUpdate() && order.getStatus() != WAITING_DELIVER_ACCEPTANCE) {
            throw new UnauthorizedOperationException("This order can't be added to a group order anymore");
        }
        order.setDeliveryAddress(this.getDeliveryAddress());
        order.setDeliveryDate(this.getDeliveryDate());
        order.setParentGroupOwner(this);
        return this.orders.add(order);
    }

    public List<OrderAbstract> getSubOrders() {
        return this.orders;
    }

    @Override
    public boolean allowSubOrder() {
        return true;
    }

    @Override
    public List<OrderItem> getItems() {
        List<OrderItem> items = new ArrayList<>();
        for (OrderAbstract suborder : getSubOrders()) {
            items.addAll(suborder.getItems());
        }
        return items;
    }

    public EOrderStatus getStatus() {
        return computeStatus(this.orders.stream().map(OrderAbstract::getStatus).toList());
    }

    @Override
    public boolean setWaitingRestaurantAcceptance() throws UnauthorizedOperationException {
        if (this.getStatus() != EOrderStatus.WAITING_PAYMENT) {
            throw new UnauthorizedOperationException("You can't wait for restaurant acceptance if the order is not waiting for customer acceptance");
        }

        if (orders == null || orders.isEmpty()) {
            throw new UnauthorizedOperationException("You can't wait for restaurant acceptance if the order is empty");
        }

        for (OrderAbstract order : this.orders) {
            order.setWaitingRestaurantAcceptance();
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

        if (orders == null || orders.isEmpty()) {
            throw new UnauthorizedOperationException("You can't deliver an empty order...");
        }

        for (OrderAbstract order : this.orders) {
            order.setInDelivery();
        }
        NotificationService.sendNotificationToUser(UserLevel.USER, "Your group order is in delivery !", this.getCustomer().getFullName());

        return true;
    }

    @Override
    public boolean setFinished() throws UnauthorizedOperationException {
        if (this.getStatus() != IN_DELIVERY) {
            throw new UnauthorizedOperationException("You can't close a non delivered order...");
        }

        if (orders == null || orders.isEmpty()) {
            throw new UnauthorizedOperationException("You can't give an empty order...");
        }

        for (OrderAbstract order : this.orders) {
            order.setFinished();
        }
        NotificationService.sendNotificationToUser(UserLevel.USER, "Your group order is delivered !", this.customer.getFullName());
        return true;
    }

    /**
     * Caution, this method consume the current credit of the user, we have a side effect, if you want to know the global price without any discount
     * please use getNonReducedTotalPrice()
     *
     * @return The price after discount and after consuming the credit of the customer
     */
    @Override
    public double getTotalPrice() {
        double total = 0.0;
        for (OrderAbstract subOrder : this.getSubOrders()) {
            total += subOrder.getTotalPrice();
        }
        return total;
    }

    @Override
    public boolean updateDeliveryDate(DateTime newDeliveryDate) throws UnauthorizedModificationException {
        if (newDeliveryDate == null) {
            throw new IllegalArgumentException("Delivery date can't be null");
        }
        if (!statusAllowsUpdate())
            throw new UnauthorizedModificationException("You can't update the delivery date of this order.");

        this.setDeliveryDate(newDeliveryDate);
        for (OrderAbstract order : this.orders) {
            boolean updated = order.updateDeliveryDate(newDeliveryDate);
            if (!updated) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean updateDeliveryAddress(String newDeliveryAddress) throws UnauthorizedModificationException {
        if (newDeliveryAddress == null) {
            throw new IllegalArgumentException("Delivery address can't be null");
        }
        if (!statusAllowsUpdate())
            throw new UnauthorizedModificationException("You can't update the delivery address of this order.");

        this.setDeliveryAddress(newDeliveryAddress);
        for (OrderAbstract order : this.orders) {
            boolean updated = order.updateDeliveryAddress(newDeliveryAddress);
            if (!updated) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean cancel() throws UnauthorizedModificationException {
        if (!statusAllowsUpdate())
            throw new UnauthorizedModificationException("You can't cancel this order.");

        for (OrderAbstract order : this.orders) {
            boolean updated = order.cancel();
            if (!updated) {
                return false;
            }
        }
        this.isCanceled = true;
        return true;
    }

    @Override
    public String toString() {
        return super.toString() +
                "sub orders=" + orders +
                '}';
    }

    @Override
    public boolean needToBePaid() {
        return false;
    }

    @Override
    public boolean addCustomerCredit(int nbOrders, double discountPercentage) throws UnauthorizedOperationException {
        List<OrderAbstract> orderList = this.getSubOrders().stream()
                .filter(SingleOrder.class::isInstance)
                .filter(singleOrder -> !singleOrder.getItems().isEmpty()).toList();

        return DiscountService.computeCustomerCredit(orderList, nbOrders, discountPercentage);
    }
}
