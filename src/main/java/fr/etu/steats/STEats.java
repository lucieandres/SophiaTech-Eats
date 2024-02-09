package fr.etu.steats;

import fr.etu.steats.registry.AdminRegistry;
import fr.etu.steats.registry.CustomerRegistry;
import fr.etu.steats.registry.DeliveryRegistry;
import fr.etu.steats.registry.RestaurantRegistry;
import fr.etu.steats.service.*;
import fr.etu.steats.utils.Scheduler;

public class STEats {
    private final CustomerRegistry customerRegistry;
    private final DeliveryRegistry deliveryRegistry;
    private final AdminRegistry adminRegistry;
    private final RestaurantRegistry restaurantRegistry;

    public STEats() {
        this(new PaymentService(), new Scheduler());
    }

    public STEats(Scheduler scheduler) {
        this(new PaymentService(), scheduler);
    }

    public STEats(PaymentService paymentService) {
        this(paymentService, new Scheduler());
    }

    public STEats(PaymentService paymentService, Scheduler scheduler) {
        RestaurantService restaurantService = new RestaurantService(scheduler);
        OrderService orderService = new OrderService(paymentService);
        RegistrationService registrationService = new RegistrationService(scheduler);
        DeliveryLocationService deliveryLocationService = new DeliveryLocationService();

        this.customerRegistry = new CustomerRegistry(orderService, registrationService, deliveryLocationService, restaurantService);
        this.deliveryRegistry = new DeliveryRegistry(registrationService, orderService);
        this.adminRegistry = new AdminRegistry(deliveryLocationService, registrationService, restaurantService, orderService);
        this.restaurantRegistry = new RestaurantRegistry(restaurantService);
    }

    public CustomerRegistry getCustomerRegistry() {
        return customerRegistry;
    }

    public DeliveryRegistry getDeliveryRegistry() {
        return deliveryRegistry;
    }

    public AdminRegistry getAdminRegistry() {
        return adminRegistry;
    }

    public RestaurantRegistry getRestaurantRegistry() {
        return restaurantRegistry;
    }
}
