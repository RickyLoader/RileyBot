package COD.PlayerStats;

import COD.Assets.CODAsset;
import org.jetbrains.annotations.NotNull;

// Stats for a COD item
public abstract class AssetStats<T extends CODAsset> implements Comparable<AssetStats<T>> {
    private final T asset;

    /**
     * Create the COD asset stats
     *
     * @param asset COD asset
     */
    public AssetStats(T asset) {
        this.asset = asset;
    }

    /**
     * Get the COD asset
     *
     * @return COD asset
     */
    public T getAsset() {
        return asset;
    }

    /**
     * Get the value used in sorting
     * Tactical - uses
     * Standard, Lethal - kills
     *
     * @return Sort value
     */
    public abstract int getSortValue();

    @Override
    public int compareTo(@NotNull AssetStats<T> o) {
        return o.getSortValue() - getSortValue();
    }
}
