package fr.etu.steats.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PaymentServiceTest {

    @Test
    void payTest() {
        PaymentService paymentService = new PaymentService();
        assertTrue(paymentService.pay(100));
        assertFalse(paymentService.pay(-100));
    }

    @Test
    void refundTest() {
        PaymentService paymentService = new PaymentService();
        assertTrue(paymentService.refund(100));
        assertFalse(paymentService.refund(-100));
    }
}