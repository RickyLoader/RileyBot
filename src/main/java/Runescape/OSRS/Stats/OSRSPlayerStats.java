package Runescape.OSRS.Stats;

import Runescape.OSRS.Boss.BossStats;
import Runescape.Clue;
import Runescape.OSRS.League.LeagueTier;
import Runescape.OSRS.League.Region;
import Runescape.OSRS.League.RelicTier;
import Runescape.PlayerStats;
import Runescape.Skill;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static Runescape.Skill.SKILL_NAME.*;

public class OSRSPlayerStats extends PlayerStats {
    private final List<BossStats> bossStatsKills;
    private final ArrayList<Achievement> completedAchievements, inProgressAchievements;
    private final LastManStanding lmsInfo;
    private LeagueTier leagueTier;
    private ArrayList<Region> regions;
    private ArrayList<RelicTier> relicTiers;
    private Date trackerStart, trackerEnd;

    /**
     * Create OSRS player stats
     *
     * @param name      Player name
     * @param url       URL to hiscores CSV
     * @param skills    Array of skill data
     * @param clues     Array of clue data
     * @param bossStats List of boss stats
     * @param lmsInfo   Last man standing info
     * @param type      Account type
     */
    public OSRSPlayerStats(String name, String url, Skill[] skills, Clue[] clues, List<BossStats> bossStats, LastManStanding lmsInfo, ACCOUNT type) {
        super(name, url, skills, clues, type);
        this.bossStatsKills = bossStats;
        this.lmsInfo = lmsInfo;
        this.completedAchievements = new ArrayList<>();
        this.inProgressAchievements = new ArrayList<>();
    }

    /**
     * Add the given achievement to either the list of completed or in progress achievements.
     *
     * @param achievement Achievement to add
     */
    public void addAchievement(Achievement achievement) {
        if(achievement.isCompleted()) {
            completedAchievements.add(achievement);
        }
        else {
            inProgressAchievements.add(achievement);
        }
    }

    /**
     * Get the players last man standing info
     *
     * @return Last man standing info
     */
    public LastManStanding getLmsInfo() {
        return lmsInfo;
    }

    /**
     * Check whether the player has any LMS points
     *
     * @return Player has LMS points
     */
    public boolean hasLmsPoints() {
        return lmsInfo.hasPoints();
    }

    /**
     * Get the list of completed achievements
     *
     * @return List of completed achievements
     */
    public ArrayList<Achievement> getCompletedAchievements() {
        return completedAchievements;
    }

    /**
     * Get the list of in progress achievements
     *
     * @return List of in progress achievements
     */
    public ArrayList<Achievement> getInProgressAchievements() {
        return inProgressAchievements;
    }

    /**
     * Get a String detailing the achievement completions of the player.
     *
     * @return Achievement summary
     */
    public String getAchievementSummary() {
        if(!hasAchievements()) {
            return "No achievements found!";
        }
        return completedAchievements.size()
                + " completed, "
                + inProgressAchievements.size() + " in progress";
    }

    /**
     * Check if the player has any achievements
     *
     * @return Player has achievements
     */
    public boolean hasAchievements() {
        return !inProgressAchievements.isEmpty() || !completedAchievements.isEmpty();
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
     * Get the league tier info
     *
     * @return League tier
     */
    public LeagueTier getLeagueTier() {
        return leagueTier;
    }

    /**
     * Set the player league points
     *
     * @param leaguePoints League points to set
     * @param rank         League point rank
     */
    public void setLeaguePoints(int leaguePoints, long rank) {
        this.leagueTier = new LeagueTier((leaguePoints == -1 ? 0 : leaguePoints), rank);
    }

    /**
     * Get a list of boss stats in order of kill count
     *
     * @return Player boss stats
     */
    public List<BossStats> getBossStats() {
        return bossStatsKills;
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

    /**
     * Set the XP tracker period with a start and end date
     *
     * @param start Start date of tracker
     * @param end   End date of tracker
     */
    public void setTrackerPeriod(Date start, Date end) {
        this.trackerStart = start;
        this.trackerEnd = end;
    }

    /**
     * Get the end date of the XP tracker
     *
     * @return End date
     */
    public Date getTrackerEndDate() {
        return trackerEnd;
    }

    /**
     * Get the start date of the XP tracker
     *
     * @return End date
     */
    public Date getTrackerStartDate() {
        return trackerStart;
    }

    /**
     * @see <a href="https://bit.ly/2TQlaWU">Formula on Oldschool Runescape Wiki</a>
     */
    @Override
    public int calculateCombatLevel() {
        final double adjustedPrayer = Math.floor((double) getSkill(PRAYER).getLevel() / 2);
        final double baseCombat = (getSkill(HITPOINTS).getLevel() + getSkill(DEFENCE).getLevel() + adjustedPrayer) / 4;
        final double multiplier = (getSkill(STRENGTH).getLevel() + getSkill(ATTACK).getLevel()) * 0.325;
        return (int) (baseCombat + multiplier);
    }
}
