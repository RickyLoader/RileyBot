package Runescape;

/**
 * Arguments for searching OSRS hiscores/building hiscores image
 */
public class OSRSHiscoresArgs extends HiscoresArgs {
    private final boolean leagueStats;
    private final boolean displayXpTracker;

    /**
     * Create the hiscores arguments
     *
     * @param virtual          Display virtual levels
     * @param leagueStats      Search for league stats instead of normal stats
     * @param displayXpTracker Display XP tracker info in the image
     */
    public OSRSHiscoresArgs(boolean virtual, boolean leagueStats, boolean displayXpTracker) {
        super(virtual);
        this.leagueStats = leagueStats;
        this.displayXpTracker = displayXpTracker;
    }

    /**
     * Check whether to display the player's XP tracker info in the image
     *
     * @return Display XP tracker in image
     */
    public boolean displayXpTracker() {
        return displayXpTracker;
    }

    /**
     * Check whether to search for the player's league stats instead of normal stats
     *
     * @return Search for league stats
     */
    public boolean searchLeagueStats() {
        return leagueStats;
    }
}
