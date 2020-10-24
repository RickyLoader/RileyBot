package COD;

import COD.MWPlayer.Ratio;

/**
 * Tactical equipment
 */
public class Tactical extends Weapon {
    private final String statName;
    private final Ratio statUse;

    /**
     * Create a tactical equipment
     *
     * @param iwName   Infinity Ward name of weapon e.g "equip_gas_grenade"
     * @param name     Real name of weapon e.g "Gas Grenade"
     * @param res      Resource location
     * @param statName Stat name e.g "healed", "stunned"
     * @param statUse  Stat/Use Ratio of equipment
     */
    public Tactical(String iwName, String name, String res, String statName, Ratio statUse) {
        super(iwName, name, "tacticals", TYPE.TACTICAL, res);
        this.statName = statName;
        this.statUse = statUse;
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
     * Get the stat name for a tactical equipment
     *
     * @return Stat name
     */
    public String getStatName() {
        return statName;
    }

    /**
     * Return if the tactical has an extra stat
     *
     * @return Tactical has extra stat
     */
    public boolean hasExtraStat() {
        return statName != null;
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

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder("FAVOURITE TACTICAL:\n\n");
        builder.append("name: ").append(getName()).append("\n")
                .append("uses: ").append(getUses());
        if(hasExtraStat()) {
            builder.append("\n").append(statName).append(": ").append(getStat()).append("\n")
                    .append("Per use: ").append(statUse.formatRatio(statUse.getRatio()));
        }
        return builder.append("\n\n").toString();
    }
}
