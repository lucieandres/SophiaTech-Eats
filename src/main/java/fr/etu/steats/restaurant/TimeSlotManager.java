package fr.etu.steats.restaurant;

import fr.etu.steats.exception.UnauthorizedOperationException;
import fr.etu.steats.order.AfterWorkOrder;
import fr.etu.steats.order.OrderItem;
import org.joda.time.DateTime;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static fr.etu.steats.enums.EOrderStatus.IN_PREPARATION;
import static fr.etu.steats.enums.EOrderStatus.WAITING_RESTAURANT_ACCEPTANCE;
import static fr.etu.steats.restaurant.TimeSlot.TIME_SLOT_DURATION;

public class TimeSlotManager {
    public static final int MAX_NUMBER_OF_SLOT_TO_CHECK = 3;
    private LocalTime openingTime;
    private LocalTime closingTime;
    private final Map<DateTime, TimeSlot> slots;
    private final int capacity;

    public TimeSlotManager(LocalTime openingTime, LocalTime closingTime, int capacity) {
        this.openingTime = openingTime.withSecondOfMinute(0);
        this.closingTime = closingTime.withSecondOfMinute(0);
        slots = new HashMap<>();
        this.capacity = capacity;
    }

    public TimeSlotManager(int capacity) {
        this(new LocalTime(8, 0, 0), new LocalTime(20, 0, 0), capacity);
    }

    public TimeSlotManager() {
        this(5);
    }

    public LocalTime getOpeningTime() {
        return openingTime;
    }

    public LocalTime getClosingTime() {
        return closingTime;
    }

    public Map<DateTime, TimeSlot> getSlots() {
        return slots;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setOpeningTime(LocalTime openingTime) {
        if (openingTime == null) {
            throw new IllegalArgumentException("The opening time can't be null");
        }
        this.openingTime = openingTime.withSecondOfMinute(0);
    }

    public void setClosingTime(LocalTime closingTime) {
        if (closingTime == null) {
            throw new IllegalArgumentException("The opening time can't be null");
        }
        this.closingTime = closingTime.withSecondOfMinute(0);
    }

    public boolean canOrderBePreparedBeforeDeadline(List<OrderItem> items, DateTime deliveryDate) throws UnauthorizedOperationException {
        if (deliveryDate == null || deliveryDate.isBeforeNow()) {
            throw new UnauthorizedOperationException("You can't add items to prepare if they don't have a delivery date or if they're delivery date is already passed...");
        }
        if (!checkIfRestaurantOpen(new LocalDateTime(deliveryDate).minusMinutes(10))) {
            throw new UnauthorizedOperationException("You can't prepare an order when the restaurant is closed...");
        }
        if (items == null || items.isEmpty()) {
            throw new UnauthorizedOperationException("You need to have at least 1 item to perform this operation...");
        }
        List<TimeSlot> nonFullSlot = getTimeSlotForOrder(deliveryDate);

        if (nonFullSlot == null || nonFullSlot.isEmpty()) {
            return false;
        }
        return nonFullSlot.stream().mapToInt(TimeSlot::numberOfAvailablePlaceForItem).sum() >= items.size();
    }

    public boolean addOrderToPrepareWithDeadline(List<OrderItem> items, DateTime deliveryDate) throws UnauthorizedOperationException {
        if (!canOrderBePreparedBeforeDeadline(items, deliveryDate)) {
            return false;
        }

        List<OrderItem> itemsToPrepare = new ArrayList<>(items);
        List<TimeSlot> nonFullSlot = getTimeSlotForOrder(deliveryDate);

        for (TimeSlot slot : nonFullSlot) {
            while (slot.numberOfAvailablePlaceForItem() > 0 && !itemsToPrepare.isEmpty()) {
                slot.addOrderToTimeSlot(itemsToPrepare.get(0));
                itemsToPrepare.remove(0);
            }
        }

        return true;
    }

    public List<TimeSlot> getTimeSlotForOrder(DateTime deliveryDate) {
        LocalDateTime finalPreparationTime = new LocalDateTime(deliveryDate);
        if (!checkIfRestaurantOpen(finalPreparationTime)) {
            finalPreparationTime = closingTime.toDateTimeToday().toLocalDateTime();
        }

        // Rounding the final preparation time to the nearest previous possible timeSlotEnd, for example 18:34 => 18:30
        while (finalPreparationTime.getMinuteOfHour() % TIME_SLOT_DURATION != 0) {
            finalPreparationTime = finalPreparationTime.minusMinutes(1);
        }

        List<DateTime> slotsToCheck = getPreviousSlotDate(finalPreparationTime, deliveryDate);
        List<TimeSlot> nonFullSlot = new ArrayList<>();
        for (DateTime date : slotsToCheck) {
            // Case where the time slot need to be created
            if (!slots.containsKey(date) || slots.get(date) == null) {
                LocalTime tmp = new LocalTime(date);
                slots.put(date, new TimeSlot(tmp, capacity));
                nonFullSlot.add(slots.get(date));
            }
            // Case where he already exists and have space left
            else if (slots.get(date).numberOfAvailablePlaceForItem() > 0) {
                nonFullSlot.add(slots.get(date));
            }
        }
        return nonFullSlot;
    }

    public List<DateTime> getPreviousSlotDate(LocalDateTime finalPreparationTime, DateTime dateOfTheLocalTime) {
        if (!checkIfRestaurantOpen(finalPreparationTime)) {
            return new ArrayList<>();
        }
        DateTime copyOfTheDate = new DateTime(dateOfTheLocalTime);
        copyOfTheDate = copyOfTheDate.withMinuteOfHour(0);

        List<DateTime> probablyAvailableSlots = new ArrayList<>();
        LocalDateTime date = finalPreparationTime;
        while (date.plusMinutes((TIME_SLOT_DURATION) * MAX_NUMBER_OF_SLOT_TO_CHECK).isAfter(finalPreparationTime) && probablyAvailableSlots.size() < MAX_NUMBER_OF_SLOT_TO_CHECK) {
            date = date.minusMinutes(TIME_SLOT_DURATION);
            if (!checkIfRestaurantOpen(date)) {
                break;
            }

            copyOfTheDate = copyOfTheDate.withHourOfDay(date.getHourOfDay());
            copyOfTheDate = copyOfTheDate.withMinuteOfHour(date.getMinuteOfHour());

            probablyAvailableSlots.add(new DateTime(copyOfTheDate));
        }

        return probablyAvailableSlots;
    }

    public boolean checkIfRestaurantOpen(LocalDateTime time) {
        return time.toLocalTime().isAfter(openingTime) && time.toLocalTime().isBefore(closingTime) || time.toLocalTime().isBefore(openingTime) && time.toLocalTime().isBefore(closingTime) && openingTime.isAfter(closingTime);
    }

    public TimeSlot getNeareastTimeSlotWithWorkRemaining() {
        if (slots.isEmpty()) {
            return null;
        }

        for (Map.Entry<DateTime, TimeSlot> entry : slots.entrySet()) {
            for (OrderItem item : entry.getValue().getOrderToPrepareDuringTimeSlot()) {
                if ((item.getStatus() == IN_PREPARATION || item.getStatus() == WAITING_RESTAURANT_ACCEPTANCE)) {
                    return entry.getValue();
                }
            }
            for (AfterWorkOrder order : entry.getValue().getAfterWorkOrderCurrentlyOnGoing()) {
                if (order.getStatus() == IN_PREPARATION) {
                    return entry.getValue();
                }
            }
        }
        return null;
    }

    public boolean addAfterWorkOrder(AfterWorkOrder order) {
        List<TimeSlot> timeSlotForOrder = this.getTimeSlotForOrder(order.getDeliveryDate());
        if (timeSlotForOrder.isEmpty()) {
            return false;
        }
        return timeSlotForOrder.get(0).addAfterWorkOrderToTimeSlot(order);
    }
}
