package COD.PlayerStats;

import COD.Assets.Ratio;

/**
 * Hold basic COD player stats - ratios etc
 */
public class PlayerBasicStats {
    private final int longestKillstreak;
    private final Ratio winLoss, killDeath;

    /**
     * Create the basic player stats
     *
     * @param longestKillstreak Longest killstreak the player has achieved
     * @param winLoss           Win/loss ratio
     * @param killDeath         Kill/death ratio
     */
    public PlayerBasicStats(int longestKillstreak, Ratio winLoss, Ratio killDeath) {
        this.longestKillstreak = longestKillstreak;
        this.winLoss = winLoss;
        this.killDeath = killDeath;
    }

    /**
     * Get the longest killstreak that the player has achieved
     *
     * @return Player longest killsteak
     */
    public int getLongestKillstreak() {
        return longestKillstreak;
    }

    /**
     * Get the kill/death ratio of the player
     *
     * @return Kill/death ratio
     */
    public Ratio getKillDeath() {
        return killDeath;
    }

    /**
     * Get the win/loss ratio of the player
     *
     * @return Win/loss ratio
     */
    public Ratio getWinLoss() {
        return winLoss;
    }

    /**
     * Get formatted kill/death ratio
     *
     * @return K/D String
     */
    public String getFormattedKillDeath() {
        return killDeath.formatRatio(killDeath.getRatio());
    }

    /**
     * Get formatted win/loss ratio
     *
     * @return W/L String
     */
    public String getFormattedWinLoss() {
        return winLoss.formatRatio(winLoss.getRatio());
    }

    /**
     * Get formatted total wins
     *
     * @return Total wins
     */
    public String getFormattedWins() {
        return winLoss.formatNumerator();
    }

    /**
     * Get formatted total losses
     *
     * @return Total losses
     */
    public String getFormattedLosses() {
        return winLoss.formatDenominator();
    }

    /**
     * Get longest kill streak
     *
     * @return Longest kill streak
     */
    public int getLongestKillStreak() {
        return longestKillstreak;
    }
}
