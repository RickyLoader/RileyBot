package Runescape.Stats;

import Bot.ResourceHandler;

import java.text.NumberFormat;

/**
 * Hold data on player last man standing info
 */
public class LastManStanding {
    public static final String POINTS_ICON = ResourceHandler.OSRS_BASE_PATH + "LMS/LMS.png";
    public static final int
            RANK_INDEX = 92,
            POINTS_INDEX = 93;
    private final int rank, points;

    /**
     * Create the Last Man Standing info
     *
     * @param rank   LMS point rank
     * @param points LMS total points
     */
    public LastManStanding(int rank, int points) {
        this.rank = rank;
        this.points = points;
    }

    /**
     * Get the player's LMS point rank
     *
     * @return LMS points rank
     */
    public int getRank() {
        return rank;
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
     * Get the rank formatted with commas
     *
     * @return Formatted rank
     */
    public String getFormattedRank() {
        return NumberFormat.getNumberInstance().format(rank);
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
