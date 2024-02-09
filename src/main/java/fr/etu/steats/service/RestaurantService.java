package fr.etu.steats.service;

import fr.etu.steats.exception.BadPasswordException;
import fr.etu.steats.exception.NonExistantRestaurantException;
import fr.etu.steats.restaurant.Menu;
import fr.etu.steats.restaurant.Restaurant;
import fr.etu.steats.utils.LoggerUtils;
import fr.etu.steats.utils.Scheduler;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;

public class RestaurantService {
    private static final String RESTAURANT_FILE_PATH = "src/main/resources/restaurants.csv";
    private static final AtomicInteger ID_FACTORY = new AtomicInteger(0);
    private final Set<Restaurant> restaurants;

    public RestaurantService() {
        this((Scheduler) null);
    }

    public RestaurantService(Scheduler scheduler) {
        // Load restaurants and menus from CSV file
        ID_FACTORY.set(0);
        this.restaurants = new HashSet<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(RESTAURANT_FILE_PATH))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");

                if (parts.length != 4) {
                    LoggerUtils.log(Level.SEVERE, "Badly formatted CSV line: " + line + "\nExpected format: name,menu,password,address");
                    continue;
                }

                Restaurant restaurant;
                if (scheduler != null) {
                    restaurant = this.addRestaurant(parts[0], parts[2], scheduler, parts[3]);
                } else {
                    restaurant = this.addRestaurant(parts[0], parts[2], parts[3]);
                }

                String[] menuItems = parts[1].split(";");
                for (String item : menuItems) {
                    String[] itemParts = item.split(":");

                    if (itemParts.length != 2) {
                        LoggerUtils.log(Level.SEVERE, "Badly formatted menu item: " + line + "\nExpected format: name:price");
                        continue;
                    }
                    Menu menuItem = new Menu(itemParts[0], Double.parseDouble(itemParts[1].trim())); // trim() to remove any spaces
                    restaurant.addMenuItem(menuItem);
                }
            }
        } catch (NumberFormatException e) {
            LoggerUtils.log(Level.SEVERE, "Error converting the price for the item ");
        } catch (FileNotFoundException e) {
            LoggerUtils.log(Level.SEVERE, "The file restaurants.csv was not found.");
        } catch (IOException e) {
            LoggerUtils.log(Level.SEVERE, "Error reading the file restaurants.csv: " + e.getMessage());
        } catch (Exception e) {
            LoggerUtils.log(Level.SEVERE, "Unexpected error: " + e.getMessage());
        }
    }

    public RestaurantService(Set<Restaurant> restaurants) {
        if (restaurants == null) {
            throw new IllegalArgumentException("restaurants cannot be null");
        }
        this.restaurants = restaurants;
        ID_FACTORY.set(0);
    }

    public List<Menu> viewMenu(int restaurantId) {
        Restaurant restaurant = findRestaurantById(restaurantId);
        if (restaurant != null) {
            return restaurant.getMenuItems();
        } else {
            throw new IllegalArgumentException("Restaurant with ID " + restaurantId + " not found.");
        }
    }

    public Restaurant findRestaurantById(int restaurantId) {
        return restaurants.stream().filter(r -> r.getId() == restaurantId).findFirst().orElse(null);
    }

    public Set<Restaurant> getRestaurants() {
        return restaurants;
    }

    public Restaurant addRestaurant(String name, String password, String address) {
        return this.addRestaurant(name, password, new Scheduler(), address);
    }

    public Restaurant addRestaurant(String name, String password, Scheduler scheduler, String address) {
        if (name == null || name.isEmpty() || password == null || password.isEmpty()) {
            throw new IllegalArgumentException("The name and the password can't be null or empty for a restaurant...");
        }
        Restaurant restaurant = new Restaurant(name, ID_FACTORY.incrementAndGet(), password, scheduler, address);
        if (restaurants.contains(restaurant)) {
            throw new IllegalArgumentException("The restaurant " + name + " already exists...");
        }
        restaurants.stream().filter(r -> r.getName().equals(name.trim().toLowerCase())).findFirst().ifPresent(r -> {
            throw new IllegalArgumentException("The restaurant " + name + " already exists...");
        });
        if (restaurants.add(restaurant)) {
            return restaurant;
        }
        return null;
    }

    public boolean removeRestaurant(int id) {
        Restaurant restaurant = this.findRestaurantById(id);
        if (restaurant == null) {
            return false;
        }
        return restaurants.remove(restaurant);
    }

    public Restaurant login(String name, String password) throws NonExistantRestaurantException, BadPasswordException {
        if (name == null || name.isEmpty() || password == null || password.isEmpty()) {
            throw new IllegalArgumentException("The firstname, the lastname and the password can't be null or empty for a login...");
        }
        name = name.toLowerCase().trim();

        for (Restaurant restaurant : restaurants) {
            if (restaurant.getName().equals(name) && (restaurant.checkPassword(password))) {
                return restaurant;
            }
        }
        throw new NonExistantRestaurantException("We don't have any restaurant registered in our system named " + name + ".\nIf that's not the case please use the register feature before trying to login.");
    }

}
