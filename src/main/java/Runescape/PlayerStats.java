package Runescape;

import java.text.DecimalFormat;
import java.util.HashMap;

/**
 * Hold Runescape player hiscores entry
 */
public class PlayerStats {
    private final String name, url;
    private final String[] clues;
    private final Skill[] skills;
    private final HashMap<Skill.SKILL_NAME, Skill> skillMap;
    private final ACCOUNT type;
    private final Skill total, virtualTotal;
    public final DecimalFormat commaFormat = new DecimalFormat("#,###");

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
    public PlayerStats(String name, String url, Skill[] skills, String[] clues, ACCOUNT type) {
        this.name = name;
        this.url = url;
        this.skills = skills;
        this.clues = clues;
        this.type = type;
        this.skillMap = new HashMap<>();
        for(Skill skill : skills) {
            skillMap.put(skill.getName(), skill);
        }
        this.total = getSkill(Skill.SKILL_NAME.TOTAL_LEVEL);
        this.virtualTotal = calculateVirtualTotal(total, skills);
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
            if(skill.getName() == Skill.SKILL_NAME.TOTAL_LEVEL) {
                continue;
            }
            xp += skill.getXP();
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
        return skillMap.getOrDefault(name, null);
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
     * Get the player hiscores rank relative to the account type.
     * Formatted with comma separator.
     *
     * @return Player hiscores rank
     */
    public String getFormattedRank() {
        return commaFormat.format(total.getRank());
    }

    /**
     * Get a list of clue scroll completions in order of Beginner, Easy, Medium, Hard, Elite, Master
     *
     * @return Clue scroll completions
     */
    public String[] getClues() {
        return clues;
    }

    /**
     * Get the image path for an account type
     *
     * @param type Account type
     * @return Image path
     */
    public static String getAccountTypeImagePath(ACCOUNT type) {
        String res = "/Runescape/Accounts/";
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
     * Get the virtual total level
     *
     * @return Virtual total level
     */
    public int getVirtualTotalLevel() {
        return virtualTotal.getLevel();
    }

    /**
     * Get the total level
     *
     * @return Total level
     */
    public int getTotalLevel() {
        return total.getLevel();
    }

    /**
     * Get the total cumulative XP
     *
     * @return Total XP
     */
    public long getTotalXP() {
        return total.getXP();
    }

    /**
     * Get the player rank
     *
     * @return Player rank
     */
    public long getRank() {
        return total.getRank();
    }
}