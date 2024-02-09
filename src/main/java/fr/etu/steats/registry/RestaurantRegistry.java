package fr.etu.steats.registry;

import fr.etu.steats.exception.BadPasswordException;
import fr.etu.steats.exception.NonExistantRestaurantException;
import fr.etu.steats.exception.UnauthorizedOperationException;
import fr.etu.steats.restaurant.Menu;
import fr.etu.steats.restaurant.Restaurant;
import fr.etu.steats.service.RestaurantService;
import fr.etu.steats.utils.LoggerUtils;

import java.util.logging.Level;

public class RestaurantRegistry {
    private Restaurant restaurant;
    private final RestaurantService restaurantService;

    public RestaurantRegistry(RestaurantService restaurantService) {
        this.restaurantService = restaurantService;
    }

    public Restaurant getRestaurant() {
        return this.restaurant;
    }

    public RestaurantService getRestaurantService() {
        return this.restaurantService;
    }

    public void restaurantLogin(String name, String password) {
        try {
            this.restaurant = this.restaurantService.login(name, password);
        } catch (NonExistantRestaurantException | BadPasswordException e) {
            LoggerUtils.log(Level.SEVERE, e.getMessage());
        }
    }

    public void editExistingMenuItem(String oldMenuName, String newMenuName, int newPrice) throws UnauthorizedOperationException {
        this.verifyRestaurant();
        this.restaurant.updateMenuItem(oldMenuName, new Menu(newMenuName, newPrice));
    }

    public void addMenuItem(String menuName, int price) throws UnauthorizedOperationException {
        this.verifyRestaurant();
        this.restaurant.addMenuItem(new Menu(menuName, price));
    }

    public void removeMenuItem(String menuName) throws UnauthorizedOperationException {
        this.verifyRestaurant();
        this.restaurant.removeMenuItem(menuName);
    }

    private void verifyRestaurant() throws UnauthorizedOperationException {
        if (this.restaurant == null) {
            throw new UnauthorizedOperationException("You need to be registered as a restaurant to update your menu. Please retry after a login.");
        }
    }
}
