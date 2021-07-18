package COD.PlayerStats;

import COD.Assets.Ratio;
import COD.Assets.Weapon;

/**
 * Lethal equipment player stats
 */
public class LethalStats extends WeaponStats {
    private final Ratio killUse;

    /**
     * Create player stats for the given lethal weapon
     *
     * @param weapon Lethal weapon that stats pertain to
     * @param kills  Number of kills with the lethal equipment
     * @param uses   Number of uses of the lethal equipment
     */
    public LethalStats(Weapon weapon, int kills, int uses) {
        super(weapon);
        this.killUse = new Ratio(kills, uses);
    }

    /**
     * Get the uses of the lethal equipment
     *
     * @return Uses
     */
    public int getUses() {
        return killUse.getDenominator();
    }

    /**
     * Get the kill/use ratio
     *
     * @return Kill/use ratio
     */
    public String getKillUse() {
        return killUse.formatRatio(killUse.getRatio());
    }

    /**
     * Get number of kills
     *
     * @return Kills
     */
    public int getKills() {
        return killUse.getNumerator();
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
     * Get the uses in a formatted String
     *
     * @return Formatted String uses
     */
    public String formatUses() {
        return killUse.formatDenominator();
    }

    @Override
    public int getSortValue() {
        return getUses() == 0 ? getKills() : getUses(); // Tracker doesn't give uses, sort by kills
    }
}
