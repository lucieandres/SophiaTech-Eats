package fr.etu.steats.account;

import fr.etu.steats.enums.ECustomerStatus;
import fr.etu.steats.enums.EOrderStatus;
import fr.etu.steats.order.OrderAbstract;

import java.util.ArrayList;
import java.util.List;

public class CustomerAccount extends AccountAbstract {
    private final List<OrderAbstract> orders;
    private double credit;
    private ECustomerStatus type;

    public CustomerAccount(String firstName, String lastName, String password) {
        this(firstName, lastName, password, ECustomerStatus.EXTERNAL);
    }

    public CustomerAccount(String firstName, String lastName, String password, ECustomerStatus type) {
        super(firstName, lastName, password);

        this.orders = new ArrayList<>();
        this.credit = 0;
        this.type = type;
    }

    public List<OrderAbstract> getOrders() {
        return this.orders;
    }

    public boolean addOrder(OrderAbstract order) {
        if (order == null) {
            throw new IllegalArgumentException("The order can't be null");
        }
        return this.orders.add(order);
    }

    public double getCredit() {
        return credit;
    }

    public void setCredit(double credit) {
        if (credit < 0) {
            throw new IllegalArgumentException("Credit must be positive");
        }
        this.credit = credit;
    }

    public void addCredit(double credit) {
        if (credit < 0) {
            throw new IllegalArgumentException("Credit must be positive");
        }
        this.credit += credit;
    }

    public ECustomerStatus getType() {
        return this.type;
    }

    public void setType(ECustomerStatus type) {
        if (type != null) {
            this.type = type;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        CustomerAccount otherAccount = (CustomerAccount) o;

        if (Double.compare(credit, otherAccount.credit) != 0) return false;
        return orders.equals(otherAccount.orders);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        long temp;
        result = 31 * result + (orders != null ? orders.hashCode() : 0);
        temp = Double.doubleToLongBits(credit);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    public List<OrderAbstract> getPreviousOrder() {
        return this.orders.stream().filter(order -> order.getStatus() == EOrderStatus.FINISH).toList();
    }
}
