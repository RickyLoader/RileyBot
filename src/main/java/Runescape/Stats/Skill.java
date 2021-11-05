package Runescape.Stats;

import Bot.ResourceHandler;

import javax.annotation.Nullable;
import java.awt.image.BufferedImage;
import java.text.DecimalFormat;

/**
 * Hold data on a skill
 */
public class Skill extends RankedMetric {
    private final long xp, maxXp, xpAtMaxLevel;
    private final int level, virtualLevel, maxLevel;
    private final SKILL_NAME name;
    private long gained, record;
    public static final int
            ICON_WIDTH = 50,
            DEFAULT_MAX_LEVEL = 99,
            DEFAULT_MAX_XP = 200000000;
    public static final long DEFAULT_XP_AT_MAX_LEVEL = getLevelXp(DEFAULT_MAX_LEVEL);
    public static final String
            RANK_IMAGE_PATH = ResourceHandler.RUNESCAPE_BASE_PATH + "rank.png",
            BASE_IMAGE_PATH = ResourceHandler.OSRS_BASE_PATH + "Skills/",
            BASE_LARGE_IMAGE_PATH = BASE_IMAGE_PATH + "Large/",
            LARGE_COMBAT_IMAGE_PATH = BASE_LARGE_IMAGE_PATH + "COMBAT.png",
            BASE_SMALL_IMAGE_PATH = BASE_IMAGE_PATH + "Small/";

    public enum SKILL_NAME {
        ATTACK,
        HITPOINTS,
        MINING,
        STRENGTH,
        AGILITY,
        SMITHING,
        DEFENCE,
        HERBLORE,
        FISHING,
        RANGED,
        THIEVING,
        COOKING,
        PRAYER,
        CRAFTING,
        FIREMAKING,
        MAGIC,
        FLETCHING,
        WOODCUTTING,
        RUNECRAFTING,
        SLAYER,
        FARMING,
        CONSTRUCTION,
        HUNTER,
        SUMMONING,
        DUNGEONEERING,
        DIVINATION,
        INVENTION,
        ARCHAEOLOGY,
        OVERALL,
        COMBAT,
        UNKNOWN;

        /**
         * Get the path to the image for the skill
         *
         * @param large Get the path for the large skill image
         * @return Path to skill image
         */
        public String getImagePath(boolean large) {
            return (large ? BASE_LARGE_IMAGE_PATH : BASE_SMALL_IMAGE_PATH) + this.name() + ".png";
        }

        /**
         * Get a skill name from the given skill name String
         *
         * @param skillName Skill name String e.g "attack"
         * @return Skill name e.g ATTACK (or UNKNOWN if unable to find a match)
         */
        public static SKILL_NAME fromName(String skillName) {
            try {
                return SKILL_NAME.valueOf(skillName.toUpperCase());
            }
            catch(IllegalArgumentException e) {
                return UNKNOWN;
            }
        }
    }

    /**
     * Create a skill
     *
     * @param name         Skill name
     * @param rank         Skill rank
     * @param level        Current level
     * @param xp           Current XP
     * @param virtualLevel Virtual level (levels after max)
     * @param maxLevel     Max (non virtual) level for the skill - e.g level 99
     * @param xpAtMaxLevel XP at the max level for the skill - e.g the XP for level 99
     * @param maxXp        Maximum possible XP in skill
     */
    public Skill(SKILL_NAME name, int rank, int level, long xp, int virtualLevel, int maxLevel, long xpAtMaxLevel, long maxXp) {
        super(rank);
        this.name = name;
        this.level = level;
        this.xp = xp;
        this.virtualLevel = virtualLevel;
        this.maxLevel = maxLevel;
        this.xpAtMaxLevel = xpAtMaxLevel;
        this.maxXp = maxXp;
    }

    /**
     * Create a skill, calculate the virtual level.
     *
     * @param name         Skill name
     * @param rank         Skill rank
     * @param level        Current level
     * @param xp           Current XP
     * @param xpAtMaxLevel XP at the max level for the skill - e.g the XP for level 99
     * @param maxXp        Maximum possible XP in skill
     */
    public Skill(SKILL_NAME name, int rank, int level, long xp, long xpAtMaxLevel, long maxXp) {
        this(
                name,
                rank,
                fixLevel(name, level),
                xp,
                getLevelAtXp(xp),
                getLevelAtXp(xpAtMaxLevel),
                xpAtMaxLevel,
                maxXp
        );
    }

    /**
     * Create a skill using the CSV from the hiscores API
     *
     * @param name         Skill name
     * @param rankIndex    Index of skill rank value - other values can be obtained relative to this index
     * @param csv          CSV from API
     * @param xpAtMaxLevel XP at the max level for the skill - e.g the XP for level 99
     * @param maxXp        Maximum possible XP in skill
     */
    public Skill(SKILL_NAME name, int rankIndex, String[] csv, long xpAtMaxLevel, long maxXp) {
        this(
                name,
                parseRank(csv, rankIndex),
                parseLevel(csv, rankIndex),
                parseXp(csv, rankIndex),
                xpAtMaxLevel,
                maxXp
        );
    }

    /**
     * Create a skill using the CSV from the hiscores API with default max values
     *
     * @param name      Skill name
     * @param rankIndex Index of skill rank value - other values can be obtained relative to this index
     * @param csv       CSV from API
     */
    public Skill(SKILL_NAME name, int rankIndex, String[] csv) {
        this(
                name,
                rankIndex,
                csv,
                DEFAULT_XP_AT_MAX_LEVEL,
                DEFAULT_MAX_XP
        );
    }

    /**
     * Parse the rank of the skill using CSV from the hiscores API.
     *
     * @param csv       Hiscores CSV
     * @param rankIndex Index of skill rank value - other values can be obtained relative to this index
     * @return Skill rank
     */
    protected static int parseRank(String[] csv, int rankIndex) {
        return Integer.parseInt(csv[rankIndex]);
    }

    /**
     * Parse the level of the skill using CSV from the hiscores API.
     *
     * @param csv       Hiscores CSV
     * @param rankIndex Index of skill rank value - other values can be obtained relative to this index
     * @return Skill level
     */
    protected static int parseLevel(String[] csv, int rankIndex) {
        return Integer.parseInt(csv[rankIndex + 1]);
    }

    /**
     * Parse the XP of the skill using CSV from the hiscores API.
     *
     * @param csv       Hiscores CSV
     * @param rankIndex Index of skill rank value - other values can be obtained relative to this index
     * @return Skill XP
     */
    protected static long parseXp(String[] csv, int rankIndex) {
        return Long.parseLong(csv[rankIndex + 2]);
    }

    /**
     * Format the given number with commas
     *
     * @param number Number to format e.g 1000
     * @return Formatted number String e.g "1,000"
     */
    public static String formatNumber(long number) {
        return new DecimalFormat("#,###").format(number);
    }

    /**
     * Get the skill XP as a comma formatted String.
     * E.g 1234 -> "1,234"
     *
     * @return Comma formatted XP
     */
    public String getFormattedXp() {
        return formatNumber(xp);
    }

    /**
     * Get the weekly gained XP
     *
     * @return Gained XP
     */
    public long getGainedXp() {
        return gained;
    }

    /**
     * Get the record XP gained in a week
     *
     * @return Record weekly XP
     */
    public long getRecordXp() {
        return record;
    }

    /**
     * Set the weekly gained XP to the given value
     *
     * @param gained Weekly XP gained
     */
    public void setGainedXp(long gained) {
        this.gained = gained;
    }

    /**
     * Set the weekly record of gained XP to the given value
     *
     * @param record Weekly XP gained record
     */
    public void setRecordXp(long record) {
        this.record = record;
    }

    /**
     * Get the skill image
     *
     * @param large Get the large version of the skill image
     * @return Skill image (May be null if the skill has no image)
     */
    @Nullable
    public BufferedImage getImage(boolean large) {
        return new ResourceHandler().getImageResource(name.getImagePath(large));
    }

    /**
     * Check if the skill has weekly gained XP
     *
     * @return Skill has weekly gained XP
     */
    public boolean hasGainedXp() {
        return gained > 0;
    }

    /**
     * Check if the skill has a weekly gained XP record
     *
     * @return Skill has weekly gained XP record
     */
    public boolean hasRecordXp() {
        return record > 0;
    }

    /**
     * Get the XP required to be the given level.
     *
     * @param level Level to calculate experience for
     * @return XP to be given level
     */
    public static long getLevelXp(int level) {
        double xp = 0;
        for(int i = 1; i < level; i++) {
            xp += Math.floor(
                    i + (300 * Math.pow(2, i / 7.0))
            );
        }
        return (long) Math.floor(xp / 4);
    }

    /**
     * Get the level that a skill will be at the given XP.
     *
     * @param xp XP to calculate level for e.g 83
     * @return Level at given XP - e.g 2
     */
    public static int getLevelAtXp(long xp) {
        int level = 1;
        long levelXp;

        // Iterate until the level XP is greater than/equal to the given XP
        while(xp > (levelXp = getLevelXp(level))) {
            level++;
        }

        // XP will either be equal to or lower than the level XP
        return xp == levelXp ? level : level - 1;
    }

    /**
     * Get the virtual level of a skill (Calculated beyond the standard max of 99)
     *
     * @return Virtual level
     */
    public int getVirtualLevel() {
        return virtualLevel;
    }

    /**
     * Get the total experience required to be the next level.
     * This is not the experience remaining until the next level, but the total experience that
     * would be had at the next level.
     *
     * @return Experience at next level
     */
    public long getXpAtNextLevel() {

        // The max XP may fall part way through the max virtual level, show max XP as the next goal
        if(virtualLevel >= getLevelAtXp(maxXp) - 1) {
            return maxXp;
        }

        return getLevelXp(virtualLevel + 1);
    }

    /**
     * Get the total experience required to be the current level.
     *
     * @return Experience to be current level
     */
    public long getXpAtCurrentLevel() {
        return getLevelXp(virtualLevel);
    }

    /**
     * Get the total experience to be the max normal level (e.g 99)
     *
     * @return Experience to be max level
     */
    public long getXpAtMaxLevel() {
        return xpAtMaxLevel;
    }

    /**
     * Get the maximum XP that the skill can have
     *
     * @return Maximum XP
     */
    public long getMaxXp() {
        return maxXp;
    }

    /**
     * Get the skill level
     *
     * @return Level
     */
    public int getLevel() {
        return level;
    }

    /**
     * Get the current skill XP
     *
     * @return Current XP
     */
    public long getXp() {
        return xp;
    }

    /**
     * Get the skill name
     *
     * @return Skill name
     */
    public SKILL_NAME getName() {
        return name;
    }

    /**
     * Fix the level provided by the hiscores - e.g Hitpoints returning as 1 when
     * it begins at 10.
     *
     * @param name  Skill name
     * @param level Level provided by hiscores
     * @return Fixed level
     */
    private static int fixLevel(SKILL_NAME name, int level) {
        switch(name) {
            case HITPOINTS:
                return level == 1 ? 10 : level;
            case INVENTION:
                return level == 0 ? 1 : level;
            default:
                return level;
        }
    }

    /**
     * Get the percentage progress until the next level.
     * If the player has max XP, this will be 1.0.
     *
     * @return Percentage progress
     */
    public double getProgressUntilNextLevel() {

        // XP required to be the current level (not current XP)
        final long xpAtCurrentLevel = getXpAtCurrentLevel();

        // Total XP required to go from current level (not current XP) to the next level
        final long totalXpInLevel = getXpAtNextLevel() - xpAtCurrentLevel;

        // Percentage progress until next level (e.g 0.42)
        return totalXpInLevel == 0 ? 1.0 : ((xp - xpAtCurrentLevel) / (double) totalXpInLevel);
    }

    /**
     * Check if the given skill is "maxed" based on the max possible XP.
     *
     * @param virtual Use virtual levels
     * @return Skill is maxed
     */
    public boolean isMaxed(boolean virtual) {
        return virtual ? xp == maxXp : xp >= xpAtMaxLevel;
    }

    /**
     * Get the max (non virtual) level for the skill
     *
     * @return Max skill level - e.g level 99
     */
    public int getMaxLevel() {
        return maxLevel;
    }
}