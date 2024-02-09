package fr.etu.steats.service;

import fr.etu.steats.utils.LoggerUtils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;

/**
 * This class represents a delivery location service.
 * Users must provide pre-identified delivery locations on campus
 */
public class DeliveryLocationService {
    private static final String DELIVERY_LOCATION_FILE_PATH = "src/main/resources/delivery_locations.csv";
    private final Set<String> deliveryLocations;

    public DeliveryLocationService() {
        this.deliveryLocations = new HashSet<>();
        this.loadDeliveryLocationsFromCSV();
    }

    private void loadDeliveryLocationsFromCSV() {
        try (BufferedReader br = new BufferedReader(new FileReader(DELIVERY_LOCATION_FILE_PATH))) {
            String line;
            while ((line = br.readLine()) != null) {
                this.deliveryLocations.add(line);
            }
        } catch (IOException e) {
            LoggerUtils.log(Level.SEVERE, "Error while reading the delivery locations CSV file: " + DELIVERY_LOCATION_FILE_PATH);
        }
    }

    public boolean isDeliveryLocation(String location) {
        return this.deliveryLocations.contains(location);
    }

    public Set<String> getDeliveryLocations() {
        return this.deliveryLocations;
    }

    public boolean addDeliveryLocation(String location) {
        if (location == null) {
            throw new IllegalArgumentException("Location cannot be null");
        }
        return this.deliveryLocations.add(location);
    }

    public boolean removeDeliveryLocation(String location) {
        if (location == null) {
            throw new IllegalArgumentException("Location cannot be null");
        }
        return this.deliveryLocations.remove(location);
    }

    public boolean editDeliveryLocation(String oldLocation, String newLocation) {
        if (oldLocation == null || newLocation == null) {
            throw new IllegalArgumentException("Locations cannot be null");
        }
        if (this.deliveryLocations.contains(oldLocation)) {
            this.deliveryLocations.remove(oldLocation);
            this.deliveryLocations.add(newLocation);
            return true;
        }
        return false;
    }
}
