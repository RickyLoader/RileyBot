package COD.PlayerStats;

import COD.Assets.Commendation;
import org.jetbrains.annotations.NotNull;

import java.text.DecimalFormat;

/**
 * Player commendation stats
 */
public class CommendationStats implements Comparable<CommendationStats> {
    private final Commendation commendation;
    private final int quantity;

    /**
     * Create player stats for the given commendation
     *
     * @param commendation Commendation that stats pertain to
     * @param quantity     Quantity of commendation that player has achieved
     */
    public CommendationStats(Commendation commendation, int quantity) {
        this.commendation = commendation;
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
     * Get the commendation that the player stats pertain to
     *
     * @return Commendation
     */
    public Commendation getCommendation() {
        return commendation;
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
    public int compareTo(@NotNull CommendationStats o) {
        return o.getQuantity() - getQuantity();
    }
}
