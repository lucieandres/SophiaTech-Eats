package fr.etu.steats.account;

import fr.etu.steats.exception.BadPasswordException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class AccountAbstractTest {
    private static Stream<Arguments> provideAllAccountTypes() {
        return Stream.of(
                Arguments.of(new CustomerAccount("John", "Doe", "password123"), "John", "Doe", "password123"),
                Arguments.of(new AdminAccount("admin", "admin", "admin"), "admin", "admin", "admin"),
                Arguments.of(new DeliveryAccount("Bob", "Delivery", "password123"), "Bob", "Delivery", "password123")

        );
    }

    @ParameterizedTest
    @MethodSource("provideAllAccountTypes")
    void testConstructor(AccountAbstract account, String firstName, String lastName, String password) {
        assertEquals(firstName.toLowerCase(), account.getFirstName());
        assertEquals(lastName.toLowerCase(), account.getLastName());
        assertEquals(password.toLowerCase(), account.getPassword());
        assertEquals(firstName.toLowerCase().substring(0, 1).toUpperCase() + firstName.toLowerCase().substring(1) + " " + lastName.toUpperCase(), account.getFullName());
    }

    @ParameterizedTest
    @MethodSource("provideAllAccountTypes")
    void testCheckPassword(AccountAbstract account, String firstName, String lastName, String password) throws BadPasswordException {
        assertTrue(account.checkPassword(password));
    }

    @ParameterizedTest
    @MethodSource("provideAllAccountTypes")
    void testCheckPasswordWithBlankPassword(AccountAbstract account) {
        assertThrows(BadPasswordException.class, () -> account.checkPassword(" "));
    }

    @ParameterizedTest
    @MethodSource("provideAllAccountTypes")
    void testCheckPasswordWithNullPassword(AccountAbstract account) {
        assertThrows(BadPasswordException.class, () -> account.checkPassword(null));
    }

    @ParameterizedTest
    @MethodSource("provideAllAccountTypes")
    void testSetPassword(AccountAbstract account) {
        account.setPassword("newPassword");
        assertEquals("newPassword", account.getPassword());
    }

    @ParameterizedTest
    @MethodSource("provideAllAccountTypes")
    void testSetPasswordWithBlankPassword(AccountAbstract account) {
        assertThrows(IllegalArgumentException.class, () -> account.setPassword(" "));
    }

    @ParameterizedTest
    @MethodSource("provideAllAccountTypes")
    void testSetPasswordWithNullPassword(AccountAbstract account) {
        assertThrows(IllegalArgumentException.class, () -> account.setPassword(null));
    }

    @Test
    void testConstructorWithNullValues() {
        assertThrows(IllegalArgumentException.class, () -> new CustomerAccount(null, "Doe", "password123"));
        assertThrows(IllegalArgumentException.class, () -> new CustomerAccount("John", null, "password123"));
        assertThrows(IllegalArgumentException.class, () -> new CustomerAccount("John", "Doe", null));
    }

    @Test
    void testConstructorWithBlankValues() {
        assertThrows(IllegalArgumentException.class, () -> new CustomerAccount("", "Doe", "password123"));
        assertThrows(IllegalArgumentException.class, () -> new CustomerAccount("John", "  ", "password123"));
        assertThrows(IllegalArgumentException.class, () -> new CustomerAccount("John", "Doe", ""));
    }

    @Test
    void testEquals_SameValues() {
        assertEquals(new CustomerAccount("John", "Doe", "password123"), new CustomerAccount("John", "Doe", "password123"));
    }

    @Test
    void testEquals_DifferentValues() {
        assertNotEquals(new CustomerAccount("John", "Doe", "password123"), new CustomerAccount("Bob", "Delivery", "password123"));
    }
}