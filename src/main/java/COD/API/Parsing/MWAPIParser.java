package COD.API.Parsing;

import COD.API.CODStatsManager;
import COD.API.MWManager;
import COD.Assets.*;
import COD.PlayerStats.*;
import org.json.JSONObject;

import java.util.ArrayList;

import static COD.Assets.FieldUpgrade.MULTIPLE_FIELD_UPGRADES_ID;

/**
 * Parsing Modern Warfare player data from COD API JSON
 */
public class MWAPIParser extends CODAPIParser<MWManager> {
    private static final String PROPERTIES_KEY = "properties";

    /**
     * Create the API stats parser
     *
     * @param manager Modern Warfare manager
     */
    public MWAPIParser(MWManager manager) {
        super(manager);
    }

    /**
     * Parse the player's killstreak stats from the given killstreak JSON data
     *
     * @param killstreaksData Killstreak stats JSON data
     * @return List of player killstreak stats
     */
    private ArrayList<KillstreakStats> parseKillstreakStats(JSONObject killstreaksData) {
        JSONObject supportStreaksData = killstreaksData.getJSONObject("lethalScorestreakData");
        JSONObject lethalStreaksData = killstreaksData.getJSONObject("supportScorestreakData");

        ArrayList<KillstreakStats> stats = new ArrayList<>();

        // Combine lethal and support streaks as they have no functional difference in data
        for(String lethalKey : lethalStreaksData.keySet()) {
            supportStreaksData.put(lethalKey, lethalStreaksData.getJSONObject(lethalKey));
        }

        for(String name : supportStreaksData.keySet()) {
            JSONObject killstreakData = supportStreaksData.getJSONObject(name).getJSONObject(PROPERTIES_KEY);
            Killstreak killstreak = getManager().getKillstreakByCodename(name);

            // Unable to locate killstreak of the given codename
            if(killstreak == null) {
                continue;
            }

            stats.add(
                    new KillstreakStats(
                            killstreak,
                            killstreakData.getInt(EXTRA_STAT_1_KEY),
                            killstreakData.getInt(USES_KEY)
                    )
            );
        }
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
            if(superName.equals(MULTIPLE_FIELD_UPGRADES_ID)) {
                continue;
            }

            JSONObject fieldUpgradeData = superData.getJSONObject(superName).getJSONObject(PROPERTIES_KEY);
            FieldUpgrade fieldUpgrade = getManager().getSuperByCodename(superName);

            // Unable to locate super of the given codename
            if(fieldUpgrade == null) {
                continue;
            }

            fieldUpgradeStats.add(
                    new FieldUpgradeStats(
                            fieldUpgrade,
                            fieldUpgradeData.getInt(KILLS_KEY),
                            fieldUpgradeData.getInt(USES_KEY),
                            fieldUpgradeData.getInt("misc1")
                    )
            );
        }
        return fieldUpgradeStats;
    }

    /**
     * Parse the player asset stats from the API JSON.
     *
     * @param playerData Player JSON data containing asset stats (killstreaks/weapons etc)
     * @return Player stats
     */
    public MWPlayerAssetStats parseAssetStats(JSONObject playerData) {
        // Contains all weapons/equipment/field upgrades
        JSONObject weapons = playerData.getJSONObject("itemData");

        // Parse field upgrades separately to weapons
        ArrayList<FieldUpgradeStats> supers = parseFieldUpgradeStats(weapons.getJSONObject(SUPERS_KEY));

        // Remove as to not parse when parsing weapons
        weapons.remove(SUPERS_KEY);

        PlayerWeaponStats weaponStats = new PlayerWeaponStats();
        PlayerEquipmentStats equipmentStats = new PlayerEquipmentStats();

        // Lethal, Tactical, Assault Rifle etc
        for(String categoryName : weapons.keySet()) {
            JSONObject categoryData = weapons.getJSONObject(categoryName);
            Weapon.CATEGORY category = Weapon.CATEGORY.discernCategory(categoryName);

            // Weapons within category
            for(String weaponName : categoryData.keySet()) {
                JSONObject weaponData = categoryData.getJSONObject(weaponName).getJSONObject(PROPERTIES_KEY);
                Weapon weapon = getManager().getWeaponByCodename(weaponName, category);

                switch(weapon.getType()) {
                    case LETHAL:
                        equipmentStats.addLethalStats(
                                new LethalStats(
                                        weapon,
                                        weaponData.getInt(KILLS_KEY),
                                        weaponData.getInt(USES_KEY)
                                )
                        );
                        break;
                    case TACTICAL:
                        equipmentStats.addTacticalStats(
                                new TacticalStats(
                                        (TacticalWeapon) weapon,
                                        weaponData.getInt(EXTRA_STAT_1_KEY),
                                        weaponData.getInt(USES_KEY)
                                )
                        );
                        break;
                    default:
                        weaponStats.addWeaponStats(
                                new StandardWeaponStats.StandardWeaponStatsBuilder()
                                        .setKillDeath(
                                                weaponData.getInt(KILLS_KEY),
                                                weaponData.getInt(DEATHS_KEY)
                                        )
                                        .setAccuracy(
                                                weaponData.getInt(HITS_KEY),
                                                weaponData.getInt(SHOTS_KEY)
                                        )
                                        .setHeadshots(
                                                weaponData.has(LOWER_HEADSHOTS_KEY)
                                                        ? weaponData.getInt(LOWER_HEADSHOTS_KEY)
                                                        : weaponData.getInt(UPPER_HEADSHOTS_KEY)
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
                parseKillstreakStats(playerData.getJSONObject("scorestreakData")),
                supers,
                parseCommendationStats(playerData.getJSONObject("accoladeData").getJSONObject(PROPERTIES_KEY))
        );
    }

    /**
     * Parse the basic player stats - ratios etc
     *
     * @param basicData Basic player JSON data
     * @return Basic player stats
     */
    public PlayerBasicStats parseBasicStats(JSONObject basicData) {
        return new PlayerBasicStats(
                basicData.getInt("recordKillStreak"),
                new Ratio(basicData.getInt(KILLS_KEY), basicData.getInt(DEATHS_KEY)),
                new Ratio(basicData.getInt(WINS_KEY), basicData.getInt(LOSSES_KEY))
        );
    }

    /**
     * Parse the player's commendation stats from the given commendation JSON data
     *
     * @param commendationsData Commendation stats JSON data
     * @return List of player commendation stats
     */
    private ArrayList<CommendationStats> parseCommendationStats(JSONObject commendationsData) {
        ArrayList<CommendationStats> commendationStats = new ArrayList<>();

        for(String commendationName : commendationsData.keySet()) {
            Commendation commendation = getManager().getCommendationByCodename(commendationName);
            if(commendation == null) {
                continue;
            }
            commendationStats.add(
                    new CommendationStats(
                            commendation,
                            commendationsData.getInt(commendationName)
                    )
            );
        }

        return commendationStats;
    }

    /**
     * Parse a Modern Warfare player's stats response from the API
     *
     * @param name        Player name - adhering to platform rules
     * @param platform    Player platform
     * @param playerStats Player stats JSON
     * @return Player stats
     */
    public MWPlayerStats parseStatsResponse(String name, CODStatsManager.PLATFORM platform, JSONObject playerStats) {
        JSONObject stats = playerStats.getJSONObject("data").getJSONObject("lifetime");
        return new MWPlayerStats(
                name,
                platform,
                parseAssetStats(stats),
                parseBasicStats(stats.getJSONObject("all").getJSONObject(PROPERTIES_KEY))
        );
    }
}
