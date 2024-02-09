package fr.etu.steats.restaurant;

import fr.etu.steats.enums.EOrderStatus;
import fr.etu.steats.exception.BadPasswordException;
import fr.etu.steats.exception.UnauthorizedOperationException;
import fr.etu.steats.order.AfterWorkOrder;
import fr.etu.steats.order.OrderItem;
import fr.etu.steats.service.NotificationService;
import fr.etu.steats.utils.Scheduler;
import fr.etu.steats.utils.UserLevel;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;

/**
 * This class represents a restaurant.
 * A restaurant can manage its menu, orders and prepare them.
 */
public class Restaurant {
    /**
     * The password is used to identify the restaurant. Used by the restaurant manager to log in.
     */
    private final String password;
    private String name;
    /**
     * The id of the restaurant is used to identify it. It is unique.
     */
    private final int id;
    /**
     * The menu of the restaurant.
     */
    private final List<Menu> menuItems = new ArrayList<>();
    /**
     * The orders in preparation by the restaurant.
     */
    private final List<OrderItem> orders = new ArrayList<>();
    private final List<OrderItem> oldOrders = new ArrayList<>();
    /**
     * The scheduler is used to simulate the preparation of an order.
     */
    private final Scheduler scheduler;
    private final TimeSlotManager timeSlotManager;
    private final String address;

    public Restaurant(String name, int id, String password, String address) {
        this(name, id, password, new Scheduler(), address);
    }

    public Restaurant(String name, int id, String password, Scheduler scheduler, String address) {
        this.name = name.trim().toLowerCase();
        this.id = id;
        this.password = password;
        this.scheduler = scheduler;
        this.timeSlotManager = new TimeSlotManager();
        this.address = address;
    }

    public Restaurant(String name, int id, String password, Scheduler scheduler, int capacity, String address) {
        this.name = name.trim().toLowerCase();
        this.id = id;
        this.password = password;
        this.scheduler = scheduler;
        this.timeSlotManager = new TimeSlotManager(capacity);
        this.address = address;
    }

    public void addMenuItem(Menu item) {
        if (item == null) {
            throw new IllegalArgumentException("item cannot be null");
        }
        if (menuItems.contains(item)) {
            throw new IllegalArgumentException("item already exists");
        }
        this.menuItems.add(item);
    }


    public void removeMenuItem(String itemName) {
        if (itemName == null || itemName.isEmpty()) {
            throw new IllegalArgumentException("itemName cannot be null or empty");
        }
        menuItems.removeIf(item -> item.getName().equals(itemName));
    }

    public Menu getMenuItem(String itemName) {
        for (Menu item : menuItems) {
            if (item.getName().equals(itemName)) {
                return item;
            }
        }
        return null;
    }

    public void updateMenuItem(String itemName, Menu newMenuItem) {
        if (itemName == null || itemName.isEmpty()) {
            throw new IllegalArgumentException("itemName cannot be null or empty");
        }
        for (Menu item : menuItems) {
            if (item.getName().equals(itemName)) {
                item.setName(newMenuItem.getName());
                item.setGlobalPrice(newMenuItem.getGlobalPrice());
                item.setStudentPrice(newMenuItem.getStudentPrice());
                item.setFacultyPrice(newMenuItem.getFacultyPrice());
                item.setExternalPrice(newMenuItem.getExternalPrice());
                item.setStaffPrice(newMenuItem.getStaffPrice());
                return;
            } else {
                throw new IllegalArgumentException("itemName does not exist");
            }
        }
    }

    public TimeSlotManager getTimeSlotManager() {
        return timeSlotManager;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("Name cannot be null or empty");
        }
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public List<Menu> getMenuItems() {
        return menuItems;
    }

    public boolean checkPassword(String password) throws BadPasswordException {
        if (!this.password.equals(password)) {
            throw new BadPasswordException("The password you provided is incorrect, please retry. \nIf you forgot your password, contact an administrator to change your it.");
        }
        return true;
    }

    public List<OrderItem> getOrders() {
        return orders;
    }

    public void addOrder(OrderItem order) {
        if (order == null) {
            throw new IllegalArgumentException("order cannot be null");
        }
        if (orders.contains(order)) {
            throw new IllegalArgumentException("order already exists");
        }
        this.orders.add(order);
        NotificationService.sendNotificationToUser(UserLevel.RESTAURANT_MANAGER, "You have a new order to prepare !", this.getName());
    }

    public void addAfterWorkOrder(AfterWorkOrder order) {
        if (this.timeSlotManager.addAfterWorkOrder(order)) {
            NotificationService.sendNotificationToUser(UserLevel.RESTAURANT_MANAGER, "You have a new after work order to prepare !", this.getName());
        }
    }

    protected void removeOrder(OrderItem order) {
        if (order == null) {
            throw new IllegalArgumentException("order cannot be null");
        }
        if (!orders.contains(order)) {
            throw new IllegalArgumentException("order does not exist");
        }
        for (TimeSlot slot : timeSlotManager.getSlots().values()) {
            slot.getOrderToPrepareDuringTimeSlot().remove(order);
        }
        this.orders.remove(order);
    }

    protected void verifyOrder(OrderItem order) {
        if (order == null) {
            throw new IllegalArgumentException("Order cannot be null");
        }
        if (!orders.contains(order)) {
            throw new IllegalArgumentException("Order does not exist");
        }
        if (this.getMenuItem(order.getMenu().getName()) != order.getMenu()) {
            throw new IllegalArgumentException("The order does not belong to this restaurant");
        }
        if (order.getMenu() == null) {
            throw new IllegalArgumentException("The order does not have a menu");
        }
        if (order.getRestaurant() != this) {
            throw new IllegalArgumentException("The order does not belong to this restaurant");
        }
    }

    public void prepareOrder(int orderId) throws InterruptedException {
        OrderItem order = orders.get(orderId - 1);
        verifyOrder(order);
        order.setStatus(EOrderStatus.IN_PREPARATION);
        NotificationService.sendNotificationToUser(UserLevel.USER, "Your order n°" + orderId + " is in preparation in the restaurant " + this.getName() + ".");
        scheduler.waitTenMinutes();
        order.setStatus(EOrderStatus.WAITING_DELIVER_ACCEPTANCE);
        NotificationService.sendNotificationToUser(UserLevel.USER, "Your order n°" + orderId + " is prepared ! Waiting for the delivery man to take it.");
        removeOrder(order);
    }

    public void prepareAllOrderOfNearestTimeSlot() throws InterruptedException, UnauthorizedOperationException {
        TimeSlot timeSlot = timeSlotManager.getNeareastTimeSlotWithWorkRemaining();

        for (AfterWorkOrder order : timeSlot.getAfterWorkOrderCurrentlyOnGoing()) {
            order.setFinished();
        }

        for (OrderItem order : timeSlot.getOrderToPrepareDuringTimeSlot()) {
            verifyOrder(order);
        }

        timeSlot.getOrderToPrepareDuringTimeSlot().forEach(orderItem -> orderItem.setStatus(EOrderStatus.IN_PREPARATION));

        scheduler.waitTenMinutes();

        timeSlot.getOrderToPrepareDuringTimeSlot()
                .forEach(orderItem -> {
                    if (orderItem.needToBeDelivered()) {
                        orderItem.setStatus(EOrderStatus.WAITING_DELIVER_ACCEPTANCE);
                    } else {
                        orderItem.setStatus(EOrderStatus.FINISH);
                    }
                });

        oldOrders.addAll(timeSlot.getOrderToPrepareDuringTimeSlot());
        orders.removeAll(timeSlot.getOrderToPrepareDuringTimeSlot());
    }

    public void cancelOrder(int orderId) {
        OrderItem order = orders.get(orderId - 1);
        verifyOrder(order);
        order.setStatus(EOrderStatus.CANCELED);
        removeOrder(order);
        NotificationService.sendNotificationToUser(UserLevel.USER, "Your order " + orderId + " has been cancelled.");
    }

    public boolean canOrderBePreparedBeforeDeadline(List<OrderItem> items, DateTime deliveryDate) throws UnauthorizedOperationException {
        return timeSlotManager.canOrderBePreparedBeforeDeadline(items, deliveryDate);
    }

    public boolean addOrderListWhoNeedToBePrepareBeforeDeadline(List<OrderItem> items, DateTime deliveryDate) throws UnauthorizedOperationException {
        if (timeSlotManager.addOrderToPrepareWithDeadline(List.copyOf(items), deliveryDate) && this.orders.addAll(items)) {
            NotificationService.sendNotificationToUser(UserLevel.RESTAURANT_MANAGER, "You have a new order to prepare !", this.getName());
            return true;
        }
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Restaurant that = (Restaurant) o;

        return id == that.id;
    }

    @Override
    public int hashCode() {
        return id;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        if (address == null || address.isEmpty()) {
            throw new IllegalArgumentException("Address cannot be null or empty");
        }
        this.name = address;
    }

    public List<Menu> getAfterWorkMenuItems() {
        return this.getMenuItems().stream().filter(Menu::isAfterWork).toList();
    }
}
