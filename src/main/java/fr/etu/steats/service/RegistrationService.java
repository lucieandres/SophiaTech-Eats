package fr.etu.steats.service;

import fr.etu.steats.account.AdminAccount;
import fr.etu.steats.account.CustomerAccount;
import fr.etu.steats.account.DeliveryAccount;
import fr.etu.steats.exception.AlreadyRegisteredUser;
import fr.etu.steats.exception.BadPasswordException;
import fr.etu.steats.exception.NoAccountFoundException;
import fr.etu.steats.utils.LoggerUtils;
import fr.etu.steats.utils.Scheduler;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

/**
 * This class is used to manage the registration of the different account type
 */
public class RegistrationService {
    private static final String CUSTOMER_FILE_PATH = "src/main/resources/customer.csv";
    private static final String DELIVERY_FILE_PATH = "src/main/resources/delivery.csv";
    private static final String ADMIN_FILE_PATH = "src/main/resources/admin.csv";
    private final Set<CustomerAccount> customerList;
    private final Set<DeliveryAccount> deliveryList;
    private final Set<AdminAccount> adminList;

    public RegistrationService() {
        this(new Scheduler());
    }

    public RegistrationService(Scheduler scheduler) {
        this.customerList = new HashSet<>();
        this.deliveryList = new HashSet<>();
        this.adminList = new HashSet<>();

        loadCustomerDataFromCSV();
        loadDeliveryDataFromCSV(scheduler);
        loadAdminDataFromCSV();
    }

    private void loadCustomerDataFromCSV() {
        try (BufferedReader br = new BufferedReader(new FileReader(CUSTOMER_FILE_PATH))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] content = line.split(",");
                this.customerList.add(new CustomerAccount(content[0], content[1], content[2]));
            }
        } catch (IOException e) {
            LoggerUtils.log(Level.SEVERE, "Error while reading the customer CSV file: " + CUSTOMER_FILE_PATH);
        }
    }

    private void loadDeliveryDataFromCSV(Scheduler scheduler) {
        try (BufferedReader br = new BufferedReader(new FileReader(DELIVERY_FILE_PATH))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] content = line.split(",");
                this.deliveryList.add(new DeliveryAccount(content[0], content[1], content[2], scheduler));
            }
        } catch (IOException e) {
            LoggerUtils.log(Level.SEVERE, "Error while reading the delivery CSV file: " + DELIVERY_FILE_PATH);
        }
    }

    private void loadAdminDataFromCSV() {
        try (BufferedReader br = new BufferedReader(new FileReader(ADMIN_FILE_PATH))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] content = line.split(",");
                this.adminList.add(new AdminAccount(content[0], content[1], content[2]));
            }
        } catch (IOException e) {
            LoggerUtils.log(Level.SEVERE, "Error while reading the admin CSV file: " + ADMIN_FILE_PATH);
        }
    }

    public List<CustomerAccount> getCustomerList() {
        return customerList.stream().toList();
    }

    /**
     * Alternative constructor mainly use for test purpose
     *
     * @param customerList the list of customer to use
     * @param deliveryList the list of delivery account to use
     * @param adminList    the list of admin account to use
     */
    protected RegistrationService(Set<CustomerAccount> customerList, Set<DeliveryAccount> deliveryList, Set<AdminAccount> adminList) {
        this.customerList = customerList;
        this.deliveryList = deliveryList;
        this.adminList = adminList;
    }

    public CustomerAccount loginCustomer(String firstname, String lastname, String password) throws NoAccountFoundException, BadPasswordException {
        checkEntryParameter(firstname, lastname, password);
        firstname = firstname.toLowerCase().trim();
        lastname = lastname.toLowerCase().trim();

        for (CustomerAccount customerAccount : this.customerList) {
            if (customerAccount.getFirstName().equals(firstname) && customerAccount.getLastName().equals(lastname) && (customerAccount.checkPassword(password))) {
                return customerAccount;
            }
        }
        throw new NoAccountFoundException("We don't have any customer registered in our system named " + firstname + " " + lastname + ".\nIf that's not the case please use the register feature before trying to login.");
    }

    public DeliveryAccount loginDelivery(String firstname, String lastname, String password) throws NoAccountFoundException, BadPasswordException {
        checkEntryParameter(firstname, lastname, password);
        firstname = firstname.toLowerCase().trim();
        lastname = lastname.toLowerCase().trim();

        for (DeliveryAccount deliveryAccount : this.deliveryList) {
            if (deliveryAccount.getFirstName().equals(firstname) && deliveryAccount.getLastName().equals(lastname) && (deliveryAccount.checkPassword(password))) {
                return deliveryAccount;
            }
        }
        throw new NoAccountFoundException("We don't have any delivery account registered in our system named " + firstname + " " + lastname + ".\nIf that's not the case please use the register feature before trying to login.");
    }

    public CustomerAccount registerCustomer(String firstname, String lastname, String password) throws AlreadyRegisteredUser {
        checkEntryParameter(firstname, lastname, password);
        firstname = firstname.toLowerCase().trim();
        lastname = lastname.toLowerCase().trim();

        for (CustomerAccount customerAccount : this.customerList) {
            if (customerAccount.getFirstName().equals(firstname) && customerAccount.getLastName().equals(lastname)) {
                throw new AlreadyRegisteredUser("You already have an account within our system, please use the login feature instead.\nIf you forgot your password, contact an administrator to change your it.");
            }
        }

        CustomerAccount newCustomer = new CustomerAccount(firstname, lastname, password);
        this.customerList.add(newCustomer);
        return newCustomer;
    }

    public DeliveryAccount registerDelivery(String firstname, String lastname, String password) throws AlreadyRegisteredUser {
        checkEntryParameter(firstname, lastname, password);
        firstname = firstname.toLowerCase().trim();
        lastname = lastname.toLowerCase().trim();

        for (DeliveryAccount deliveryAccount : this.deliveryList) {
            if (deliveryAccount.getFirstName().equals(firstname) && deliveryAccount.getLastName().equals(lastname)) {
                throw new AlreadyRegisteredUser("You already have an account within our system, please use the login feature instead.\nIf you forgot your password, contact an administrator to change your it.");
            }
        }

        DeliveryAccount newDeliveryMan = new DeliveryAccount(firstname, lastname, password);
        this.deliveryList.add(newDeliveryMan);
        return newDeliveryMan;
    }

    public AdminAccount loginAdmin(String firstname, String lastname, String password) throws NoAccountFoundException, BadPasswordException {
        checkEntryParameter(firstname, lastname, password);
        firstname = firstname.toLowerCase().trim();
        lastname = lastname.toLowerCase().trim();

        for (AdminAccount adminAccount : this.adminList) {
            if (adminAccount.getFirstName().equals(firstname) && adminAccount.getLastName().equals(lastname) && (adminAccount.checkPassword(password))) {
                return adminAccount;
            }
        }
        throw new NoAccountFoundException("We don't have any admin account registered in our system named " + firstname + " " + lastname + ".");
    }

    public boolean removeDeliveryAccount(DeliveryAccount deliveryAccount) {
        return this.deliveryList.remove(deliveryAccount);
    }

    private void checkEntryParameter(String firstname, String lastname, String password) {
        if (firstname == null || firstname.isEmpty() || lastname == null || lastname.isEmpty() || password == null || password.isEmpty()) {
            throw new IllegalArgumentException("The firstname, the lastname and the password can't be null or empty for a login...");
        }
    }
}
