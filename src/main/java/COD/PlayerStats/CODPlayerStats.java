package COD.PlayerStats;

import COD.API.CODStatsManager.PLATFORM;

/**
 * Hold COD player stats
 */
public abstract class CODPlayerStats<T extends PlayerAssetStats> {
    private final PLATFORM platform;
    private final String name;
    private final T assetStats;
    private final PlayerBasicStats basicStats;

    /**
     * Create a COD player's stats
     *
     * @param name       Player name
     * @param platform   Player platform
     * @param assetStats Player's stats with weapons/equipment/killstreaks etc
     * @param basicStats Basic player stats - ratios etc
     */
    public CODPlayerStats(String name, PLATFORM platform, T assetStats, PlayerBasicStats basicStats) {
        this.name = name;
        this.platform = platform;
        this.assetStats = assetStats;
        this.basicStats = basicStats;
    }

    /**
     * Get player stats with weapons/equipment/killstreaks etc
     *
     * @return Player asset stats
     */
    public T getAssetStats() {
        return assetStats;
    }

    /**
     * Get basic player stats - ratios etc
     *
     * @return Basic player stats
     */
    public PlayerBasicStats getBasicStats() {
        return basicStats;
    }

    /**
     * Get the player's name
     *
     * @return Player name
     */
    public String getName() {
        return name;
    }

    /**
     * Get the player's platform
     *
     * @return Player platform
     */
    public PLATFORM getPlatform() {
        return platform;
    }
}
