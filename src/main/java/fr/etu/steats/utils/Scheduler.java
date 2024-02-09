package fr.etu.steats.utils;

import java.util.concurrent.TimeUnit;

public class Scheduler {

    // This method is used to simulate a 10 minutes wait.
    public void waitTenMinutes() throws InterruptedException {
        try {
            TimeUnit.MINUTES.sleep(10);
        } catch (InterruptedException e) {
            throw new InterruptedException("The wait was interrupted.");
        }
    }
}
