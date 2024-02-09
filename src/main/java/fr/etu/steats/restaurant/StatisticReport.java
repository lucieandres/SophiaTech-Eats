package fr.etu.steats.restaurant;

import fr.etu.steats.order.OrderItem;

import java.util.Date;
import java.util.Set;

/**
 * This class represents a statistic report.
 * It is used to generate a report of the restaurant's activity by the admin.
 */
public class StatisticReport {
    private final Date dateCreated;
    private int nbOrders;
    private int nbOrdersWaitingPayment;
    private int nbOrdersWaitingRestaurantAcceptance;
    private int nbOrdersInPreparation;
    private int nbOrdersWaitingDeliverAcceptance;
    private int nbOrdersInDelivery;
    private int nbOrdersFinish;
    private int nbOrdersCancelled;
    private int nbRestaurants;

    public StatisticReport(Set<Restaurant> restaurants) {
        this.dateCreated = new Date();
        this.generateReport(restaurants);
    }

    public StatisticReport(Restaurant restaurant) {
        this.dateCreated = new Date();
        this.generateReport(Set.of(restaurant));
    }

    private void generateReport(Set<Restaurant> restaurants) {
        this.nbRestaurants = restaurants.size();
        for (Restaurant restaurant : restaurants) {
            this.nbOrders += restaurant.getOrders().size();
            for (OrderItem orderItem : restaurant.getOrders()) {
                switch (orderItem.getStatus()) {
                    case WAITING_PAYMENT:
                        this.nbOrdersWaitingPayment++;
                        break;
                    case WAITING_RESTAURANT_ACCEPTANCE:
                        this.nbOrdersWaitingRestaurantAcceptance++;
                        break;
                    case IN_PREPARATION:
                        this.nbOrdersInPreparation++;
                        break;
                    case WAITING_DELIVER_ACCEPTANCE:
                        this.nbOrdersWaitingDeliverAcceptance++;
                        break;
                    case IN_DELIVERY:
                        this.nbOrdersInDelivery++;
                        break;
                    case FINISH:
                        this.nbOrdersFinish++;
                        break;
                    case CANCELED:
                        this.nbOrdersCancelled++;
                        break;
                }
            }
        }
    }

    public Date getDateCreated() {
        return dateCreated;
    }

    public int getNbOrders() {
        return nbOrders;
    }

    public int getNbOrdersWaitingPayment() {
        return nbOrdersWaitingPayment;
    }

    public int getNbOrdersWaitingRestaurantAcceptance() {
        return nbOrdersWaitingRestaurantAcceptance;
    }

    public int getNbOrdersInPreparation() {
        return nbOrdersInPreparation;
    }

    public int getNbOrdersWaitingDeliverAcceptance() {
        return nbOrdersWaitingDeliverAcceptance;
    }

    public int getNbOrdersInDelivery() {
        return nbOrdersInDelivery;
    }

    public int getNbOrdersFinish() {
        return nbOrdersFinish;
    }

    public int getNbOrdersCancelled() {
        return nbOrdersCancelled;
    }

    public int getNbRestaurants() {
        return nbRestaurants;
    }

    @Override
    public String toString() {
        return "StatisticReport{" +
                "dateCreated=" + dateCreated +
                ", nbOrders=" + nbOrders +
                ", nbOrdersWaitingPayment=" + nbOrdersWaitingPayment +
                ", nbOrdersWaitingRestaurantAcceptance=" + nbOrdersWaitingRestaurantAcceptance +
                ", nbOrdersInPreparation=" + nbOrdersInPreparation +
                ", nbOrdersWaitingDeliverAcceptance=" + nbOrdersWaitingDeliverAcceptance +
                ", nbOrdersInDelivery=" + nbOrdersInDelivery +
                ", nbOrdersFinish=" + nbOrdersFinish +
                ", nbOrdersCancelled=" + nbOrdersCancelled +
                ", nbRestaurants=" + nbRestaurants +
                '}';
    }
}
