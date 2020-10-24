package COD;

import COD.MWPlayer.Ratio;

/**
 * Lethal equipment
 */
public class Lethal extends Weapon {

    private final Ratio killUse;

    /**
     * Create a lethal equipment
     *
     * @param iwName  Infinity Ward name of weapon e.g "equip_frag"
     * @param name    Real name of weapon e.g "Frag Grenade"
     * @param res     Resource location
     * @param killUse Kill/Use ratio of equipment
     */
    public Lethal(String iwName, String name, String res, Ratio killUse) {
        super(iwName, name, "lethals", TYPE.LETHAL, res);
        this.killUse = killUse;
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
        return getUses();
    }
}
