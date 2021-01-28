package COD.PlayerStats;

import COD.Assets.Ratio;
import COD.Assets.Weapon;

/**
 * Tactical equipment player stats
 */
public class TacticalStats extends WeaponStats {
    private final Ratio statUse;

    /**
     * Create player stats for the given tactical weapon
     *
     * @param weapon Tactical weapon that stats pertain to
     * @param stat   Number of unique stat for this tactical equipment
     * @param uses   Number of uses of the tactical equipment
     */
    public TacticalStats(Weapon weapon, int stat, int uses) {
        super(weapon);
        this.statUse = new Ratio(stat, uses);
    }

    /**
     * Get the stat value in a formatted String
     *
     * @return Formatted String stat
     */
    public String formatStat() {
        return statUse.formatNumerator();
    }

    /**
     * Get the uses in a formatted String
     *
     * @return Formatted String uses
     */
    public String formatUses() {
        return statUse.formatDenominator();
    }

    /**
     * Get the stat/use ratio
     *
     * @return stat/use ratio
     */
    public String getStatUse() {
        return statUse.formatRatio(statUse.getRatio());
    }

    /**
     * Get a tactical equipment's property stat
     *
     * @return Quantity of equipment's stat
     */
    public int getStat() {
        return statUse.getNumerator();
    }

    /**
     * Get the uses of the tactical equipment
     *
     * @return Uses
     */
    public int getUses() {
        return statUse.getDenominator();
    }

    @Override
    public int getSortValue() {
        return getUses();
    }
}
