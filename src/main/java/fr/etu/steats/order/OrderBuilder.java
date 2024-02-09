package fr.etu.steats.order;

import fr.etu.steats.account.AdminAccount;
import fr.etu.steats.account.CustomerAccount;
import fr.etu.steats.enums.EOrderStatus;
import fr.etu.steats.exception.UnauthorizedOperationException;
import fr.etu.steats.restaurant.Restaurant;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OrderBuilder {
    private final DateTime deliveryDate;
    private final CustomerAccount customer;
    private Restaurant restaurant;
    private AdminAccount staff;
    private String deliveryAddress;
    private Map<OrderItem, Integer> items;
    private int numberOfParticipant;

    public OrderBuilder(DateTime deliveryDate, CustomerAccount customer) {
        if (deliveryDate == null || deliveryDate.minusSeconds(1).isBeforeNow() || customer == null) {
            throw new IllegalArgumentException("An order need a deliveryDate in the future and a customer.");
        }
        this.deliveryDate = deliveryDate;
        this.customer = customer;
    }

    public OrderBuilder addMenuItem(OrderItem item, int quantity) {
        if (item == null || quantity <= 0) {
            return this;
        }
        if (this.items == null) {
            this.items = new HashMap<>();
        }

        if (this.items.containsKey(item)) {
            this.items.put(item, this.items.get(item) + quantity);
        } else {
            this.items.put(item, quantity);
        }

        return this;
    }

    public OrderBuilder addMenuItems(List<OrderItem> itemList) {
        if (itemList == null || itemList.isEmpty()) {
            return this;
        }
        if (this.items == null) {
            this.items = new HashMap<>();
        }

        for (OrderItem item : itemList) {
            if (this.items.containsKey(item)) {
                this.items.put(item, this.items.get(item) + 1);
            } else {
                this.items.put(item, 1);
            }
        }

        return this;
    }

    public OrderBuilder setNumberOfParticipant(int number) {
        if (number <= 0) {
            this.numberOfParticipant = 0;
            return this;
        }
        this.numberOfParticipant = number;
        return this;
    }

    public OrderBuilder setStaff(AdminAccount staff) {
        this.staff = staff;
        return this;
    }

    public OrderBuilder setDeliveryAddress(String address) {
        this.deliveryAddress = address;
        return this;
    }

    public OrderBuilder setRestaurant(Restaurant restaurant) {
        if (restaurant == null) {
            throw new IllegalArgumentException("You can't add a null restaurant");
        }
        this.restaurant = restaurant;
        return this;
    }

    public OrderAbstract build() throws UnauthorizedOperationException {
        if (this.deliveryAddress != null && !this.deliveryAddress.isEmpty()) {
            if ((this.items == null || this.items.isEmpty())) {
                return new GroupOrder(this.customer, this.deliveryDate, this.deliveryAddress);
            }
            return new SingleOrder(this.customer, this.deliveryDate, this.deliveryAddress, getItemList());
        }
        if (!this.items.isEmpty() && this.restaurant != null) {
            if (this.staff != null) {
                return new BuffetOrder(this.customer, this.deliveryDate, getItemList(), this.restaurant);
            }
            if (numberOfParticipant > 0) {
                List<OrderItem> itemList = new ArrayList<>();
                restaurant.getMenuItems().forEach(item -> {
                    OrderItem newItem = new OrderItem(item, restaurant);
                    newItem.setStatus(EOrderStatus.IN_PREPARATION);
                    itemList.add(new OrderItem(item, restaurant));
                });
                return new AfterWorkOrder(customer, deliveryDate, deliveryAddress, itemList, numberOfParticipant);
            }
        }
        throw new UnauthorizedOperationException("You didn't provide enough data to build an order.");
    }

    private List<OrderItem> getItemList() {
        List<OrderItem> itemList = new ArrayList<>();
        for (Map.Entry<OrderItem, Integer> itemByQuantity : this.items.entrySet()) {
            OrderItem item = itemByQuantity.getKey();
            for (int quantity = 0; quantity < itemByQuantity.getValue(); quantity++) {
                itemList.add(item);
            }
        }
        return itemList;
    }
}
