package Runescape.Stats;

import Bot.ResourceHandler;
import org.jetbrains.annotations.Nullable;

import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;

/**
 * Hold Runescape player hiscores entry
 */
public abstract class PlayerStats {
    private final String name, url;
    private final Clue[] clues;
    private final Skill[] skills;
    private final HashMap<Skill.SKILL_NAME, Skill> skillMap;
    private final HashMap<Clue.TYPE, Clue> clueMap;
    private final ACCOUNT type;
    private final Skill total, closestToLevel, virtualClosestToLevel, highestXp;
    private final int combatLevel, totalMaxedSkills, totalMaxedVirtualSkills;
    private final long xpTowardsMax;

    // Value given to a players skill xp/boss kills/etc that are not ranked (not in top 2 million players)
    public static final int UNRANKED = -1;

    public enum ACCOUNT {
        NORMAL("", "normal"),
        IRON("_ironman", "ironman"),
        ULTIMATE("_ultimate", "ultimate_ironman"),
        LEAGUE("_seasonal", "seasonal"),
        HARDCORE("_hardcore_ironman", "hardcore_ironman"),
        DMM("_tournament", "tournament"),
        LOCATE();

        private final String urlSuffix, databaseKey;
        public static final String
                SKULL_IMAGE_FILENAME = "skull.png",
                IMAGE_PATH = ResourceHandler.RUNESCAPE_BASE_PATH + "Accounts/",
                SKULL_IMAGE_PATH = IMAGE_PATH + SKULL_IMAGE_FILENAME;

        /**
         * Create an account type
         *
         * @param urlSuffix   Suffix to append to a hiscores URL to fetch stats for this account type
         * @param databaseKey Database key for account type - e.g "ironman"
         */
        ACCOUNT(@Nullable String urlSuffix, @Nullable String databaseKey) {
            this.urlSuffix = urlSuffix;
            this.databaseKey = databaseKey;
        }

        /**
         * Create an account type without a URL suffix or database key
         */
        ACCOUNT() {
            this(null, null);
        }

        /**
         * Get an account type by name
         *
         * @param name Type name - e.g "iron"
         * @return Type e.g IRON or null
         */
        @Nullable
        public static ACCOUNT fromName(String name) {
            try {
                return ACCOUNT.valueOf(name.toUpperCase());
            }
            catch(IllegalArgumentException e) {
                return null;
            }
        }

        /**
         * Check if the given name is an account type
         *
         * @param name Type name - e.g "iron"
         * @return Name is account type
         */
        public static boolean isAccountType(String name) {
            return fromName(name) != null;
        }

        /**
         * Get the suffix to append to a hiscores URL for this account type
         *
         * @return Suffix - e.g "_iron"
         */
        public String getUrlSuffix() {
            return urlSuffix;
        }

        /**
         * Get the database key for this account type.
         *
         * @return Database key - e.g "ironman" or null
         */
        @Nullable
        public String getDatabaseKey() {
            return databaseKey;
        }

        /**
         * Get the icon for the account type (not all account types have an icon).
         *
         * @return Account type icon or null
         */
        @Nullable
        public BufferedImage getIcon() {

            // E.g "iron.png"
            return new ResourceHandler().getImageResource(IMAGE_PATH + this.name().toLowerCase() + ".png");
        }
    }

    /**
     * Create player stats
     *
     * @param name   Player name
     * @param url    URL to hiscores CSV
     * @param skills Array of player skills (excluding total level)
     * @param total  Total level
     * @param clues  Array of clue data
     * @param type   Account type
     */
    public PlayerStats(String name, String url, Skill[] skills, Clue[] clues, TotalLevel total, ACCOUNT type) {
        this.name = name;
        this.url = url;
        this.skills = skills;
        this.clues = clues;
        this.type = type;
        this.total = total;
        this.skillMap = new HashMap<>();

        // Map skills by their unique ID & calculate running values
        int totalMaxedSkills = 0, totalMaxedVirtualSkills = 0;

        /*
         * Start with total XP and remove any post 99 XP to find the progress towards max.
         * This is more accurate than summing the XP of skills as some may not be ranked and will report -1 XP
         * while their actual XP is still included in the total.
         */
        long xpTowardsMax = total.getXp();

        for(Skill skill : skills) {

            // Max XP
            if(skill.isMaxed(true)) {
                totalMaxedVirtualSkills++;
            }

            // Level 99
            if(skill.isMaxed(false)) {
                totalMaxedSkills++;

                // Don't count XP over level 99
                if(skill.getXp() > Skill.MAX_LEVEL_XP) {
                    final long xpToRemove = (skill.getXp() - Skill.MAX_LEVEL_XP);
                    xpTowardsMax -= xpToRemove;
                }
            }

            // Map by ID
            skillMap.put(skill.getName(), skill);
        }

        this.totalMaxedSkills = totalMaxedSkills;
        this.totalMaxedVirtualSkills = totalMaxedVirtualSkills;
        this.xpTowardsMax = xpTowardsMax;

        this.clueMap = new HashMap<>();
        for(Clue clue : clues) {
            clueMap.put(clue.getType(), clue);
        }

        this.combatLevel = calculateCombatLevel();

        this.virtualClosestToLevel = calculateClosestToLevelSkill(skills, true);
        this.closestToLevel = calculateClosestToLevelSkill(skills, false);

        this.highestXp = calculateHighestXpSkill(skills);

        // Sort by in-game order
        sortSkills(skills);
    }

    /**
     * Calculate the skill that has the highest XP
     * This is null if all skills are equal.
     *
     * @param skills Player skills
     * @return Highest XP skill
     */
    @Nullable
    private Skill calculateHighestXpSkill(Skill[] skills) {
        Arrays.sort(skills, Comparator.comparingLong(Skill::getXp));
        final Skill highestXp = skills[skills.length - 1];
        return skills[0].getXp() == highestXp.getXp() ? null : highestXp;
    }

    /**
     * Calculate the skill that is closest to leveling.
     * This is null if all skills are equal.
     *
     * @param skills  Player skills
     * @param virtual Include virtual levels
     * @return Closest to level skill
     */
    @Nullable
    private Skill calculateClosestToLevelSkill(Skill[] skills, boolean virtual) {

        skills = Arrays.stream(skills)

                // Ignore maxed skills as they have 100% progress
                .filter(skill -> {
                    final boolean notMaxed = skill.getVirtualLevel() < Skill.ABSOLUTE_MAX;

                    // Exclude virtual levels
                    if(!virtual) {
                        return notMaxed && skill.getLevel() < Skill.DEFAULT_MAX;
                    }

                    return notMaxed;
                })

                .sorted(Comparator.comparingDouble(Skill::getProgressUntilNextLevel))
                .toArray(Skill[]::new);

        // Every skill is maxed - no skills are closest to leveling
        if(skills.length == 0) {
            return null;
        }

        final Skill highestProgress = skills[skills.length - 1];
        return skills[0].getProgressUntilNextLevel() == highestProgress.getProgressUntilNextLevel()
                ? null
                : highestProgress;
    }

    /**
     * Get the skill that is closest to leveling.
     * This is null if all skills are equal.
     *
     * @param virtual Include virtual level progress
     * @return Closest to level skill
     */
    @Nullable
    public Skill getClosestToLevelSkill(boolean virtual) {
        return virtual ? virtualClosestToLevel : closestToLevel;
    }

    /**
     * Get the skill with the highest XP.
     * This is null if all skills are equal.
     *
     * @return Highest XP skill
     */
    @Nullable
    public Skill getHighestXpSkill() {
        return highestXp;
    }

    /**
     * Calculate the player's combat level
     *
     * @return Player's combat level
     */
    public abstract int calculateCombatLevel();

    /**
     * Get the player's combat level
     *
     * @return Combat level
     */
    public int getCombatLevel() {
        return combatLevel;
    }

    /**
     * Get the skill with the given name
     *
     * @param name Skill name
     * @return Skill from given name
     */
    public Skill getSkill(Skill.SKILL_NAME name) {
        return name == Skill.SKILL_NAME.OVERALL ? total : skillMap.get(name);
    }

    /**
     * Get the URL to the hiscores CSV
     *
     * @return URL to hiscores CSV
     */
    public String getUrl() {
        return url;
    }

    /**
     * Get the player name
     *
     * @return Player name
     */
    public String getName() {
        return name;
    }

    /**
     * Get a list of clue scroll data
     *
     * @return Clue scroll data
     */
    public Clue[] getClues() {
        return clues;
    }

    /**
     * Check if the player is a hardcore account
     *
     * @return Player is a hardcore account
     */
    public boolean isHardcore() {
        return type == ACCOUNT.HARDCORE;
    }

    /**
     * Check if the player has any clue completions
     *
     * @return Player has clue completions
     */
    public boolean hasClueCompletions() {
        return clueMap.get(Clue.TYPE.ALL).hasCompletions();
    }

    /**
     * Get the player account type
     *
     * @return Account type
     */
    public ACCOUNT getAccountType() {
        return type;
    }

    /**
     * Get a list of skill values in the in-game order (reading left to right).
     * This does not include the total level.
     *
     * @return Player skill values
     */
    public Skill[] getSkills() {
        return skills;
    }

    /**
     * Sort an array of skills by their in-game order (reading left to right).
     *
     * @param skills Skills to sort
     */
    public static void sortSkills(Skill[] skills) {
        Arrays.sort(skills, Comparator.comparing(Skill::getName));
    }

    /**
     * Get the total cumulative XP
     *
     * @return Total XP
     */
    public long getTotalXp() {
        return total.getXp();
    }

    /**
     * Get the total level skill
     *
     * @return Total level skill
     */
    public Skill getTotalLevel() {
        return total;
    }

    /**
     * Get the total XP the player would have if all skills were maxed.
     *
     * @param virtual Use virtual max
     * @return Total maxed XP
     */
    public long getXpAtMax(boolean virtual) {
        return skills.length * (virtual ? Skill.MAX_XP : Skill.MAX_LEVEL_XP);
    }

    /**
     * Get the total number of maxed skills that the player has.
     * This is either the number of skills with 200m XP, or the number of skills at level 99.
     *
     * @param virtual Use virtual max
     * @return Number of maxed skills
     */
    public int getTotalMaxedSkills(boolean virtual) {
        return virtual ? totalMaxedVirtualSkills : totalMaxedSkills;
    }

    /**
     * Get the XP towards max that the player has. This is not their total XP, but the sum of their XP in every skill
     * capped at level 99. E.g if a player has 99 attack with 15 million XP, only the XP required to be level 99
     * is counted.
     *
     * @return XP towards max
     */
    public long getXpTowardsMax() {
        return xpTowardsMax;
    }
}