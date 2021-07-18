package COD.PlayerStats;

import COD.Assets.CODAsset;

import java.util.ArrayList;
import java.util.stream.Collectors;

/**
 * COD player stats with COD assets
 */
public class PlayerAssetStats {
    private final PlayerWeaponStats weaponStats;
    private final PlayerEquipmentStats equipmentStats;
    private final ArrayList<KillstreakStats> killstreakStats;

    /**
     * Create the player asset stats
     *
     * @param weaponStats     Weapon stats
     * @param equipmentStats  Equipment stats
     * @param killstreakStats Killstreak stats
     */
    public PlayerAssetStats(PlayerWeaponStats weaponStats, PlayerEquipmentStats equipmentStats, ArrayList<KillstreakStats> killstreakStats) {
        this.weaponStats = weaponStats;
        this.equipmentStats = equipmentStats;
        this.killstreakStats = killstreakStats;
    }

    /**
     * Get the player's weapon stats
     *
     * @return Player weapon stats
     */
    public PlayerWeaponStats getWeaponStats() {
        return weaponStats;
    }

    /**
     * Get the player's equipment stats
     *
     * @return Player equipment stats
     */
    public PlayerEquipmentStats getEquipmentStats() {
        return equipmentStats;
    }

    /**
     * Get a list of player killstreak stats
     *
     * @return Player killstreak stats
     */
    public ArrayList<KillstreakStats> getKillstreakStats() {
        return killstreakStats;
    }

    /**
     * Get a list of all player stats for all assets - killstreaks/weapons/etc
     *
     * @return List of all player asset stats
     */
    public ArrayList<AssetStats<? extends CODAsset>> getAllStats() {
        ArrayList<AssetStats<? extends CODAsset>> assetStats = new ArrayList<>();
        assetStats.addAll(weaponStats.getPrimaryWeaponStats());
        assetStats.addAll(weaponStats.getSecondaryWeaponStats());
        assetStats.addAll(equipmentStats.getLethalStats());
        assetStats.addAll(equipmentStats.getTacticalStats());
        assetStats.addAll(killstreakStats);
        return assetStats;
    }


    /**
     * Get a list of asset stats containing for the given query.
     * If a singular matching asset stats is found, the returned list will contain only the match,
     * otherwise the list will contain all asset stats matching the query.
     *
     * @param query Asset query (Passed to asset to check for a match, assets may compare to different values)
     * @return List of matching asset stats
     */
    public ArrayList<AssetStats<? extends CODAsset>> getAssetStatsByName(String query) {
        ArrayList<AssetStats<? extends CODAsset>> results = getAllStats()
                .stream()
                .filter(assetStats -> assetStats.getAsset().isPartialMatch(query))
                .collect(Collectors.toCollection(ArrayList::new));

        ArrayList<AssetStats<? extends CODAsset>> matching = results
                .stream()
                .filter(assetStats -> assetStats.getAsset().isExactMatch(query))
                .collect(Collectors.toCollection(ArrayList::new));

        return matching.size() == 1 ? matching : results;
    }


    /**
     * Get a player's favourite asset from the given list of asset stats.
     * The sort value is based on the asset - e.g kills for weapons, uses for killstreaks, etc.
     *
     * @param stats List of player asset stats
     * @return Player's favourite asset from given list
     */
    public static AssetStats<? extends CODAsset> getFavouriteAsset(ArrayList<? extends AssetStats<? extends CODAsset>> stats) {
        ArrayList<? extends AssetStats<? extends CODAsset>> filtered = stats
                .stream()
                .sorted()
                .collect(Collectors.toCollection(ArrayList::new));
        return filtered.get(0);
    }
}
