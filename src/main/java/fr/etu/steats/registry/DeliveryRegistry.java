package fr.etu.steats.registry;

import fr.etu.steats.account.DeliveryAccount;
import fr.etu.steats.exception.BadPasswordException;
import fr.etu.steats.exception.NoAccountFoundException;
import fr.etu.steats.exception.UnauthorizedOperationException;
import fr.etu.steats.order.OrderAbstract;
import fr.etu.steats.service.OrderService;
import fr.etu.steats.service.RegistrationService;

import java.util.List;

public class DeliveryRegistry {
    private DeliveryAccount deliveryAccount;
    private final RegistrationService registrationService;
    private final OrderService orderService;

    public DeliveryRegistry(RegistrationService registrationService, OrderService orderService) {
        this.registrationService = registrationService;
        this.orderService = orderService;
    }

    public DeliveryAccount getDeliveryAccount() {
        return this.deliveryAccount;
    }

    public RegistrationService getRegistrationService() {
        return registrationService;
    }

    public OrderService getOrderService() {
        return orderService;
    }

    public void deliveryLogin(String firstname, String lastname, String password) throws BadPasswordException, NoAccountFoundException {
        this.deliveryAccount = this.registrationService.loginDelivery(firstname, lastname, password);
    }

    public List<OrderAbstract> getOrderReadyToDeliver() throws UnauthorizedOperationException {
        if (this.deliveryAccount == null) {
            throw new UnauthorizedOperationException("A non registered/logged delivery man can't handle an order...");
        }

        return this.orderService.getOrderReadyToDeliver();
    }
}
