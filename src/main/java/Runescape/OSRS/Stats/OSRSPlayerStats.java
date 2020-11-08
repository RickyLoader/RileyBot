package Runescape.OSRS.Stats;

import Runescape.Boss;
import Runescape.OSRS.League.Region;
import Runescape.OSRS.League.RelicTier;
import Runescape.PlayerStats;
import Runescape.Skill;

import java.util.ArrayList;
import java.util.List;

public class OSRSPlayerStats extends PlayerStats {
    private final List<Boss> bossKills;
    private int leaguePoints;
    private ArrayList<Region> regions;
    private ArrayList<RelicTier> relicTiers;

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

    /**
     * Get the player unlocked relic tiers
     *
     * @return Player unlocked relic tiers
     */
    public ArrayList<RelicTier> getRelicTiers() {
        return relicTiers;
    }

    /**
     * Get the player unlocked regions
     *
     * @return Player unlocked regions
     */
    public ArrayList<Region> getRegions() {
        return regions;
    }

    /**
     * Set the list of player unlocked relic tiers
     *
     * @param relicTiers List of relic tiers the player has unlocked
     */
    public void setRelicTiers(ArrayList<RelicTier> relicTiers) {
        this.relicTiers = relicTiers;
    }

    /**
     * Set the list of player unlocked regions
     *
     * @param regions List of regions the player has unlocked
     */
    public void setRegions(ArrayList<Region> regions) {
        this.regions = regions;
    }

    /**
     * Check if the player has any stored relic & region
     * unlock data
     *
     * @return Player has stored relic & region data
     */
    public boolean hasLeagueUnlockData() {
        return !relicTiers.isEmpty() && !regions.isEmpty();
    }

    /**
     * Add weekly gained XP to the skill of the given name
     *
     * @param skillName Skill name
     * @param gained    XP gained
     */
    public void addGainedXP(Skill.SKILL_NAME skillName, long gained) {
        getSkill(skillName).setGained(gained);
    }

    /**
     * Check if the player has weekly gained XP
     *
     * @return Player has weekly gained XP
     */
    public boolean hasWeeklyGains() {
        return getTotal().hasGainedXP();
    }

    /**
     * Get the weekly gained XP
     *
     * @return Weekly gained XP
     */
    public long getGainedXP() {
        return getTotal().getGainedXP();
    }
}
