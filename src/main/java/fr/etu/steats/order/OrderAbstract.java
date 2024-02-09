package fr.etu.steats.order;

import fr.etu.steats.account.CustomerAccount;
import fr.etu.steats.account.DeliveryAccount;
import fr.etu.steats.enums.EOrderStatus;
import fr.etu.steats.exception.UnauthorizedModificationException;
import fr.etu.steats.exception.UnauthorizedOperationException;
import fr.etu.steats.service.DiscountService;
import org.joda.time.DateTime;

import java.util.List;

import static fr.etu.steats.enums.EOrderStatus.CANCELED;
import static fr.etu.steats.enums.EOrderStatus.WAITING_PAYMENT;

public abstract class OrderAbstract {

    protected static int nextId = 1;
    protected final int id;
    protected final CustomerAccount customer;
    protected DateTime deliveryDate;
    protected String deliveryAddress;
    protected boolean isCanceled;
    protected DeliveryAccount deliveryMan;
    private OrderAbstract parentGroupOwner;

    protected OrderAbstract(CustomerAccount customer, DateTime deliveryDate, String deliveryAddress) {
        if (customer == null || deliveryDate == null) {
            throw new IllegalArgumentException("The customer, delivery date and address can't be null");
        }
        this.id = nextId;
        nextId++;
        this.customer = customer;
        this.deliveryDate = deliveryDate;
        this.deliveryAddress = deliveryAddress;
        this.isCanceled = false;
    }

    public int getId() {
        return id;
    }

    public CustomerAccount getCustomer() {
        return this.customer;
    }

    public DateTime getDeliveryDate() {
        if (isPartOfGroupOrder()) {
            return parentGroupOwner.getDeliveryDate();
        }
        return deliveryDate;
    }

    public void setDeliveryDate(DateTime deliveryDate) {
        this.deliveryDate = deliveryDate;
    }

    public String getDeliveryAddress() {
        if (isPartOfGroupOrder()) {
            return parentGroupOwner.getDeliveryAddress();
        }
        return deliveryAddress;
    }

    public void setDeliveryAddress(String deliveryAddress) {
        this.deliveryAddress = deliveryAddress;
    }

    public abstract List<OrderItem> getItems();

    public double getTotalPrice() {
        if (needToBePaid()) {
            return DiscountService.computePriceAfterDiscount(this);
        }
        return 0;
    }

    public double getNonReducedTotalPrice() {
        if (needToBePaid()) {
            return this.getItems().stream().mapToDouble(item -> item.getPrice(this.customer.getType())).sum();
        }
        return 0;
    }

    public abstract EOrderStatus getStatus();

    public EOrderStatus computeStatus(List<EOrderStatus> status) {
        EOrderStatus res = null;
        for (EOrderStatus aStatus : status) {
            if (List.of(EOrderStatus.WAITING_PAYMENT, EOrderStatus.IN_DELIVERY, EOrderStatus.FINISH, EOrderStatus.CANCELED).contains(aStatus)) {
                return aStatus;
            }
            if (res == null) {
                res = aStatus;
            } else if (aStatus == EOrderStatus.WAITING_RESTAURANT_ACCEPTANCE || res == EOrderStatus.WAITING_RESTAURANT_ACCEPTANCE) {
                res = EOrderStatus.WAITING_RESTAURANT_ACCEPTANCE;
            } else if (aStatus == EOrderStatus.IN_PREPARATION || res == EOrderStatus.IN_PREPARATION) {
                res = EOrderStatus.IN_PREPARATION;
            } else {
                res = EOrderStatus.WAITING_DELIVER_ACCEPTANCE;
            }
        }

        EOrderStatus defaultStatus = (isCanceled) ? CANCELED : WAITING_PAYMENT;

        return (res != null) ? res : defaultStatus;
    }

    public void assignDeliveryMan(DeliveryAccount deliveryMan) {
        this.deliveryMan = deliveryMan;
    }

    public abstract boolean setWaitingRestaurantAcceptance() throws UnauthorizedOperationException;

    public abstract boolean setInDelivery() throws UnauthorizedOperationException;

    public abstract boolean setFinished() throws UnauthorizedOperationException;

    /**
     * @param nbOrder            the minimum number of menus for discount eligibility
     * @param discountPercentage the discount percentage
     * @return wether the discount has been applied
     */
    public abstract boolean addCustomerCredit(int nbOrder, double discountPercentage) throws UnauthorizedOperationException;

    public DeliveryAccount getDeliveryMan() {
        return this.deliveryMan;
    }

    public boolean isPartOfGroupOrder() {
        return parentGroupOwner != null;
    }

    public void setParentGroupOwner(OrderAbstract joinedGroupOrder) {
        if (joinedGroupOrder.allowSubOrder()) {
            parentGroupOwner = joinedGroupOrder;
        }
    }

    public boolean allowSubOrder() {
        return false;
    }

    public boolean addSubOrder(OrderAbstract order) throws UnauthorizedOperationException {
        return false;
    }

    public abstract boolean updateDeliveryDate(DateTime newDeliveryDate) throws UnauthorizedModificationException;

    public abstract boolean updateDeliveryAddress(String newDeliveryAddress) throws UnauthorizedModificationException;

    public abstract boolean cancel() throws UnauthorizedModificationException;

    public boolean statusAllowsUpdate() {
        return (getStatus() == EOrderStatus.WAITING_RESTAURANT_ACCEPTANCE || getStatus() == EOrderStatus.WAITING_PAYMENT) && !isPartOfGroupOrder();
    }

    public CustomerAccount globalOwner() {
        return (this.parentGroupOwner != null) ? parentGroupOwner.globalOwner() : this.getCustomer();
    }

    public boolean canBeSubOrder() {
        return true;
    }

    public boolean needToBePaid() {
        return true;
    }

    public boolean needToBeDelivered() {
        return true;
    }

    @Override
    public String toString() {
        return "OrderAbstract{" +
                "id=" + id +
                ", customer=" + customer +
                ", deliveryDate=" + deliveryDate +
                ", deliveryAddress='" + deliveryAddress + '\'' +
                ", deliveryMan=" + deliveryMan +
                '}';
    }
}
