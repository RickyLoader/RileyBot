package COD.PlayerStats;

import COD.Assets.Killstreak;
import COD.Assets.Ratio;

/**
 * Player killstreak stats
 */
public class KillstreakStats extends AssetStats<Killstreak> {
    private final Ratio statUse;

    /**
     * Create player stats for the given killstreak
     *
     * @param killstreak   Killstreak that stats pertain to
     * @param statQuantity Quantity of unique killstreak stat the player has earned e.g kills/assists/.
     * @param uses         Total number of uses of the killstreak by the player
     */
    public KillstreakStats(Killstreak killstreak, int statQuantity, int uses) {
        super(killstreak);
        this.statUse = new Ratio(statQuantity, uses);
    }

    /**
     * Get the formatted average stat per use of the killstreak e.g "Avg: 5.30"
     *
     * @return Average stat per use
     */
    public String formatAverageStatQuantity() {
        return "Avg: " + statUse.formatRatio(statUse.getRatio());
    }

    /**
     * Get the quantity of the unique killstreak stat
     *
     * @return Quantity of stat
     */
    public int getStatQuantity() {
        return statUse.getNumerator();
    }

    /**
     * Get the number of uses
     *
     * @return Killstreak uses
     */
    public int getUses() {
        return statUse.getDenominator();
    }

    /**
     * Get the uses in a formatted String e.g "Quantity: 1,234"
     *
     * @return Formatted uses
     */
    public String formatUses() {
        return "Uses: " + statUse.formatDenominator();
    }

    /**
     * Get the stat quantity in a formatted String e.g "Assists: 1,234"
     *
     * @return Formatted stat quantity
     */
    public String formatStatQuantity() {
        if(!getAsset().hasExtraStat()) {
            return "No extra stat!";
        }
        return getAsset().getStatName() + ": " + statUse.formatNumerator();
    }

    @Override
    public int getSortValue() {
        return getUses();
    }
}
