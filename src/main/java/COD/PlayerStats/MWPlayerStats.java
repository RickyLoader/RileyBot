package COD.PlayerStats;

import COD.API.CODStatsManager.PLATFORM;

/**
 * Modern Warfare player stats
 */
public class MWPlayerStats extends CODPlayerStats<MWPlayerAssetStats> {

    /**
     * Create a Modern Warfare player's stats
     *
     * @param name       Player name
     * @param platform   Player platform
     * @param assetStats Player's stats with weapons/equipment/killstreaks etc
     * @param basicStats Basic player stats - ratios etc
     */
    public MWPlayerStats(String name, PLATFORM platform, MWPlayerAssetStats assetStats, PlayerBasicStats basicStats) {
        super(name, platform, assetStats, basicStats);
    }
}
