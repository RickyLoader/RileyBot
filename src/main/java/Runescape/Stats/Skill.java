package Runescape.Stats;

import Bot.ResourceHandler;

import javax.annotation.Nullable;
import java.awt.image.BufferedImage;
import java.text.DecimalFormat;

/**
 * Hold data on a skill
 */
public class Skill {
    private final long rank, xp;
    private final int level, virtualLevel;
    private final SKILL_NAME name;
    public static final DecimalFormat commaFormat = new DecimalFormat("#,###");
    private long gained, record;
    public static final int
            ICON_WIDTH = 50,
            DEFAULT_MAX = 99,
            ABSOLUTE_MAX = 126,
            MAX_RANK = 2000000,
            MAX_XP = 200000000;
    public static final String
            RANK_IMAGE_PATH = ResourceHandler.RUNESCAPE_BASE_PATH + "rank.png",
            BASE_IMAGE_PATH = ResourceHandler.OSRS_BASE_PATH + "Skills/",
            BASE_LARGE_IMAGE_PATH = BASE_IMAGE_PATH + "Large/",
            LARGE_COMBAT_IMAGE_PATH = BASE_LARGE_IMAGE_PATH + "COMBAT.png",
            BASE_SMALL_IMAGE_PATH = BASE_IMAGE_PATH + "Small/";

    public static final long MAX_LEVEL_XP = getLevelExperience(DEFAULT_MAX);

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
     * @param level        Level
     * @param xp           XP value
     * @param virtualLevel Virtual level (levels after max)
     */
    public Skill(SKILL_NAME name, long rank, int level, long xp, int virtualLevel) {
        this.name = name;
        this.rank = rank;
        this.level = fixLevel(name, level);
        this.xp = xp;
        this.virtualLevel = virtualLevel;
    }

    /**
     * Create a skill, calculate the virtual level.
     *
     * @param name  Skill name
     * @param rank  Skill rank
     * @param level Level
     * @param xp    XP value
     */
    public Skill(SKILL_NAME name, long rank, int level, long xp) {
        this(
                name,
                rank,
                level,
                xp,
                level <= (DEFAULT_MAX - 1) ? level : calculateVirtualLevel((int) xp)
        );
    }

    /**
     * Create a skill using the CSV from the hiscores API
     *
     * @param name      Skill name
     * @param rankIndex Index of skill rank value - other values can be obtained relative to this index
     * @param csv       CSV from API
     */
    public Skill(SKILL_NAME name, int rankIndex, String[] csv) {
        this(
                name,
                parseRank(csv, rankIndex),
                parseLevel(csv, rankIndex),
                parseXp(csv, rankIndex)
        );
    }

    /**
     * Parse the rank of the skill using CSV from the hiscores API.
     *
     * @param csv       Hiscores CSV
     * @param rankIndex Index of skill rank value - other values can be obtained relative to this index
     * @return Skill rank
     */
    protected static long parseRank(String[] csv, int rankIndex) {
        return Long.parseLong(csv[rankIndex]);
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
        return commaFormat.format(number);
    }

    /**
     * Check if the player is ranked in this skill.
     * If a player is not ranked, the rank returned from the hiscores will be -1 and the level will be 1
     * (even if the player has leveled the skill).
     * A player is considered unranked if they are not in the top 2 million players for a given skill.
     *
     * @return Player is ranked in the skill
     */
    public boolean isRanked() {
        return rank != PlayerStats.UNRANKED;
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
     * Get the skill rank as a comma formatted String.
     * E.g 1234 -> "1,234"
     *
     * @return Comma formatted rank
     */
    public String getFormattedRank() {
        return formatNumber(rank);
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
     * Calculate the virtual level of a skill above 99
     *
     * @param xp XP to calculate virtual level for
     * @return Virtual level
     */
    private static int calculateVirtualLevel(int xp) {
        int level;
        for(level = DEFAULT_MAX; level <= ABSOLUTE_MAX; level++) {
            if(getLevelExperience(level) > xp) {
                break;
            }
        }
        return level - 1;
    }

    /**
     * Get the experience value at the given level
     *
     * @param level Level to calculate experience for
     * @return Experience at given level
     */
    public static int getLevelExperience(int level) {
        double xp = 0;
        for(int i = 1; i < level; i++) {
            xp += Math.floor(
                    i + (300 * Math.pow(2, i / 7.0))
            );
        }
        return (int) Math.floor(xp / 4);
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
    public int getXpAtNextLevel() {

        // The max XP falls part way through the max virtual level, show max XP as the next goal once at 125/126
        if(virtualLevel >= ABSOLUTE_MAX - 1) {
            return MAX_XP;
        }
        return getLevelExperience(virtualLevel + 1);
    }

    /**
     * Get the total experience required to be the current level.
     *
     * @return Experience to be current level
     */
    public int getXpAtCurrentLevel() {
        return getLevelExperience(virtualLevel);
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
     * Get the skill XP value
     *
     * @return XP value
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
     * Get the skill rank
     *
     * @return Skill rank
     */
    public long getRank() {
        return rank;
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
        final int xpAtCurrentLevel = getXpAtCurrentLevel();

        // Total XP required to go from current level (not current XP) to the next level
        final int totalXpInLevel = getXpAtNextLevel() - xpAtCurrentLevel;

        // Percentage progress until next level (e.g 0.42)
        return totalXpInLevel == 0 ? 1.0 : ((xp - xpAtCurrentLevel) / (double) totalXpInLevel);
    }

    /**
     * Check if the given skill is "maxed" based on the max possible level/XP.
     * The max XP falls part way through the max virtual level, meaning a skill may be the max virtual level without
     * having the max XP. In these cases, the skill is not considered maxed until the max XP is reached.
     *
     * @param virtual Use virtual levels
     * @return Skill is maxed
     */
    public boolean isMaxed(boolean virtual) {
        return virtual ? xp == Skill.MAX_XP : level >= Skill.DEFAULT_MAX;
    }
}