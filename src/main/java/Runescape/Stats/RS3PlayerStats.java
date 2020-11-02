package Runescape.Stats;

import Runescape.PlayerStats;
import Runescape.Skill;

public class RS3PlayerStats extends PlayerStats {
    private final int combatLevel;
    private final RuneMetrics runeMetrics;
    private HCIMStatus hcimStatus;

    /**
     * Create RS3 player stats
     *
     * @param name        Player name
     * @param url         URL to hiscores CSV
     * @param skills      Array of skill data
     * @param clues       Array of clue data
     * @param runeMetrics RuneMetrics data
     * @param type        Account type
     */
    public RS3PlayerStats(String name, String url, Skill[] skills, String[] clues, RuneMetrics runeMetrics, ACCOUNT type) {
        super(name, url, skills, clues, type);
        this.combatLevel = calculateCombatLevel(
                getSkill(Skill.SKILL_NAME.ATTACK),
                getSkill(Skill.SKILL_NAME.STRENGTH),
                getSkill(Skill.SKILL_NAME.MAGIC),
                getSkill(Skill.SKILL_NAME.RANGED),
                getSkill(Skill.SKILL_NAME.DEFENCE),
                getSkill(Skill.SKILL_NAME.HITPOINTS),
                getSkill(Skill.SKILL_NAME.PRAYER),
                getSkill(Skill.SKILL_NAME.SUMMONING)
        );
        this.runeMetrics = runeMetrics;
    }

    /**
     * Set the HCIM status of the account
     *
     * @param hcimStatus HCIM status
     */
    public void setHcimStatus(HCIMStatus hcimStatus) {
        this.hcimStatus = hcimStatus;
    }

    /**
     * Get the HCIM status
     *
     * @return HCIM status
     */
    public HCIMStatus getHcimStatus() {
        return hcimStatus;
    }

    /**
     * Check if the account has a HCIM status
     *
     * @return HCIM status exists
     */
    public boolean hasHCIMStatus() {
        return hcimStatus != null;
    }

    /**
     * Return presence of RuneMetrics data
     *
     * @return RuneMetrics data exists
     */
    public boolean hasRuneMetrics() {
        return runeMetrics != null;
    }

    /**
     * Get the RuneMetrics data
     *
     * @return RuneMetrics data
     */
    public RuneMetrics getRuneMetrics() {
        return runeMetrics;
    }

    /**
     * Get the player combat level
     *
     * @return Combat level
     */
    public int getCombatLevel() {
        return combatLevel;
    }

    /**
     * Calculate the player's combat level
     *
     * @param attack    Attack skill
     * @param defence   Defence skill
     * @param hitpoints Hitpoints skill
     * @param magic     Magic skill
     * @param prayer    Prayer skill
     * @param ranged    Ranged skill
     * @param strength  Strength skill
     * @param summoning Summoning skill
     * @return Combat level
     */
    private int calculateCombatLevel(Skill attack, Skill strength, Skill magic, Skill ranged, Skill defence, Skill hitpoints, Skill prayer, Skill summoning) {
        int multiplier = Math.max(
                (attack.getLevel() + strength.getLevel()),
                Math.max((2 * magic.getLevel()), (2 * ranged.getLevel()))
        );
        return (int) (((13d / 10d) * multiplier + defence.getLevel() + hitpoints.getLevel() + (prayer.getLevel() / 2d) + (summoning.getLevel() / 2d)) / 4);
    }
}
