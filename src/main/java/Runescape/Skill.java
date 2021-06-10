package Runescape;

import Bot.ResourceHandler;

import javax.annotation.Nullable;
import java.awt.image.BufferedImage;

/**
 * Hold data on a skill
 */
public class Skill {
    private final long rank, xp;
    private final int level, virtualLevel;
    private final SKILL_NAME name;
    private long gained;
    public final static String RANK_IMAGE_PATH = ResourceHandler.RUNESCAPE_BASE_PATH + "rank.png";
    private static final String BASE_IMAGE_PATH = ResourceHandler.OSRS_BASE_PATH + "Skills/";

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
         * @return Path to skill image
         */
        public String getImagePath() {
            return BASE_IMAGE_PATH + this.name() + ".png";
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
        this.virtualLevel = this.level <= 98 ? this.level : calculateVirtualLevel((int) xp);
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
     * Get the gained XP
     *
     * @return Gained XP
     */
    public long getGainedXP() {
        return gained;
    }

    /**
     * Set the gained XP to the given value
     *
     * @param gained XP gained
     */
    public void setGained(long gained) {
        this.gained = gained;
    }

    /**
     * Get the skill image
     *
     * @return Skill image (May be null if the skill has no image)
     */
    @Nullable
    public BufferedImage getImage() {
        return new ResourceHandler().getImageResource(name.getImagePath());
    }

    /**
     * Check if the skill has weekly gained XP
     *
     * @return Skill has weekly gained XP
     */
    public boolean hasGainedXP() {
        return gained > 0;
    }

    /**
     * Calculate the virtual level of a skill above 99
     *
     * @param xp XP to calculate virtual level for
     * @return Virtual level
     */
    private int calculateVirtualLevel(int xp) {
        int level;
        for(level = 99; level <= 120; level++) {
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
     * Get the skill level
     *
     * @return Level
     */
    public int getLevel() {
        return level;
    }

    /**
     * Get the skill xp value
     *
     * @return XP value
     */
    public long getXP() {
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