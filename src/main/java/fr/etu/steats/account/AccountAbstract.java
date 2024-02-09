package fr.etu.steats.account;

import fr.etu.steats.exception.BadPasswordException;

import java.util.Objects;

public abstract class AccountAbstract {
    private final String firstName;
    private final String lastName;
    private String password;

    protected AccountAbstract(String firstName, String lastName, String password) {
        if (firstName == null || lastName == null || password == null) {
            throw new IllegalArgumentException("The first name, the last name and the password can't be null");
        }
        if (firstName.isBlank() || lastName.isBlank() || password.isBlank()) {
            throw new IllegalArgumentException("The first name, the last name and the password can't be blank");
        }
        this.firstName = firstName.trim().toLowerCase();
        this.lastName = lastName.trim().toLowerCase();
        this.password = password;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    /**
     * Return the full name of the account
     * Format: FirstName LASTNAME
     */
    public String getFullName() {
        return this.firstName.substring(0, 1).toUpperCase() + this.firstName.substring(1) + " " + this.lastName.toUpperCase();
    }

    public String getPassword() {
        return password;
    }

    public boolean checkPassword(String password) throws BadPasswordException {
        if (password == null || password.isBlank()) {
            throw new BadPasswordException("The password can't be null or blank");
        }
        if (!this.password.equals(password)) {
            throw new BadPasswordException("The password you provided is incorrect, please retry. \nIf you forgot your password, contact an administrator to change your it.");
        }
        return true;
    }

    public void setPassword(String password) {
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("The password can't be null or blank");
        }
        this.password = password;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AccountAbstract accountAbstract = (AccountAbstract) o;

        if (!firstName.equals(accountAbstract.firstName)) return false;
        if (!lastName.equals(accountAbstract.lastName)) return false;
        return Objects.equals(password, accountAbstract.password);
    }

    @Override
    public int hashCode() {
        int result = firstName.hashCode();
        result = 31 * result + lastName.hashCode();
        result = 31 * result + (password != null ? password.hashCode() : 0);
        return result;
    }
}
