package COD.PlayerStats;

import COD.Assets.FieldUpgrade;
import COD.Assets.Ratio;
import org.jetbrains.annotations.NotNull;

/**
 * Player field upgrade stats
 */
public class FieldUpgradeStats implements Comparable<FieldUpgradeStats> {
    private final FieldUpgrade fieldUpgrade;
    private final Ratio killUse;
    private final int propertyStat;


    /**
     * Create player stats for the given field upgrade
     *
     * @param fieldUpgrade Field upgrade that stats pertain to
     * @param kills        Total number of kills using the field upgrade by the player
     * @param uses         Total number of uses of the field upgrade by the player
     * @param propertyStat Quantity of unique field upgrade property that the player has earned e.g Longest streak
     */
    public FieldUpgradeStats(FieldUpgrade fieldUpgrade, int kills, int uses, int propertyStat) {
        this.fieldUpgrade = fieldUpgrade;
        this.killUse = new Ratio(kills, uses);
        this.propertyStat = propertyStat;
    }

    /**
     * Get the quantity of the unique field upgrade property
     * that the player has earned
     *
     * @return Quantity of unique property
     */
    public int getPropertyStat() {
        return propertyStat;
    }

    /**
     * Return whether player has any kills with the field upgrade
     *
     * @return Field upgrade has kills
     */
    public boolean hasKills() {
        return getKills() > 0;
    }

    /**
     * Get the number of player kills with the field upgrade
     *
     * @return Field upgrade kills
     */
    public int getKills() {
        return killUse.getNumerator();
    }

    /**
     * Get the uses in a formatted String
     *
     * @return Formatted String uses
     */
    public String formatUses() {
        return killUse.formatDenominator();
    }

    /**
     * Get the kills in a formatted String
     *
     * @return Formatted String kills
     */
    public String formatKills() {
        return killUse.formatNumerator();
    }

    /**
     * Get the number of uses
     *
     * @return Number of uses
     */
    public int getUses() {
        return killUse.getDenominator();
    }

    /**
     * Get the formatted kill/use ratio
     *
     * @return kill/use ratio
     */
    public String getKillUseRatio() {
        return killUse.formatRatio(killUse.getRatio());
    }

    /**
     * Get the field upgrade that the player stats pertain to
     *
     * @return Field upgrade that player stats pertain to
     */
    public FieldUpgrade getFieldUpgrade() {
        return fieldUpgrade;
    }

    @Override
    public int compareTo(@NotNull FieldUpgradeStats o) {
        return o.getUses() - getUses();
    }
}
