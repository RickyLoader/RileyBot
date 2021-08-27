package Runescape.Stats;

import static Runescape.Stats.Skill.SKILL_NAME.*;

public class RS3PlayerStats extends PlayerStats {
    private RuneMetrics runeMetrics;
    private Clan clan;
    private HCIMStatus hcimStatus;

    /**
     * Create RS3 player stats
     *
     * @param name   Player name
     * @param url    URL to hiscores CSV
     * @param skills Array of player skills (excluding total level)
     * @param clues  Array of clue data
     * @param total  Total level
     * @param type   Account type
     */
    public RS3PlayerStats(String name, String url, Skill[] skills, Clue[] clues, TotalLevel total, ACCOUNT type) {
        super(name, url, skills, clues, total, type);
    }

    /**
     * Set the clan that the player is a member of
     *
     * @param clan Player clan
     */
    public void setClan(Clan clan) {
        this.clan = clan;
    }

    /**
     * Set the player's RuneMetrics
     *
     * @param runeMetrics Player RuneMetrics
     */
    public void setRuneMetrics(RuneMetrics runeMetrics) {
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
     * Get the clan that the player belongs to
     *
     * @return Player clan
     */
    public Clan getClan() {
        return clan;
    }

    /**
     * Return whether the player belongs to a clan
     *
     * @return Player belongs to a clan
     */
    public boolean isClanMember() {
        return clan != null;
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
     * @see <a href="https://runescape.wiki/w/Combat_level">Formula on Runescape Wiki</a>
     */
    @Override
    public int calculateCombatLevel() {
        int multiplier = Math.max(
                (getSkill(ATTACK).getLevel() + getSkill(STRENGTH).getLevel()),
                Math.max((2 * getSkill(MAGIC).getLevel()), (2 * getSkill(RANGED).getLevel()))
        );
        return (int) (((13d / 10d) * multiplier + getSkill(DEFENCE).getLevel() + getSkill(HITPOINTS).getLevel()
                + (getSkill(PRAYER).getLevel() / 2d) + (getSkill(SUMMONING).getLevel() / 2d)) / 4);
    }
}
