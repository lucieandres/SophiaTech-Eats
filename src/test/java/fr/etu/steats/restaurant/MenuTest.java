package fr.etu.steats.restaurant;

import fr.etu.steats.enums.ECustomerStatus;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MenuTest {

    @Test
    void testConstructorAndGetters() {
        Menu menu = new Menu("Burger", 10.0);
        assertEquals("Burger", menu.getName());
        assertEquals(10.0, menu.getGlobalPrice(), 0.01); // Utilisation de 0.01 pour gérer les arrondis
    }

    @Test
    void testSetName() {
        Menu menu = new Menu("Pizza", 12.5);
        menu.setName("Spaghetti");
        assertEquals("Spaghetti", menu.getName());
    }

    @Test
    void testSetPrice() {
        Menu menu = new Menu("Salad", 5.5);
        menu.setGlobalPrice(6.0);
        assertEquals(6.0, menu.getGlobalPrice(), 0.01);

        assertThrows(IllegalArgumentException.class, () -> menu.setGlobalPrice(-1.0));
    }

    @Test
    void testConstructorWithInvalidArguments() {
        assertThrows(IllegalArgumentException.class, () -> new Menu(null, 10.0));

        assertThrows(IllegalArgumentException.class, () -> new Menu("", 10.0));

        assertThrows(IllegalArgumentException.class, () -> new Menu("Hot Dog", -2.0));
    }

    @Test
    void testGetPriceWithDifferentCustomerStatus() {
        Menu menu = new Menu("Burger", 10.0);
        menu.setStudentPrice(8.0);
        menu.setStaffPrice(9.0);

        assertEquals(8.0, menu.getPrice(ECustomerStatus.STUDENT));
        assertEquals(9.0, menu.getPrice(ECustomerStatus.STAFF));
        assertEquals(10.0, menu.getPrice(ECustomerStatus.FACULTY));
    }

    @Test
    void testIsAfterWork() {
        Menu menu = new Menu("Burger", 10.0, false);
        assertFalse(menu.isAfterWork());
    }

    @Test
    void testSetAfterWork() {
        Menu menu = new Menu("Burger", 10.0);
        menu.setAfterWork(false);
        assertFalse(menu.isAfterWork());
    }
}
