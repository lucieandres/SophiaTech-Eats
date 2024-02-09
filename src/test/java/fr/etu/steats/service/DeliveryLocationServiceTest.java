package fr.etu.steats.service;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class DeliveryLocationServiceTest {
    private DeliveryLocationService deliveryLocationService;

    @Before
    public void setUp() {
        deliveryLocationService = new DeliveryLocationService();
    }

    @Test
    public void testIsDeliveryLocation() {
        assertTrue(deliveryLocationService.isDeliveryLocation("Polytech, 06410 Biot, France"));
        assertFalse(deliveryLocationService.isDeliveryLocation("NonExistentLocation"));
    }

    @Test
    public void testGetDeliveryLocations() {
        assertEquals(6, deliveryLocationService.getDeliveryLocations().size());
    }

    @Test
    public void testAddDeliveryLocation() {
        int initialSize = deliveryLocationService.getDeliveryLocations().size();
        assertTrue(deliveryLocationService.addDeliveryLocation("NewLocation"));
        assertEquals(initialSize + 1, deliveryLocationService.getDeliveryLocations().size());
    }

    @Test
    public void testAddNullDeliveryLocation() {
        assertThrows(IllegalArgumentException.class, () -> deliveryLocationService.addDeliveryLocation(null));
    }

    @Test
    public void testRemoveNullDeliveryLocation() {
        assertThrows(IllegalArgumentException.class, () -> deliveryLocationService.removeDeliveryLocation(null));
    }

    @Test
    public void testAddExistingDeliveryLocation() {
        int initialSize = deliveryLocationService.getDeliveryLocations().size();
        assertFalse(deliveryLocationService.addDeliveryLocation("Polytech, 06410 Biot, France"));
        assertEquals(initialSize, deliveryLocationService.getDeliveryLocations().size());
    }

    @Test
    public void testRemoveDeliveryLocation() {
        int initialSize = deliveryLocationService.getDeliveryLocations().size();
        assertTrue(deliveryLocationService.removeDeliveryLocation("Polytech, 06410 Biot, France"));
        assertEquals(initialSize - 1, deliveryLocationService.getDeliveryLocations().size());
    }

    @Test
    public void testEditDeliveryLocation() {
        assertTrue(deliveryLocationService.editDeliveryLocation("Polytech, 06410 Biot, France", "NewLocation"));
        assertFalse(deliveryLocationService.isDeliveryLocation("Polytech, 06410 Biot, France"));
        assertTrue(deliveryLocationService.isDeliveryLocation("NewLocation"));
    }
}
