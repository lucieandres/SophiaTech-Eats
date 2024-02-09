package fr.etu.steats.utils;

import java.util.Objects;
import java.util.logging.Level;

import static fr.etu.steats.service.NotificationService.*;

/**
 * This class is used to define the type of user.
 * It allows to add colors to the log for different types of users.
 */
public class UserLevel extends Level {

    public static final UserLevel USER = new UserLevel("USER", 800, TEXT_BLUE);
    public static final UserLevel DELIVERY_MAN = new UserLevel("DELIVERY MAN", 800, TEXT_GREEN);
    public static final UserLevel RESTAURANT_MANAGER = new UserLevel("RESTAURANT MANAGER", 800, TEXT_YELLOW);

    private final String color;

    protected UserLevel(String name, int value, String color) {
        super(name, value);
        this.color = color;
    }

    public String getColor() {
        return color;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        UserLevel userLevel = (UserLevel) o;

        return Objects.equals(color, userLevel.color);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (color != null ? color.hashCode() : 0);
        return result;
    }
}
