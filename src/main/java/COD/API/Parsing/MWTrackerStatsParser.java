package COD.API.Parsing;

import COD.API.CODStatsManager.PLATFORM;
import COD.API.MWManager;
import COD.Assets.*;
import COD.PlayerStats.*;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Parsing Modern Warfare stats response from cod.tracker.gg
 */
public class MWTrackerStatsParser extends MWStatsParser {
    private static final String
            VALUE_KEY = "value",
            DATA_KEY = "data",
            STATS_KEY = "stats",
            KILLS_KEY = "kills",
            DEATHS_KEY = "deaths",
            USES_KEY = "uses",
            KEY = "key",
            LOWER_HEADSHOTS_KEY = "headshots",
            ATTRIBUTES_KEY = "attributes";

    /**
     * Create the tracker stats parser
     *
     * @param manager Modern Warfare manager
     */
    public MWTrackerStatsParser(MWManager manager) {
        super(manager);
    }

    /**
     * Parse a Modern Warfare player's stats response from cod.tracker.gg
     *
     * @param name           Player name - adhering to platform rules
     * @param platform       Player platform
     * @param basicData      Basic player JSON
     * @param weaponData     Player weapon JSON
     * @param killstreakData Player killstreak JSON
     * @return Player stats
     */
    public MWPlayerStats parseTrackerResponse(String name, PLATFORM platform, JSONObject basicData, JSONObject weaponData, JSONObject killstreakData) {
        JSONArray mainData = basicData.getJSONObject("data").getJSONArray("segments");
        JSONObject playerData = mainData.getJSONObject(0).getJSONObject(STATS_KEY);
        JSONObject commendationsData = mainData.getJSONObject(1).getJSONObject(STATS_KEY);

        return new MWPlayerStats(
                name,
                platform,
                parseAssetStats(weaponData, killstreakData, commendationsData),
                parseBasicStats(playerData)
        );
    }

    /**
     * Parse the player asset stats from the given JSON data.
     *
     * @param weaponData        Player weapon/field upgrade/equipment stats data
     * @param killstreakData    Player killstreak stats data
     * @param commendationsData Player commendations data
     * @return Player asset stats
     */
    private MWPlayerAssetStats parseAssetStats(JSONObject weaponData, JSONObject killstreakData, JSONObject commendationsData) {
        ArrayList<FieldUpgradeStats> fieldUpgradeStats = new ArrayList<>();
        PlayerWeaponStats weaponStats = new PlayerWeaponStats();
        PlayerEquipmentStats equipmentStats = new PlayerEquipmentStats();

        JSONArray assetList = weaponData.getJSONArray(DATA_KEY);

        for(int i = 0; i < assetList.length(); i++) {
            JSONObject assetData = assetList.getJSONObject(i);
            JSONObject attributes = assetData.getJSONObject(ATTRIBUTES_KEY);
            JSONObject stats = assetData.getJSONObject(STATS_KEY);

            // Either a weapon category eg - "weapon_assault_rifle" or "supers" (field upgrades)
            final String categoryKey = attributes.getString("categoryKey");

            // Asset codename - e.g "iw8_ar_mike4" or "super_ammo_drop"
            final String codename = attributes.getString(KEY);

            // Parse field upgrade
            if(categoryKey.equals("supers")) {
                FieldUpgrade fieldUpgrade = getManager().getSuperByCodename(codename);

                // Unable to locate super of the given codename
                if(fieldUpgrade == null) {
                    continue;
                }
                fieldUpgradeStats.add(
                        new FieldUpgradeStats(
                                fieldUpgrade,
                                getIntValue(stats, KILLS_KEY),
                                0, // They do not provide the uses value
                                0 // They do not provide the misc value - e.g longest streak w/ dead silence
                        )
                );
            }
            // Parse weapons/equipment
            else {
                Weapon.CATEGORY category = Weapon.CATEGORY.discernCategory(categoryKey);
                Weapon weapon = getManager().getWeaponByCodename(codename, category);

                // Parse weapon based on type
                switch(weapon.getType()) {

                    // They don't provide tacticals
                    case TACTICAL:
                    case UNKNOWN:
                        continue;

                    case LETHAL:
                        equipmentStats.addLethalStats(
                                new LethalStats(
                                        weapon,
                                        getIntValue(stats, KILLS_KEY),
                                        0 // They do not provide the uses value
                                )
                        );
                        break;

                    // Primary/secondary weapon
                    default:
                        weaponStats.addWeaponStats(
                                new StandardWeaponStats.StandardWeaponStatsBuilder()
                                        .setKillDeath(
                                                getIntValue(stats, KILLS_KEY),
                                                getIntValue(stats, DEATHS_KEY)
                                        )
                                        .setAccuracy(
                                                getIntValue(stats, "hits"),
                                                getIntValue(stats, "shots")
                                        )
                                        .setHeadshots(
                                                getIntValue(
                                                        stats,
                                                        stats.has(LOWER_HEADSHOTS_KEY)
                                                                ? LOWER_HEADSHOTS_KEY
                                                                : "headShots"
                                                )
                                        )
                                        .setWeapon(weapon)
                                        .build()
                        );
                        break;
                }
            }
        }

        return new MWPlayerAssetStats(
                weaponStats,
                equipmentStats,
                parseKillstreakStats(killstreakData),
                fieldUpgradeStats,
                parseCommendationStats(commendationsData)
        );
    }

    /**
     * Parse the player's killstreak stats from the given JSON.
     *
     * @param killstreaksData Player killstreak stats JSON.
     * @return Player killstreak stats list
     */
    private ArrayList<KillstreakStats> parseKillstreakStats(JSONObject killstreaksData) {
        ArrayList<KillstreakStats> killstreakStatsList = new ArrayList<>();
        JSONArray killstreaksDataList = killstreaksData.getJSONArray(DATA_KEY);

        for(int i = 0; i < killstreaksDataList.length(); i++) {
            JSONObject killstreakData = killstreaksDataList.getJSONObject(i);
            JSONObject stats = killstreakData.getJSONObject(STATS_KEY);
            Killstreak killstreak = getManager().getKillstreakByCodename(
                    killstreakData.getJSONObject(ATTRIBUTES_KEY).getString(KEY)
            );

            // Unable to locate killstreak of the given codename
            if(killstreak == null) {
                continue;
            }

            killstreakStatsList.add(
                    new KillstreakStats(
                            killstreak,
                            getIntValue(stats, USES_KEY),
                            getIntValue(stats, "extraStat1")
                    )
            );
        }
        return killstreakStatsList;
    }

    /**
     * Parse the player's commendation stats from the given JSON.
     *
     * @param commendationsData Player commendation stats JSON.
     * @return Player commendation stats list
     */
    private ArrayList<CommendationStats> parseCommendationStats(JSONObject commendationsData) {
        ArrayList<CommendationStats> commendationStatsList = new ArrayList<>();

        for(String codename : commendationsData.keySet()) {
            Commendation commendation = getManager().getCommendationByCodename(codename);

            // Unable to locate commendation of the given codename
            if(commendation == null) {
                continue;
            }

            commendationStatsList.add(
                    new CommendationStats(
                            commendation,
                            getIntValue(commendationsData, codename)
                    )
            );
        }
        return commendationStatsList;
    }

    /**
     * Parse the basic player stats - ratios etc
     *
     * @param basicData Basic player JSON data
     * @return Basic player stats
     */
    public PlayerBasicStats parseBasicStats(JSONObject basicData) {
        return new PlayerBasicStats(
                getIntValue(basicData, "longestKillstreak"),
                new Ratio(
                        getIntValue(basicData, "wins"),
                        getIntValue(basicData, "losses")
                ),
                new Ratio(
                        getIntValue(basicData, KILLS_KEY),
                        getIntValue(basicData, DEATHS_KEY)
                )
        );
    }

    /**
     * Stats are stored in a JSON object containing an object for each stat - e.g kills, deaths, etc.
     * Retrieve the integer value for the given stat key.
     *
     * @param statsData Stats
     * @param statKey   Stat to retrieve - e.g "kills"
     * @return Stat value - e.g 100
     */
    private int getIntValue(JSONObject statsData, String statKey) {
        return statsData.getJSONObject(statKey).getInt(VALUE_KEY);
    }
}
