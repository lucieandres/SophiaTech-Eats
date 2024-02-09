package fr.etu.steats.order;

import fr.etu.steats.enums.ECustomerStatus;
import fr.etu.steats.enums.EOrderStatus;
import fr.etu.steats.restaurant.Menu;
import fr.etu.steats.restaurant.Restaurant;

public class OrderItem {
    private final Menu menu;
    private final Restaurant restaurant;
    private EOrderStatus status;
    private boolean deliverable;

    public OrderItem(Menu menu, Restaurant restaurant) {
        if (menu == null || restaurant == null) {
            throw new IllegalArgumentException("The menu and the restaurant can't be null for an orderItem");
        }
        this.menu = menu;
        this.restaurant = restaurant;
        this.status = EOrderStatus.WAITING_PAYMENT;
        this.deliverable = true;
    }

    public Menu getMenu() {
        return this.menu;
    }

    public Restaurant getRestaurant() {
        return this.restaurant;
    }

    public void setStatus(EOrderStatus status) {
        this.status = status;
    }

    public EOrderStatus getStatus() {
        return this.status;
    }

    public boolean needToBeDelivered() {
        return this.deliverable;
    }

    public void setNeedToBeDelivered(boolean bool) {
        this.deliverable = bool;
    }

    public double getPrice(ECustomerStatus typeOfCustomer) {
        return this.menu.getPrice(typeOfCustomer);
    }
}
