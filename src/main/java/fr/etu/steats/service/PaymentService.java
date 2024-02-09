package fr.etu.steats.service;

/**
 * This class is used to simulate a payment service.
 */
public class PaymentService {

    public PaymentService() {
        // Empty constructor because this class for simulate a payment service
    }

    public boolean pay(double totalPrice) {
        return totalPrice > 0;
    }

    public boolean refund(double totalPrice) {
        return totalPrice > 0;
    }
}