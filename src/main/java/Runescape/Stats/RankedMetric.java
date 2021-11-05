package Runescape.Stats;

import java.text.DecimalFormat;

/**
 * Ranked value for player
 */
public class RankedMetric {
    private final int rank;

    public static final int
            UNRANKED = -1,
            MAX_RANK = 2000000;

    /**
     * Create the ranked metric
     *
     * @param rank Player rank
     */
    public RankedMetric(int rank) {
        this.rank = rank;
    }

    /**
     * Check if the player is ranked for this metric.
     * If a player is not ranked, the rank returned from the hiscores will be -1.
     * A player is considered unranked if they are not in the top 2 million players for a given metric.
     *
     * @return Player is ranked in the metric
     */
    public boolean isRanked() {
        return rank != UNRANKED;
    }

    /**
     * Get the skill rank as a comma formatted String.
     * E.g 1234 -> "1,234" or -1 -> "-"
     *
     * @return Comma formatted rank
     */
    public String getFormattedRank() {
        return isRanked() ? new DecimalFormat("#,###").format(rank) : "-";
    }

    /**
     * Get the player rank
     *
     * @return Player rank
     */
    public int getRank() {
        return rank;
    }
}
