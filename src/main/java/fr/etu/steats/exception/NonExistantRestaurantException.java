package fr.etu.steats.exception;

public class NonExistantRestaurantException extends Exception {
    public NonExistantRestaurantException(String message) {
        super(message);
    }
}
