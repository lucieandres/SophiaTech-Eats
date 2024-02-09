package fr.etu.steats.service;

import fr.etu.steats.account.AdminAccount;
import fr.etu.steats.account.CustomerAccount;
import fr.etu.steats.exception.UnauthorizedModificationException;
import fr.etu.steats.exception.UnauthorizedOperationException;
import fr.etu.steats.order.*;
import fr.etu.steats.restaurant.Restaurant;
import fr.etu.steats.utils.UserLevel;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static fr.etu.steats.enums.EOrderStatus.*;

public class OrderService {
    private static final String ORDER_ERROR = "At this stage of the order you can't add new element, please create a separate order.";
    private static final String PAYMENT_ERROR = "Payment failed";
    private final PaymentService paymentService;
    private final List<OrderAbstract> orders;
    public static final int MIN_NUMBER_OF_ORDERS = 10;
    public static final double DISCOUNT = 0.1;

    public OrderService() {
        this(new PaymentService());
    }

    public OrderService(PaymentService paymentService) {
        this.paymentService = paymentService;
        this.orders = new ArrayList<>();
    }

    public List<OrderAbstract> fetchAllOrder() {
        return this.orders;
    }

    public boolean createSingleOrder(CustomerAccount customer, List<OrderItem> items, DateTime deliveryDate, String deliveryAddress) throws UnauthorizedOperationException {
        if (customer != null && items != null && !items.isEmpty() && deliveryDate != null && deliveryAddress != null) {
            OrderAbstract order = new OrderBuilder(deliveryDate, customer)
                    .setDeliveryAddress(deliveryAddress)
                    .addMenuItems(items)
                    .build();

            Map<Restaurant, List<OrderItem>> restaurantItems = order.getItems().stream()
                    .collect(Collectors.groupingBy(OrderItem::getRestaurant, HashMap::new, Collectors.toList()));

            // Check if they all can be handled by the restaurant before the deliveryDate, else they all are refused
            for (Map.Entry<Restaurant, List<OrderItem>> entry : restaurantItems.entrySet()) {
                if (!entry.getKey().canOrderBePreparedBeforeDeadline(entry.getValue(), deliveryDate)) {
                    return false;
                }
            }
            if (!paymentService.pay(order.getTotalPrice())) {
                throw new UnauthorizedOperationException(PAYMENT_ERROR);
            }

            order.getItems().forEach(item -> item.setStatus(WAITING_RESTAURANT_ACCEPTANCE));

            for (Map.Entry<Restaurant, List<OrderItem>> entry : restaurantItems.entrySet()) {
                entry.getKey().addOrderListWhoNeedToBePrepareBeforeDeadline(entry.getValue(), deliveryDate);
            }

            this.orders.add(order);
            customer.addOrder(order);
            NotificationService.sendNotificationToUser(UserLevel.USER, "Your single order n°" + order.getId() + " has been created ! To " + order.getDeliveryAddress(), customer.getFullName());
            return true;
        }
        return false;
    }

    public boolean createGroupOrder(CustomerAccount customer, DateTime deliveryDate, String deliveryAddress) throws UnauthorizedOperationException {
        if (customer != null && deliveryDate != null && deliveryAddress != null) {

            OrderAbstract order = new OrderBuilder(deliveryDate, customer)
                    .setDeliveryAddress(deliveryAddress)
                    .build();

            this.orders.add(order);
            NotificationService.sendNotificationToUser(UserLevel.USER, "Your group order n°" + order.getId() + " has been created ! To " + order.getDeliveryAddress(), customer.getFullName());
            return true;
        }
        return false;
    }

    public boolean createBuffetOrder(CustomerAccount customer, DateTime deliveryDate, List<OrderItem> items, AdminAccount adminAccount, Restaurant restaurant) throws UnauthorizedOperationException {
        if (customer != null && deliveryDate != null && items != null && adminAccount != null && restaurant != null) {
            OrderAbstract bufferOrder = new OrderBuilder(deliveryDate, customer)
                    .addMenuItems(items)
                    .setRestaurant(restaurant)
                    .setStaff(adminAccount)
                    .build();

            if (!restaurant.canOrderBePreparedBeforeDeadline(items, deliveryDate)) {
                throw new UnauthorizedOperationException("The restaurant can't prepare this buffet order before the deadline. Please contact the restaurant.");
            }

            if (bufferOrder.needToBePaid()) {
                if (paymentService.pay(bufferOrder.getTotalPrice())) {
                    bufferOrder.getItems().forEach(item -> item.setStatus(WAITING_RESTAURANT_ACCEPTANCE));
                } else {
                    throw new UnauthorizedOperationException(PAYMENT_ERROR);
                }
            }

            restaurant.addOrderListWhoNeedToBePrepareBeforeDeadline(items, deliveryDate);
            this.orders.add(bufferOrder);
            customer.addOrder(bufferOrder);
            NotificationService.sendNotificationToUser(UserLevel.USER, "Your buffet order n°" + bufferOrder.getId() + " has been created !", customer.getFullName());

            return true;
        }
        return false;
    }

    public boolean createAfterWorkOrder(CustomerAccount customer, List<OrderItem> items, DateTime deliveryDate, String deliveryAddress, int numberOfParticipant) throws UnauthorizedOperationException {
        if (customer != null && deliveryDate != null && deliveryAddress != null) {
            Restaurant restaurant = items.get(0).getRestaurant();
            OrderAbstract order = new OrderBuilder(deliveryDate, customer)
                    .addMenuItems(items)
                    .setNumberOfParticipant(numberOfParticipant)
                    .setRestaurant(restaurant)
                    .build();
            order.setDeliveryAddress(deliveryAddress);
            for (OrderItem item : items) {
                if (item.getRestaurant() != restaurant) {
                    throw new UnauthorizedOperationException("You can't create an after work order with items from different restaurants.");
                }
                item.setStatus(IN_PREPARATION);
            }

            this.orders.add(order);
            customer.addOrder(order);
            restaurant.addAfterWorkOrder((AfterWorkOrder) order);
            NotificationService.sendNotificationToUser(UserLevel.USER, "Your after work order n°" + order.getId() + " has been created !\n It'll take place at " + deliveryAddress + " on " + deliveryDate + ".", customer.getFullName());
            return true;
        }
        return false;
    }

    public GroupOrder getGroupOrderById(int id) {
        return orders.stream()
                .filter(GroupOrder.class::isInstance)
                .map(GroupOrder.class::cast)
                .filter(groupOrder -> groupOrder.getId() == id)
                .findFirst()
                .orElse(null);
    }

    public boolean addOrderToGroupOrder(CustomerAccount customer, List<OrderItem> items, OrderAbstract groupOrder) throws UnauthorizedOperationException {
        if (List.of(IN_DELIVERY, FINISH, CANCELED).contains(groupOrder.getStatus())) {
            throw new UnauthorizedOperationException(ORDER_ERROR);
        }
        if (!groupOrder.allowSubOrder()) {
            throw new UnauthorizedOperationException("You tried to add an order to a type of order who doesn't allow that.");
        }

        OrderAbstract order = new OrderBuilder(groupOrder.getDeliveryDate(), customer)
                .setDeliveryAddress(groupOrder.getDeliveryAddress())
                .addMenuItems(items)
                .build();

        // If the order contain item that need to be prepared by the restaurant, then we should check the schedule.
        if (!items.isEmpty()) {
            Map<Restaurant, List<OrderItem>> restaurantItems = order.getItems().stream()
                    .collect(Collectors.groupingBy(OrderItem::getRestaurant, HashMap::new, Collectors.toList()));

            // Check if they all can be handled by the restaurant before the deliveryDate, else they all are refused
            for (Map.Entry<Restaurant, List<OrderItem>> entry : restaurantItems.entrySet()) {
                if (!entry.getKey().canOrderBePreparedBeforeDeadline(entry.getValue(), new DateTime(groupOrder.getDeliveryDate()))) {
                    return false;
                }
            }
            if (!paymentService.pay(order.getTotalPrice())) {
                throw new UnauthorizedOperationException(PAYMENT_ERROR);
            }

            order.getItems().forEach(item -> item.setStatus(WAITING_RESTAURANT_ACCEPTANCE));

            for (Map.Entry<Restaurant, List<OrderItem>> entry : restaurantItems.entrySet()) {
                entry.getKey().addOrderListWhoNeedToBePrepareBeforeDeadline(entry.getValue(), new DateTime(groupOrder.getDeliveryDate()));
            }
        }

        if (groupOrder.addSubOrder(order)) {
            customer.addOrder(order);
            this.orders.add(order);
            NotificationService.sendNotificationToUser(UserLevel.USER, "Your single order n°" + order.getId() + " has been added to the group order n°" + groupOrder.getId() + " !", customer.getFullName());
            return true;
        }

        return false;
    }

    public Boolean joinOrder(OrderAbstract parentOrder, OrderAbstract joiningOrder) throws UnauthorizedOperationException {
        if (List.of(IN_DELIVERY, FINISH, CANCELED).contains(parentOrder.getStatus())) {
            throw new UnauthorizedOperationException(ORDER_ERROR);
        }
        if (!List.of(WAITING_DELIVER_ACCEPTANCE, WAITING_RESTAURANT_ACCEPTANCE).contains(joiningOrder.getStatus())) {
            throw new UnauthorizedOperationException("The single order you want to join doesn't have the right status to join this order.");
        }

        OrderAbstract parentGroupOrder;
        joiningOrder.setDeliveryDate(parentOrder.getDeliveryDate());
        joiningOrder.setDeliveryAddress(parentOrder.getDeliveryAddress());

        if (!parentOrder.allowSubOrder() && joiningOrder.canBeSubOrder()) {
            parentGroupOrder = new OrderBuilder(parentOrder.getDeliveryDate(), parentOrder.getCustomer())
                    .setDeliveryAddress(parentOrder.getDeliveryAddress())
                    .build();
            parentGroupOrder.addSubOrder(parentOrder);
            this.orders.add(parentGroupOrder);
        } else {
            parentGroupOrder = parentOrder;
        }

        parentGroupOrder.assignDeliveryMan(parentOrder.getDeliveryMan());

        parentGroupOrder.addSubOrder(joiningOrder);
        joiningOrder.setParentGroupOwner(parentGroupOrder);
        NotificationService.sendNotificationToUser(UserLevel.USER, "Your order n°" + joiningOrder.getId() + " has been added to the group order n°" + parentGroupOrder.getId() + " !", joiningOrder.getCustomer().getFullName());
        return true;
    }

    public List<OrderAbstract> getOrderReadyToDeliver() {
        return orders.stream()
                .filter(order -> !order.isPartOfGroupOrder() && order.needToBeDelivered())
                .filter(order -> order.getStatus().equals(IN_PREPARATION) || order.getStatus().equals(WAITING_DELIVER_ACCEPTANCE))
                .toList();

    }

    public List<OrderAbstract> fetchAllOrdersByCustomer(CustomerAccount customer) {
        return orders.stream()
                .filter(orderAbstract -> orderAbstract.getCustomer().equals(customer))
                .toList();
    }


    public List<CustomerAccount> fetchCustomersInOrder(GroupOrder groupOrder) {
        return groupOrder.getSubOrders().stream().map(OrderAbstract::getCustomer).toList();
    }

    public boolean updateOrderItems(SingleOrder order, List<OrderItem> newItems) throws UnauthorizedModificationException {
        if (!order.statusAllowsUpdate()) {
            throw new UnauthorizedModificationException("You can't update this order.");
        }
        return order.updateItems(newItems);
    }

    public boolean updateOrderDeliveryDate(OrderAbstract order, DateTime newDeliveryDate) throws UnauthorizedModificationException {
        if (!order.statusAllowsUpdate()) {
            throw new UnauthorizedModificationException("You can't update the delivery date of this order.");
        }
        return order.updateDeliveryDate(newDeliveryDate);
    }

    public boolean updateOrderDeliveryAddress(OrderAbstract order, String newDeliveryAddress) throws UnauthorizedModificationException {
        if (!order.statusAllowsUpdate()) {
            throw new UnauthorizedModificationException("You can't update this order.");
        }
        return order.updateDeliveryAddress(newDeliveryAddress);
    }

    public boolean cancelOrder(OrderAbstract order) throws UnauthorizedModificationException {
        if (!order.statusAllowsUpdate()) {
            throw new UnauthorizedModificationException("You can't cancel this order.");
        }
        if (order.cancel()) {
            if (order.needToBePaid()) {
                if (!paymentService.refund(order.getTotalPrice())) {
                    throw new UnauthorizedModificationException("Refund failed");
                }
                NotificationService.sendNotificationToUser(UserLevel.USER, "Your order n°" + order.getId() + " has been canceled ! You have been refunded of " + order.getTotalPrice() + "€", order.getCustomer().getFullName());
            } else {
                NotificationService.sendNotificationToUser(UserLevel.USER, "Your order n°" + order.getId() + " has been canceled !", order.getCustomer().getFullName());
            }
            return true;
        }
        return false;
    }

    public OrderAbstract findOrderById(int id) {
        return orders.stream()
                .filter(orderAbstract -> orderAbstract.getId() == id)
                .findFirst()
                .orElse(null);
    }
}
