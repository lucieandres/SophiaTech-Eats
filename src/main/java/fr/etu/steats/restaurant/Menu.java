package fr.etu.steats.restaurant;

import fr.etu.steats.enums.ECustomerStatus;

public class Menu {
    private static final String ERROR_NEGATIVE = "Price cannot be negative";
    private String name;
    private double globalPrice;
    private boolean afterWork;
    private double studentPrice;
    private double staffPrice;
    private double facultyPrice;
    private double externalPrice;

    public Menu(String name, double price) {
        this(name, price, true);
    }

    public Menu(String name, double price, boolean afterWork) {
        if (price < 0) {
            throw new IllegalArgumentException(ERROR_NEGATIVE);
        }
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("Name cannot be null or empty");
        }
        this.name = name;
        this.globalPrice = price;
        this.afterWork = afterWork;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getPrice(ECustomerStatus typeOfCustomer) {
        if (typeOfCustomer == ECustomerStatus.STUDENT && studentPrice > 0) {
            return studentPrice;
        }
        if (typeOfCustomer == ECustomerStatus.STAFF && staffPrice > 0) {
            return staffPrice;
        }
        if (typeOfCustomer == ECustomerStatus.FACULTY && facultyPrice > 0) {
            return facultyPrice;
        }
        if (typeOfCustomer == ECustomerStatus.EXTERNAL && externalPrice > 0) {
            return externalPrice;
        }
        return globalPrice;
    }

    public double getGlobalPrice() {
        return globalPrice;
    }

    public void setGlobalPrice(double globalPrice) {
        if (globalPrice < 0) {
            throw new IllegalArgumentException(ERROR_NEGATIVE);
        }
        this.globalPrice = globalPrice;
    }

    public double getStudentPrice() {
        return studentPrice;
    }

    public void setStudentPrice(double studentPrice) {
        if (studentPrice < 0) {
            throw new IllegalArgumentException(ERROR_NEGATIVE);
        }
        this.studentPrice = studentPrice;
    }

    public double getStaffPrice() {
        return staffPrice;
    }

    public void setStaffPrice(double staffPrice) {
        if (staffPrice < 0) {
            throw new IllegalArgumentException(ERROR_NEGATIVE);
        }
        this.staffPrice = staffPrice;
    }

    public double getFacultyPrice() {
        return facultyPrice;
    }

    public void setFacultyPrice(double facultyPrice) {
        if (facultyPrice < 0) {
            throw new IllegalArgumentException(ERROR_NEGATIVE);
        }
        this.facultyPrice = facultyPrice;
    }

    public double getExternalPrice() {
        return externalPrice;
    }

    public void setExternalPrice(double externalPrice) {
        if (externalPrice < 0) {
            throw new IllegalArgumentException(ERROR_NEGATIVE);
        }
        this.externalPrice = externalPrice;
    }

    public boolean isAfterWork() {
        return this.afterWork;
    }

    public void setAfterWork(boolean afterWork) {
        this.afterWork = afterWork;
    }
}
