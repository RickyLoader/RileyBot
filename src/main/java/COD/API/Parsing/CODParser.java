package COD.API.Parsing;

import COD.API.CODManager;

/**
 * Parsing player data from COD JSON
 */
public class CODParser<T extends CODManager> {
    private final T manager;
    static final String
            SHOTS_LANDED_KEY = "shotsLanded",
            SHOTS_FIRED_KEY = "shotsFired",
            TEAM_1_SCORE_KEY = "team1Score",
            TEAM_2_SCORE_KEY = "team2Score",
            IS_PRESENT_KEY = "isPresentAtEnd",
            TEAM_KEY = "team",
            KILLS_KEY = "kills",
            DEATHS_KEY = "deaths",
            TIME_PLAYED_KEY = "timePlayed",
            LONGEST_STREAK_KEY = "longestStreak",
            HIGHEST_STREAK_KEY = "highestStreak",
            DISTANCE_TRAVELED_KEY = "distanceTraveled",
            PERCENT_TIME_MOVING_KEY = "percentTimeMoving",
            DAMAGE_DONE_KEY = "damageDone",
            DAMAGE_DEALT_KEY = "damageDealt",
            DAMAGE_TAKEN_KEY = "damageTaken",
            MATCH_XP_KEY = "matchXp",
            LOWER_HEADSHOTS_KEY = "headshots",
            UPPER_HEADSHOTS_KEY = "headShots",
            SUPERS_KEY = "supers",
            EXTRA_STAT_1_KEY = "extraStat1",
            HITS_KEY = "hits",
            SHOTS_KEY = "shots",
            USES_KEY = "uses",
            WINS_KEY = "wins",
            LOSSES_KEY = "losses";

    /**
     * Create the stats parser
     *
     * @param manager COD resource manager
     */
    public CODParser(T manager) {
        this.manager = manager;
    }

    /**
     * Get the COD resource manager
     *
     * @return COD resource manager
     */
    public T getManager() {
        return manager;
    }
}
