package Runescape.Stats;

import Bot.ResourceHandler;

import java.text.NumberFormat;

/**
 * Hold data on player last man standing info
 */
public class LastManStanding extends RankedMetric {
    public static final String POINTS_ICON = ResourceHandler.OSRS_BASE_PATH + "LMS/LMS.png";
    public static final int
            RANK_INDEX = 92,
            POINTS_INDEX = 93;
    private final int points;

    /**
     * Create the Last Man Standing info
     *
     * @param rank   LMS point rank
     * @param points LMS total points
     */
    public LastManStanding(int rank, int points) {
        super(rank);
        this.points = points;
    }

    /**
     * Get the player's LMS points
     *
     * @return LMS points
     */
    public int getPoints() {
        return points;
    }

    /**
     * Get the points formatted with commas
     *
     * @return Formatted points
     */
    public String getFormattedPoints() {
        return NumberFormat.getNumberInstance().format(points);
    }

    /**
     * Check whether the player has any points
     *
     * @return Player has points
     */
    public boolean hasPoints() {
        return points > 0;
    }
}
