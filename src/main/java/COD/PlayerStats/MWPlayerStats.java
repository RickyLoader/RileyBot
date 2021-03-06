package COD.PlayerStats;

import COD.Assets.*;
import COD.CODAPI;
import COD.MWManager;
import Command.Structure.CODLookupCommand.PLATFORM;
import org.json.JSONObject;

import java.util.*;
import java.util.stream.Collectors;

public class MWPlayerStats {
    private final String name, data;
    private final PLATFORM platform;
    private int streak;
    private String status;
    private Ratio wl, kd;
    private StandardWeaponStats primaryStats, secondaryStats;
    private LethalStats lethalStats;
    private TacticalStats tacticalStats;
    private FieldUpgradeStats fieldUpgradeStats;
    private ArrayList<KillstreakStats> killstreakStats;
    private ArrayList<CommendationStats> commendationStats;
    private ArrayList<AssetStats<? extends CODAsset>> assetStats;

    /**
     * Create a Modern Warfare player's stats
     *
     * @param name     Player name
     * @param platform Player platform
     */
    public MWPlayerStats(String name, PLATFORM platform) {
        this.name = name;
        this.platform = platform;
        this.data = fetchPlayerData();
    }

    /**
     * Get player name
     *
     * @return Player name
     */
    public String getName() {
        return name;
    }

    /**
     * Get the top 5 player killstreak stats based on usage
     *
     * @return Top 5 player killstreak stats
     */
    public ArrayList<KillstreakStats> getKillstreakStats() {
        return killstreakStats;
    }

    /**
     * Get the player's favourite primary weapon stats
     *
     * @return Primary weapon stats
     */
    public StandardWeaponStats getPrimaryStats() {
        return primaryStats;
    }

    /**
     * Get the player's favourite secondary weapon stats
     *
     * @return Secondary weapon stats
     */
    public StandardWeaponStats getSecondaryStats() {
        return secondaryStats;
    }

    /**
     * Get player commendation stats
     *
     * @return Player commendations
     */
    public ArrayList<CommendationStats> getCommendationStats() {
        return commendationStats;
    }

    /**
     * Get the player's favourite lethal equipment stats
     *
     * @return Lethal equipment stats
     */
    public LethalStats getLethalStats() {
        return lethalStats;
    }

    /**
     * Get the player's favourite tactical equipment stats
     *
     * @return Tactical equipment stats
     */
    public TacticalStats getTacticalStats() {
        return tacticalStats;
    }

    /**
     * Get the player's favourite field upgrade stats
     *
     * @return Field upgrade stats
     */
    public FieldUpgradeStats getSuperStats() {
        return fieldUpgradeStats;
    }

    /**
     * Get formatted kill/death ratio
     *
     * @return K/D String
     */
    public String getKD() {
        return kd.formatRatio(kd.getRatio());
    }

    /**
     * Get formatted win/loss ratio
     *
     * @return W/L String
     */
    public String getWinLoss() {
        return wl.formatRatio(wl.getRatio());
    }

    /**
     * Get total wins
     *
     * @return Total wins
     */
    public String getWins() {
        return wl.formatNumerator();
    }

    /**
     * Get total losses
     *
     * @return Total losses
     */
    public String getLosses() {
        return wl.formatDenominator();
    }

    /**
     * Get longest kill streak
     *
     * @return Longest kill streak
     */
    public int getLongestKillStreak() {
        return streak;
    }

    /**
     * Check if the network request for player data was successful
     *
     * @return Boolean account exists
     */
    public boolean success() {
        return data != null;
    }

    /**
     * Get the status message from from the player data request
     *
     * @return Status message
     */
    public String getStatus() {
        return status;
    }

    /**
     * Fetch player data from the API
     *
     * @return JSON from API
     */
    private String fetchPlayerData() {
        String json = CODAPI.getMWStats(name, platform);

        if(json == null) {
            status = "Failed to communicate with API, try again later.";
            return null;
        }

        JSONObject data = new JSONObject(json);
        status = data.getString("status");

        if(!status.equals("success")) {
            return null;
        }

        JSONObject player = data.getJSONObject("data").getJSONObject("lifetime");
        JSONObject basic = player.getJSONObject("all").getJSONObject("properties");

        this.kd = new Ratio(basic.getInt("kills"), basic.getInt("deaths"));
        this.wl = new Ratio(basic.getInt("wins"), basic.getInt("losses"));
        this.streak = basic.getInt("recordKillStreak");

        JSONObject weapons = player.getJSONObject("itemData");
        JSONObject supers = weapons.getJSONObject("supers");
        weapons.remove("supers");

        ArrayList<WeaponStats> weaponStats = parseWeaponStats(weapons);
        this.primaryStats = (StandardWeaponStats) getFavouriteWeapon(weaponStats, Weapon.TYPE.PRIMARY);
        this.secondaryStats = (StandardWeaponStats) getFavouriteWeapon(weaponStats, Weapon.TYPE.SECONDARY);
        this.lethalStats = (LethalStats) getFavouriteWeapon(weaponStats, Weapon.TYPE.LETHAL);
        this.tacticalStats = (TacticalStats) getFavouriteWeapon(weaponStats, Weapon.TYPE.TACTICAL);

        ArrayList<FieldUpgradeStats> fieldUpgradeStats = parseFieldUpgradeStats(supers);
        this.fieldUpgradeStats = fieldUpgradeStats.get(0);

        JSONObject killstreaks = player.getJSONObject("scorestreakData");
        this.killstreakStats = parseKillstreakStats(
                killstreaks.getJSONObject("lethalScorestreakData"),
                killstreaks.getJSONObject("supportScorestreakData")
        );
        this.commendationStats = parseCommendationStats(
                player.getJSONObject("accoladeData").getJSONObject("properties")
        );

        this.assetStats = new ArrayList<>();
        assetStats.addAll(weaponStats);
        assetStats.addAll(fieldUpgradeStats);
        assetStats.addAll(commendationStats);
        assetStats.addAll(killstreakStats);
        return json;
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
        ArrayList<AssetStats<? extends CODAsset>> results = assetStats
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
     * Get the player's favourite weapon for the given weapon type
     *
     * @param stats List of player stats for each weapon
     * @param type  Desired weapon type to find player favourite
     * @return Player's favourite weapon for given weapon type
     */
    private WeaponStats getFavouriteWeapon(ArrayList<WeaponStats> stats, Weapon.TYPE type) {
        ArrayList<WeaponStats> filtered = stats
                .stream()
                .filter(weaponStats -> weaponStats.getAsset().getType() == type)
                .sorted()
                .collect(Collectors.toCollection(ArrayList::new));
        return filtered.get(0);
    }

    /**
     * Parse the player's commendation stats from the given commendation JSON
     *
     * @param commendations Commendation stats JSON
     * @return List of player commendation stats
     */
    private ArrayList<CommendationStats> parseCommendationStats(JSONObject commendations) {
        ArrayList<CommendationStats> commendationStats = new ArrayList<>();

        for(String commendationName : commendations.keySet()) {
            Commendation commendation = MWManager.getInstance().getCommendationByCodename(commendationName);
            if(commendation == null) {
                continue;
            }
            commendationStats.add(
                    new CommendationStats(
                            commendation,
                            commendations.getInt(commendationName)
                    )
            );
        }

        Collections.sort(commendationStats);
        return commendationStats;
    }

    /**
     * Parse the player's killstreak stats from the given commendation JSON
     *
     * @param lethalStreaks  Lethal killstreak stats JSON
     * @param supportStreaks Support killstreak stats JSON
     * @return List of player killstreak stats
     */
    private ArrayList<KillstreakStats> parseKillstreakStats(JSONObject lethalStreaks, JSONObject supportStreaks) {
        ArrayList<KillstreakStats> stats = new ArrayList<>();

        // Combine lethal and support streaks
        for(String lethalKey : lethalStreaks.keySet()) {
            supportStreaks.put(lethalKey, lethalStreaks.getJSONObject(lethalKey));
        }

        for(String name : supportStreaks.keySet()) {
            JSONObject killstreakData = supportStreaks.getJSONObject(name).getJSONObject("properties");
            Killstreak killstreak = MWManager.getInstance().getKillstreakByCodename(name);
            stats.add(
                    new KillstreakStats(
                            killstreak,
                            killstreakData.getInt("extraStat1"),
                            killstreakData.getInt("uses")
                    )
            );
        }
        Collections.sort(stats);
        return stats;
    }


    /**
     * Parse the player's field upgrade JSON in to a list of field upgrade stats
     *
     * @param superData Field upgrade JSON
     * @return List of field upgrade stats
     */
    private ArrayList<FieldUpgradeStats> parseFieldUpgradeStats(JSONObject superData) {
        ArrayList<FieldUpgradeStats> fieldUpgradeStats = new ArrayList<>();
        for(String superName : superData.keySet()) {
            // Counts Field Upgrade Pro (Running 2 field upgrades) as a field upgrade itself
            if(superName.equals("super_select")) {
                continue;
            }
            JSONObject fieldUpgradeData = superData.getJSONObject(superName).getJSONObject("properties");
            FieldUpgrade fieldUpgrade = MWManager.getInstance().getSuperByCodename(superName);
            if(fieldUpgrade == null) {
                continue;
            }
            fieldUpgradeStats.add(
                    new FieldUpgradeStats(
                            fieldUpgrade,
                            fieldUpgradeData.getInt("kills"),
                            fieldUpgradeData.getInt("uses"),
                            fieldUpgradeData.getInt("misc1")
                    )
            );
        }
        Collections.sort(fieldUpgradeStats);
        return fieldUpgradeStats;
    }

    /**
     * Parse the player's weapon stats for all weapons from the weapon JSON
     *
     * @param weapons JSON weapons from the API
     * @return List of player weapon stats
     */
    private ArrayList<WeaponStats> parseWeaponStats(JSONObject weapons) {
        ArrayList<WeaponStats> weaponStats = new ArrayList<>();
        for(String categoryName : weapons.keySet()) {
            JSONObject categoryData = weapons.getJSONObject(categoryName);
            Weapon.CATEGORY category = Weapon.CATEGORY.discernCategory(categoryName);
            for(String weaponName : categoryData.keySet()) {
                JSONObject weaponData = categoryData.getJSONObject(weaponName).getJSONObject("properties");
                Weapon weapon = MWManager.getInstance().getWeaponByCodename(weaponName, category);
                WeaponStats stats;

                switch(weapon.getType()) {
                    case LETHAL:
                        stats = new LethalStats(
                                weapon,
                                weaponData.getInt("kills"),
                                weaponData.getInt("uses")
                        );
                        break;
                    case TACTICAL:
                        stats = new TacticalStats(
                                (TacticalWeapon) weapon,
                                weaponData.getInt("extraStat1"),
                                weaponData.getInt("uses")
                        );
                        break;
                    default:
                        stats = new StandardWeaponStats.StandardWeaponStatsBuilder()
                                .setKillDeath(
                                        weaponData.getInt("kills"),
                                        weaponData.getInt("deaths")
                                )
                                .setAccuracy(
                                        weaponData.getInt("hits"),
                                        weaponData.getInt("shots")
                                )
                                .setHeadshots(
                                        weaponData.has("headshots")
                                                ? weaponData.getInt("headshots")
                                                : weaponData.getInt("headShots")
                                )
                                .setWeapon(weapon)
                                .build();
                        break;
                }
                weaponStats.add(stats);
            }
        }
        return weaponStats;
    }
}
