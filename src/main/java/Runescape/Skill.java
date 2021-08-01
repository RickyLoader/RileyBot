package Runescape;

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
    public final DecimalFormat commaFormat = new DecimalFormat("#,###");
    private long gained, record;
    public static final int
            ICON_WIDTH = 50,
            DEFAULT_MAX = 99,
            ABSOLUTE_MAX = 120;
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
        VIRTUAL_TOTAL_LEVEL,
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
     * @param name  Skill name
     * @param rank  Skill rank
     * @param level Level
     * @param xp    XP value
     */
    public Skill(SKILL_NAME name, long rank, int level, long xp) {
        this.name = name;
        this.rank = rank;
        this.level = fixLevel(name, level);
        this.xp = xp;
        this.virtualLevel = name == SKILL_NAME.OVERALL || this.level <= 98 ? this.level : calculateVirtualLevel((int) xp);
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
                Long.parseLong(csv[rankIndex]),
                Integer.parseInt(csv[rankIndex + 1]),
                Long.parseLong(csv[rankIndex + 2])
        );
    }

    /**
     * Format the given number with commas
     *
     * @param number Number to format e.g 1000
     * @return Formatted number String e.g "1,000"
     */
    private String formatNumber(long number) {
        return commaFormat.format(number);
    }

    /**
     * Check if the player is ranked in this skill.
     * If a player is not ranked, the level returned from the hiscores will be 1 (even if the player is not level 1).
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
    private int calculateVirtualLevel(int xp) {
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
    private int getLevelExperience(int level) {
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
        if(level == ABSOLUTE_MAX) {
            return getLevelExperience(level);
        }
        return getLevelExperience(level + 1);
    }

    /**
     * Get the total experience required to be the current level.
     *
     * @return Experience to be current level
     */
    public int getXpAtCurrentLevel() {
        return getLevelExperience(level);
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
    private int fixLevel(SKILL_NAME name, int level) {
        switch(name) {
            case HITPOINTS:
                return level == 1 ? 10 : level;
            case INVENTION:
                return level == 0 ? 1 : level;
            default:
                return level;
        }
    }
}