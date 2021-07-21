package COD.API.Parsing;

import COD.API.CODStatsManager.PLATFORM;
import COD.API.MWManager;
import COD.API.TrackerAPI;
import COD.Assets.*;
import COD.PlayerStats.*;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import static COD.Assets.FieldUpgrade.MULTIPLE_FIELD_UPGRADES_ID;

/**
 * Parsing Modern Warfare player data from cod.tracker.gg JSON
 */
public class MWTrackerParser extends CODTrackerParser<MWManager> {
    private static final String KEY = "key";

    /**
     * Create the tracker stats parser
     *
     * @param manager Modern Warfare manager
     */
    public MWTrackerParser(MWManager manager) {
        super(manager);
    }

    /**
     * Parse a Modern Warfare player's stats response from cod.tracker.gg
     *
     * @param name        Player name - adhering to platform rules
     * @param platform    Player platform
     * @param playerStats Player stats JSON
     * @return Player stats
     */
    public MWPlayerStats parseStatsResponse(String name, PLATFORM platform, JSONObject playerStats) {
        return new MWPlayerStats(
                name,
                platform,
                parseAssetStats(
                        playerStats.getJSONArray(TrackerAPI.WEAPONS_KEY),
                        playerStats.getJSONArray(TrackerAPI.KILLSTREAKS_KEY),
                        playerStats.getJSONObject(TrackerAPI.COMMENDATIONS_KEY)
                ),
                parseBasicStats(playerStats.getJSONObject(TrackerAPI.BASIC_KEY))
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
    private MWPlayerAssetStats parseAssetStats(JSONArray weaponData, JSONArray killstreakData, JSONObject commendationsData) {
        ArrayList<FieldUpgradeStats> fieldUpgradeStats = new ArrayList<>();
        PlayerWeaponStats weaponStats = new PlayerWeaponStats();
        PlayerEquipmentStats equipmentStats = new PlayerEquipmentStats();

        for(int i = 0; i < weaponData.length(); i++) {
            JSONObject assetData = weaponData.getJSONObject(i);
            JSONObject attributes = assetData.getJSONObject(ATTRIBUTES_KEY);
            JSONObject stats = assetData.getJSONObject(STATS_KEY);

            // Either a weapon category eg - "weapon_assault_rifle" or "supers" (field upgrades)
            final String categoryKey = attributes.getString("categoryKey");

            // Asset codename - e.g "iw8_ar_mike4" or "super_ammo_drop"
            final String codename = attributes.getString(KEY);

            // Parse field upgrade
            if(categoryKey.equals(SUPERS_KEY)) {

                // Counts Field Upgrade Pro (Running 2 field upgrades) as a field upgrade itself
                if(codename.equals(MULTIPLE_FIELD_UPGRADES_ID)) {
                    continue;
                }

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
                                                getIntValue(stats, HITS_KEY),
                                                getIntValue(stats, SHOTS_KEY)
                                        )
                                        .setHeadshots(
                                                getIntValue(
                                                        stats,
                                                        stats.has(LOWER_HEADSHOTS_KEY)
                                                                ? LOWER_HEADSHOTS_KEY
                                                                : UPPER_HEADSHOTS_KEY
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
    private ArrayList<KillstreakStats> parseKillstreakStats(JSONArray killstreaksData) {
        ArrayList<KillstreakStats> killstreakStatsList = new ArrayList<>();

        for(int i = 0; i < killstreaksData.length(); i++) {
            JSONObject killstreakData = killstreaksData.getJSONObject(i);
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
                            getIntValue(stats, EXTRA_STAT_1_KEY)
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
                        getIntValue(basicData, WINS_KEY),
                        getIntValue(basicData, LOSSES_KEY)
                ),
                new Ratio(
                        getIntValue(basicData, KILLS_KEY),
                        getIntValue(basicData, DEATHS_KEY)
                )
        );
    }
}
