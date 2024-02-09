package fr.etu.steats.service;

import fr.etu.steats.account.CustomerAccount;
import fr.etu.steats.exception.UnauthorizedOperationException;
import fr.etu.steats.order.OrderAbstract;
import fr.etu.steats.order.OrderItem;
import fr.etu.steats.restaurant.Restaurant;
import org.joda.time.DateTime;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

public class DiscountService {
    private static final Map<CustomerAccount, Map<Restaurant, DateTime>> discounts = new HashMap<>();

    private DiscountService(){
        // Empty constructor
    }

    public static double computePriceAfterDiscount(OrderAbstract orderAbstract) {
        double price = 0;

        CustomerAccount customerAccount = orderAbstract.getCustomer();

        Map<Restaurant, List<OrderItem>> restaurantOrderItemMap = orderAbstract.getItems().stream()
                .collect(Collectors.groupingBy(OrderItem::getRestaurant));

        for (Entry<Restaurant, List<OrderItem>> entry : restaurantOrderItemMap.entrySet()) {
            for (OrderItem item : entry.getValue()) {
                //Apply the discount by type of customer if we have one
                double temporaryPrice = item.getPrice(customerAccount.getType());
                //Apply the cumulated order discount if we have one
                if (isUnderCumulatedOrderDiscount(customerAccount, entry.getKey())) {
                    temporaryPrice = temporaryPrice * 0.95;
                }
                price += temporaryPrice;
            }
        }

        return price;
    }

    public static boolean isUnderCumulatedOrderDiscount(CustomerAccount customerAccount, Restaurant restaurant) {
        List<OrderAbstract> history = customerAccount.getPreviousOrder();

        //If we already have a discount on going that is still valid for a cumulated number of order at the same restaurant, then we return true
        if (discounts.containsKey(customerAccount) && discounts.get(customerAccount).containsKey(restaurant)) {
            DateTime finishingDate = discounts.get(customerAccount).get(restaurant);
            if (finishingDate.isAfterNow()) {
                return true;
            }
            discounts.get(customerAccount).remove(restaurant);
        }

        //Else if we have 10 order at the same restaurant in the last 15 days, then we create a discount and return true
        DateTime oldestOrderDateForDiscount = DateTime.now().minusDays(15);
        List<OrderAbstract> numberOfOrderInDateRange = history.stream().filter(
                order -> !order.getItems().stream().filter(item -> item.getRestaurant() == restaurant).toList().isEmpty() && order.getDeliveryDate().isAfter(oldestOrderDateForDiscount)
        ).toList();


        if (numberOfOrderInDateRange.size() >= 10) {
            if (discounts.containsKey(customerAccount)) {
                discounts.get(customerAccount).put(restaurant, DateTime.now().plusDays(15));
            } else {
                discounts.put(customerAccount, Map.of(restaurant, DateTime.now().plusDays(15)));
            }

            return true;
        }

        return false;
    }

    public static boolean computeCustomerCredit(List<OrderAbstract> orders, int minNumberOfOrder, double discountRate) throws UnauthorizedOperationException {
        if (minNumberOfOrder < 0) {
            throw new UnauthorizedOperationException("Minimum number of orders is a positive number.");
        }
        if (discountRate < 0 || discountRate > 1) {
            throw new UnauthorizedOperationException("Discount is a percentage between 0 and 1.");
        }

        if (orders.stream().mapToInt(order -> order.getItems().size()).sum() >= minNumberOfOrder) {
            for (OrderAbstract order : orders) {
                order.getCustomer().addCredit(order.getNonReducedTotalPrice() * discountRate);
            }
            return true;
        }
        return false;
    }
}
