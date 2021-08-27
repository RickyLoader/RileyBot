package Runescape.Stats;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Hold information on a HCIM death
 */
public class HCIMStatus {
    private final boolean dead;
    private final String cause, location, date;

    /**
     * Create a HCIM status
     *
     * @param dead     Account is dead (other values should not be set)
     * @param cause    Cause of death - e.g "Fighting against: Dharok The Wretched"
     * @param location Location of death - e.g "In an unknown location"
     * @param date     Date of death - e.g "12-Jan-2018 23:16"
     */
    private HCIMStatus(boolean dead, @Nullable String cause, @Nullable String location, @Nullable String date) {
        this.dead = dead;
        this.cause = cause;
        this.location = location;
        this.date = date;
    }

    /**
     * Create a HCIM status for a dead account
     *
     * @param cause    Cause of death - e.g "Fighting against: Dharok The Wretched"
     * @param location Location of death - e.g "In an unknown location"
     * @param date     Date of death - e.g "12-Jan-2018 23:16"
     */
    public HCIMStatus(@NotNull String cause, @NotNull String location, @NotNull String date) {
        this(true, cause, location, date);
    }

    /**
     * Create a HCIM status for an alive account
     */
    public HCIMStatus() {
        this(false, null, null, null);
    }

    /**
     * Check whether the account has died as a HCIM
     *
     * @return Death status
     */
    public boolean isDead() {
        return dead;
    }

    /**
     * Get the date of death
     * e.g "12-Jan-2018 23:16"
     *
     * @return Date of death
     */
    public String getDate() {
        return date;
    }

    /**
     * Get the location of death
     * e.g "In an unknown location"
     *
     * @return Location of death
     */
    public String getLocation() {
        return location;
    }

    /**
     * Get cause of death
     * e.g "Fighting against: Dharok The Wretched"
     *
     * @return Death cause
     */
    public String getCause() {
        return cause;
    }
}
