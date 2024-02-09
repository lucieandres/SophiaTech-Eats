package fr.etu.steats.restaurant;

import fr.etu.steats.order.AfterWorkOrder;
import fr.etu.steats.order.OrderItem;
import org.joda.time.LocalTime;

import java.util.ArrayList;
import java.util.List;

public class TimeSlot {
    public static final int TIME_SLOT_DURATION = 10;
    private final LocalTime beginningTime;
    private final LocalTime finishingTime;
    private final int capacity;
    private final List<OrderItem> orderToPrepareDuringTimeSlot;

    private final List<AfterWorkOrder> afterWorkCurrentlyOnGoing;

    public TimeSlot(LocalTime beginningTime, int capacity, List<OrderItem> orderToPrepare, List<AfterWorkOrder> afterWorkOrders) {
        this.beginningTime = beginningTime;
        this.finishingTime = beginningTime.plusMinutes(TIME_SLOT_DURATION);
        this.capacity = capacity;
        this.orderToPrepareDuringTimeSlot = orderToPrepare;
        this.afterWorkCurrentlyOnGoing = afterWorkOrders;
    }

    public TimeSlot(LocalTime beginningTime, int capacity, List<OrderItem> orderToPrepare) {
        this(beginningTime, capacity, orderToPrepare, new ArrayList<>());
    }

    public TimeSlot(LocalTime beginningTime, int capacity) {
        this(beginningTime, capacity, new ArrayList<>(), new ArrayList<>());
    }

    public LocalTime getBeginningTime() {
        return beginningTime;
    }

    public LocalTime getFinishingTime() {
        return finishingTime;
    }

    public int getCapacity() {
        return capacity;
    }

    public List<OrderItem> getOrderToPrepareDuringTimeSlot() {
        return orderToPrepareDuringTimeSlot;
    }

    public boolean isOnGoing() {
        LocalTime now = new LocalTime();
        return now.isAfter(beginningTime) && now.isBefore(finishingTime);
    }

    public boolean addOrderToTimeSlot(OrderItem item) {
        if (item == null) {
            throw new IllegalArgumentException("You can't add a null order item to the time slot...");
        }
        if (orderToPrepareDuringTimeSlot.size() < capacity) {
            return orderToPrepareDuringTimeSlot.add(item);
        }
        return false;
    }

    public boolean addAfterWorkOrderToTimeSlot(AfterWorkOrder order) {
        return this.afterWorkCurrentlyOnGoing.add(order);
    }

    public int numberOfAvailablePlaceForItem() {
        return capacity - orderToPrepareDuringTimeSlot.size();
    }

    public List<AfterWorkOrder> getAfterWorkOrderCurrentlyOnGoing() {
        return this.afterWorkCurrentlyOnGoing;
    }
}
