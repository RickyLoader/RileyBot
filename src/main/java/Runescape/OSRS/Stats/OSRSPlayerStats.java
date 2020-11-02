package Runescape.OSRS.Stats;

import Runescape.Boss;
import Runescape.PlayerStats;
import Runescape.Skill;

import java.util.List;

public class OSRSPlayerStats extends PlayerStats {
    private final List<Boss> bossKills;
    private int leaguePoints;

    /**
     * Create OSRS player stats
     *
     * @param name      Player name
     * @param url       URL to hiscores CSV
     * @param skills    Array of skill data
     * @param clues     Array of clue data
     * @param bossKills List of boss kill data
     * @param type      Account type
     */
    public OSRSPlayerStats(String name, String url, Skill[] skills, String[] clues, List<Boss> bossKills, ACCOUNT type) {
        super(name, url, skills, clues, type);
        this.bossKills = bossKills;
    }

    /**
     * Check if the stats are from a league
     *
     * @return Stats are from a league
     */
    public boolean isLeague() {
        return getAccountType() == PlayerStats.ACCOUNT.LEAGUE;
    }

    /**
     * Get the player league points
     *
     * @return League points
     */
    public String getLeaguePoints() {
        return commaFormat.format(leaguePoints) + " points";
    }

    /**
     * Set the player league points
     *
     * @param leaguePoints League points to set
     */
    public void setLeaguePoints(int leaguePoints) {
        this.leaguePoints = leaguePoints == -1 ? 0 : leaguePoints;
    }

    /**
     * Get a list of bosses in order of player kill count
     *
     * @return Bosses
     */
    public List<Boss> getBossKills() {
        return bossKills;
    }
}
