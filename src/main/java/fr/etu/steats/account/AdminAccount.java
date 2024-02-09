package fr.etu.steats.account;

/**
 * This class represents an admin account
 * An admin account can manage restaurants and delivery accounts and can see statistics
 */
public class AdminAccount extends AccountAbstract {

    public AdminAccount(String firstName, String lastName, String password) {
        super(firstName, lastName, password);
    }
}