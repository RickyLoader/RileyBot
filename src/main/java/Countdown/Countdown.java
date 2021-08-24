package Countdown;

import java.text.DecimalFormat;

/**
 * Hold a period of time
 */
public class Countdown {
    private final long days, hours, minutes, seconds, totalDays, totalHours, totalMinutes, totalSeconds, timeMs;
    private final DecimalFormat timeFormat = new DecimalFormat("00");

    /**
     * Create a countdown
     *
     * @param period Millisecond period to create countdown for
     */
    public Countdown(long period) {
        this.timeMs = period;
        this.totalSeconds = period / 1000;
        this.totalMinutes = totalSeconds / 60;
        this.totalHours = totalMinutes / 60;
        this.totalDays = totalHours / 24;

        this.seconds = totalSeconds % 60;
        this.minutes = totalMinutes % 60;
        this.hours = totalHours % 24;
        this.days = totalDays;
    }

    /**
     * Create a Countdown between the given times in ms
     *
     * @param from From time in ms
     * @param to   To time in ms
     * @return Countdown object
     */
    public static Countdown from(long from, long to) {
        return new Countdown(Math.abs(to - from));
    }

    /**
     * Format the given countdown in to a String showing mm:ss
     * Show total minutes instead of modulo as hours are not being displayed.
     *
     * @return Countdown formatted in String
     */
    public String formatMinutesSeconds() {
        return timeFormat.format(totalMinutes) + ":" + timeFormat.format(seconds);
    }

    /**
     * Format the countdown in to a String showing HH:mm:ss
     * Show total hours instead of modulo as days are not being displayed.
     *
     * @return Countdown formatted in String
     */
    public String formatHoursMinutesSeconds() {
        return timeFormat.format(totalHours) + ":" + timeFormat.format(minutes) + ":" + timeFormat.format(seconds);
    }

    /**
     * Get the total seconds between the two dates
     *
     * @return Total seconds
     */
    public long getTotalSeconds() {
        return totalSeconds;
    }

    /**
     * Get the total time (in ms)
     *
     * @return Total time (in ms)
     */
    public long getTimeMs() {
        return timeMs;
    }

    /**
     * Format the countdown in to a String showing dd:HH:mm:ss
     *
     * @return Countdown formatted in String
     */
    public String formatDaysHoursMinutes() {
        return timeFormat.format(totalDays) + ":" + timeFormat.format(hours) + ":" + timeFormat.format(minutes);
    }

    /**
     * Get remaining days
     *
     * @return Days
     */
    public long getDays() {
        return days;
    }

    /**
     * Get remaining hours
     *
     * @return Hours
     */
    public long getHours() {
        return hours;
    }

    /**
     * Get remaining minutes
     *
     * @return Minutes
     */
    public long getMinutes() {
        return minutes;
    }

    /**
     * Get remaining seconds
     *
     * @return Seconds
     */
    public long getSeconds() {
        return seconds;
    }
}