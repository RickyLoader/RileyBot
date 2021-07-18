package COD.PlayerStats;

import COD.Assets.CODAsset;

import java.util.ArrayList;

/**
 * Modern Warfare player stats with COD assets
 */
public class MWPlayerAssetStats extends PlayerAssetStats {
    private final ArrayList<FieldUpgradeStats> fieldUpgradeStats;
    private final ArrayList<CommendationStats> commendationStats;

    /**
     * Create the Modern Warfare player asset stats
     *
     * @param weaponStats       Weapon stats
     * @param equipmentStats    Equipment stats
     * @param killstreakStats   Killstreak stats
     * @param commendationStats Commendation stats
     * @param fieldUpgradeStats Field upgrade stats
     */
    public MWPlayerAssetStats(PlayerWeaponStats weaponStats, PlayerEquipmentStats equipmentStats, ArrayList<KillstreakStats> killstreakStats, ArrayList<FieldUpgradeStats> fieldUpgradeStats, ArrayList<CommendationStats> commendationStats) {
        super(weaponStats, equipmentStats, killstreakStats);
        this.fieldUpgradeStats = fieldUpgradeStats;
        this.commendationStats = commendationStats;
    }

    /**
     * Get a list of player commendation stats
     *
     * @return Player commendation stats
     */
    public ArrayList<CommendationStats> getCommendationStats() {
        return commendationStats;
    }

    /**
     * Get a list of player field upgrade stats
     *
     * @return Field upgrade stats
     */
    public ArrayList<FieldUpgradeStats> getFieldUpgradeStats() {
        return fieldUpgradeStats;
    }

    /**
     * Override to also add field upgrade stats & commendation stats
     *
     * @return List of all player asset stats
     */
    @Override
    public ArrayList<AssetStats<? extends CODAsset>> getAllStats() {
        ArrayList<AssetStats<? extends CODAsset>> assetStats = super.getAllStats();
        assetStats.addAll(commendationStats);
        assetStats.addAll(fieldUpgradeStats);
        return assetStats;
    }
}
