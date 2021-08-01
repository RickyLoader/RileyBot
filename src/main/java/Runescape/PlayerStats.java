package Runescape;

import Bot.ResourceHandler;

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
    private final Skill total, virtualTotal;
    private final int combatLevel;

    // Value given to a players skill ranks/boss kills/etc that are not ranked (not in top 2 million players)
    public static final int UNRANKED = -1;

    public enum ACCOUNT {
        NORMAL, IRON, ULTIMATE, LEAGUE, HARDCORE
    }

    /**
     * Create player stats
     *
     * @param name   Player name
     * @param url    URL to hiscores CSV
     * @param skills Array of skill data
     * @param clues  Array of clue data
     * @param type   Account type
     */
    public PlayerStats(String name, String url, Skill[] skills, Clue[] clues, ACCOUNT type) {
        this.name = name;
        this.url = url;
        this.skills = skills;
        this.clues = clues;
        this.type = type;
        this.skillMap = new HashMap<>();
        for(Skill skill : skills) {
            skillMap.put(skill.getName(), skill);
        }
        this.clueMap = new HashMap<>();
        for(Clue clue : clues) {
            clueMap.put(clue.getType(), clue);
        }
        this.total = getSkill(Skill.SKILL_NAME.OVERALL);
        this.virtualTotal = calculateVirtualTotal(total, skills);
        this.combatLevel = calculateCombatLevel();
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
     * Calculate the virtual total level
     *
     * @param skills       Skills
     * @param currentTotal Current total level
     * @return Virtual total level
     */
    private Skill calculateVirtualTotal(Skill currentTotal, Skill[] skills) {
        int xp = 0;
        int level = 0;
        for(Skill skill : skills) {
            if(skill.getName() == Skill.SKILL_NAME.OVERALL) {
                continue;
            }
            xp += skill.getXp();
            level += skill.getVirtualLevel();
        }
        return new Skill(
                Skill.SKILL_NAME.VIRTUAL_TOTAL_LEVEL,
                currentTotal.getRank(),
                level,
                xp
        );
    }

    /**
     * Get the skill with the given name
     *
     * @param name Skill name
     * @return Skill from given name
     */
    public Skill getSkill(Skill.SKILL_NAME name) {
        return skillMap.get(name);
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
     * Get the image path for an account type
     *
     * @param type Account type
     * @return Image path
     */
    public static String getAccountTypeImagePath(ACCOUNT type) {
        String res = ResourceHandler.RUNESCAPE_BASE_PATH + "Accounts/";
        switch(type) {
            case IRON:
                return res + "iron.png";
            case HARDCORE:
                return res + "hardcore.png";
            case ULTIMATE:
                return res + "ultimate.png";
            case LEAGUE:
                return res + "league.png";
            default:
                return null;
        }
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
     * Check if the player account type has an image
     *
     * @return Account type has image
     */
    public boolean hasAccountTypeImage() {
        return getAccountTypeImagePath(type) != null;
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
     * Get a list of skill values in the in-game order
     *
     * @return Player skill values
     */
    public Skill[] getSkills() {
        return skills;
    }

    /**
     * Get the virtual total level skill
     *
     * @return Virtual total level skill
     */
    public Skill getVirtualTotalLevel() {
        return virtualTotal;
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
}