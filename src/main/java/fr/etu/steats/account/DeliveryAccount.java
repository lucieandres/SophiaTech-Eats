package fr.etu.steats.account;

import fr.etu.steats.exception.UnauthorizedOperationException;
import fr.etu.steats.order.OrderAbstract;
import fr.etu.steats.service.NotificationService;
import fr.etu.steats.service.OrderService;
import fr.etu.steats.utils.Scheduler;
import fr.etu.steats.utils.UserLevel;

import java.util.Objects;

import static fr.etu.steats.enums.EOrderStatus.WAITING_DELIVER_ACCEPTANCE;

public class DeliveryAccount extends AccountAbstract {
    private OrderAbstract assignedOrder;
    private final Scheduler scheduler;

    public DeliveryAccount(String firstName, String lastName, String password) {
        this(firstName, lastName, password, new Scheduler());
    }

    public DeliveryAccount(String firstName, String lastName, String password, Scheduler scheduler) {
        super(firstName, lastName, password);
        this.scheduler = (scheduler == null) ? new Scheduler() : scheduler;
    }

    public void setAssignedOrder(OrderAbstract orderAbstract) {
        this.assignedOrder = orderAbstract;
        this.assignedOrder.assignDeliveryMan(this);
        NotificationService.sendNotificationToUser(UserLevel.DELIVERY_MAN, "New order n°" + orderAbstract.getId() + " to deliver to " + this.assignedOrder.getDeliveryAddress(), this.getFullName());
    }

    public void putOrderInDelivery() throws UnauthorizedOperationException, InterruptedException {
        if (this.assignedOrder != null && this.assignedOrder.getStatus().equals(WAITING_DELIVER_ACCEPTANCE)) {
            this.assignedOrder.setInDelivery();
            scheduler.waitTenMinutes();
            this.assignedOrder.setFinished();
            this.assignedOrder.addCustomerCredit(OrderService.MIN_NUMBER_OF_ORDERS, OrderService.DISCOUNT);
            this.assignedOrder = null;
        }
        NotificationService.sendNotificationToUser(UserLevel.DELIVERY_MAN, "Congrats ! You have finished your delivery !", this.getFullName());
    }

    public OrderAbstract getAssignedOrder() {
        return assignedOrder;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        DeliveryAccount otherAccount = (DeliveryAccount) o;

        return Objects.equals(assignedOrder, otherAccount.assignedOrder);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (assignedOrder != null ? assignedOrder.hashCode() : 0);
        return result;
    }
}
