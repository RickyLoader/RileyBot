package COD.PlayerStats;

import COD.Assets.Commendation;

import java.text.DecimalFormat;

/**
 * Player commendation stats
 */
public class CommendationStats extends AssetStats<Commendation> {
    private final int quantity;

    /**
     * Create player stats for the given commendation
     *
     * @param commendation Commendation that stats pertain to
     * @param quantity     Quantity of commendation that player has achieved
     */
    public CommendationStats(Commendation commendation, int quantity) {
        super(commendation);
        this.quantity = quantity;
    }

    /**
     * Get the quantity of the commendation that the player has achieved
     *
     * @return Total quantity of commendation achieved
     */
    public int getQuantity() {
        return quantity;
    }

    /**
     * Format the quantity from 1 to x1
     *
     * @return Formatted quantity
     */
    public String formatQuantity() {
        return "x" + new DecimalFormat("#,###").format(quantity);
    }

    @Override
    public int getSortValue() {
        return getQuantity();
    }
}
