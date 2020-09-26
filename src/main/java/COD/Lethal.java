package COD;

import COD.CODPlayer.Ratio;

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
     * Get the uses of the tactical equipment
     *
     * @return Uses
     */
    public int getUses() {
        return killUse.getDenominator();
    }

    /**
     * Get number of kills
     *
     * @return Kills
     */
    public int getKills() {
        return killUse.getNumerator();
    }

    @Override
    public int getSortValue() {
        return getUses();
    }
}
