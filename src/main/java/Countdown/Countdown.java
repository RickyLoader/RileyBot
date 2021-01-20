package Countdown;

/**
 * Hold a period of time
 */
public class Countdown {
    private final long days, hours, minutes, seconds;

    /**
     * Create a countdown
     *
     * @param days    Days until release
     * @param hours   Hours until release
     * @param minutes Minutes until release
     * @param seconds Seconds until release
     */
    private Countdown(long days, long hours, long minutes, long seconds) {
        this.days = days;
        this.hours = hours;
        this.minutes = minutes;
        this.seconds = seconds;
    }

    /**
     * Create a Countdown until the current time in ms
     *
     * @param from From time in ms
     * @param to   To time in ms
     * @return Countdown object
     */
    public static Countdown from(long from, long to) {
        long period = Math.abs(to - from);
        long seconds = period / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;
        return new Countdown(
                days,
                hours % 24,
                minutes % 60,
                seconds % 60
        );
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