package fr.etu.steats.order;

import fr.etu.steats.account.CustomerAccount;
import fr.etu.steats.enums.EOrderStatus;
import fr.etu.steats.exception.UnauthorizedModificationException;
import fr.etu.steats.exception.UnauthorizedOperationException;
import fr.etu.steats.service.DiscountService;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;

import static fr.etu.steats.enums.EOrderStatus.CANCELED;
import static fr.etu.steats.enums.EOrderStatus.FINISH;

public class AfterWorkOrder extends OrderAbstract {
    private List<OrderItem> items;

    private int numberOfParticipant;

    protected AfterWorkOrder(CustomerAccount customer, DateTime deliveryDate, String deliveryAddress, List<OrderItem> items, int numberOfParticipant) {
        super(customer, deliveryDate, deliveryAddress);
        this.numberOfParticipant = numberOfParticipant;
        this.items = items;
        if (items == null) {
            this.items = new ArrayList<>();
        }
    }

    @Override
    public boolean needToBePaid() {
        return false;
    }

    @Override
    public boolean needToBeDelivered() {
        return false;
    }

    @Override
    public List<OrderItem> getItems() {
        return items;
    }

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
    public boolean setWaitingRestaurantAcceptance() throws UnauthorizedOperationException {
        throw new UnauthorizedOperationException("You can't set an after work order in waiting restaurant acceptance");
    }

    @Override
    public boolean setInDelivery() throws UnauthorizedOperationException {
        throw new UnauthorizedOperationException("You can't set an after work order in delivery");
    }

    @Override
    public boolean setFinished() throws UnauthorizedOperationException {
        if (items.get(0).getStatus().equals(FINISH)) {
            throw new UnauthorizedOperationException("It's already in finished status");
        }
        if (items.get(0).getStatus().equals(CANCELED)) {
            throw new UnauthorizedOperationException("It's already in canceled status");
        }
        for (OrderItem item : this.items) {
            item.setStatus(FINISH);
        }
        return true;
    }

    @Override
    public boolean addCustomerCredit(int nbOrders, double discountPercentage) throws UnauthorizedOperationException {
        return DiscountService.computeCustomerCredit(List.of(this), nbOrders, discountPercentage);
    }

    @Override
    public boolean updateDeliveryDate(DateTime newDeliveryDate) throws UnauthorizedModificationException {
        if (items.get(0).getStatus().equals(CANCELED) || items.get(0).getStatus().equals(EOrderStatus.FINISH)) {
            throw new UnauthorizedModificationException("You can't change the delivery date of a canceled or finished order");
        }
        this.deliveryDate = newDeliveryDate;
        return true;

    }

    @Override
    public boolean updateDeliveryAddress(String newDeliveryAddress) throws UnauthorizedModificationException {
        throw new UnauthorizedModificationException("You can't change the delivery address of an after work order");
    }

    @Override
    public boolean cancel() throws UnauthorizedModificationException {
        if (statusAllowsUpdate()) {
            for (OrderItem item : this.items) {
                item.setStatus(CANCELED);
            }
            this.isCanceled = true;
            return true;
        }
        return false;
    }

    public int getNumberOfParticipant() {
        return numberOfParticipant;
    }

    public void setNumberOfParticipant(int numberOfParticipant) {
        if (deliveryDate.compareTo(DateTime.now()) > 0) {
            throw new IllegalArgumentException("You can't change the number of participant after the delivery date");
        }
        this.numberOfParticipant = numberOfParticipant;
    }
}
