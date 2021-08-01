package Runescape;

/**
 * Arguments for searching OSRS hiscores/building hiscores image
 */
public class OSRSHiscoresArgs extends HiscoresArgs {
    private final boolean leagueStats, fetchXpGains, fetchAchievements, showBoxes, highlightMaxedSkills;

    /**
     * Create the hiscores arguments
     *
     * @param virtual              Display virtual levels
     * @param leagueStats          Search for league stats instead of normal stats
     * @param fetchXpGains         Fetch recent XP gains
     * @param fetchAchievements    Fetch player achievements
     * @param showBoxes            Show the individual image boxes in the image
     * @param highlightMaxedSkills Highlight maxed skills as green
     */
    public OSRSHiscoresArgs(boolean virtual, boolean leagueStats, boolean fetchXpGains, boolean fetchAchievements, boolean showBoxes, boolean highlightMaxedSkills) {
        super(virtual);
        this.leagueStats = leagueStats;
        this.fetchXpGains = fetchXpGains;
        this.fetchAchievements = fetchAchievements;
        this.showBoxes = showBoxes;
        this.highlightMaxedSkills = highlightMaxedSkills;
    }

    /**
     * Check whether to highlight maxed skills as green in the image
     *
     * @return Highlight maxed skills
     */
    public boolean highlightMaxedSkills() {
        return highlightMaxedSkills;
    }

    /**
     * Check whether to show individual image boxes in the image
     *
     * @return Show image boxes
     */
    public boolean showBoxes() {
        return showBoxes;
    }

    /**
     * Check whether to fetch the player's recent achievements
     *
     * @return Fetch recent achievements
     */
    public boolean fetchAchievements() {
        return fetchAchievements;
    }

    /**
     * Check whether to fetch the player's recent XP gains
     *
     * @return Fetch XP gains
     */
    public boolean fetchXpGains() {
        return fetchXpGains;
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
