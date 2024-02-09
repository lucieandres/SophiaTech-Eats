package fr.etu.steats.exception;

public class AlreadyRegisteredUser extends Exception {
    public AlreadyRegisteredUser(String msg) {
        super(msg);
    }
}
