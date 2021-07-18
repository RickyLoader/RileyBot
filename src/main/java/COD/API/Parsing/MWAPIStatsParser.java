package COD.API.Parsing;

import COD.API.MWManager;
import COD.Assets.*;
import COD.PlayerStats.*;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Parsing Modern Warfare stats response from the API
 */
public class MWAPIStatsParser extends MWStatsParser {

    /**
     * Create the API stats parser
     *
     * @param manager Modern Warfare manager
     */
    public MWAPIStatsParser(MWManager manager) {
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
            JSONObject killstreakData = supportStreaksData.getJSONObject(name).getJSONObject("properties");
            Killstreak killstreak = getManager().getKillstreakByCodename(name);

            // Unable to locate killstreak of the given codename
            if(killstreak == null){
                continue;
            }

            stats.add(
                    new KillstreakStats(
                            killstreak,
                            killstreakData.getInt("extraStat1"),
                            killstreakData.getInt("uses")
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
            if(superName.equals("super_select")) {
                continue;
            }
            JSONObject fieldUpgradeData = superData.getJSONObject(superName).getJSONObject("properties");
            FieldUpgrade fieldUpgrade = getManager().getSuperByCodename(superName);
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
        return fieldUpgradeStats;
    }

    /**
     * Parse the player asset stats from the API JSON.
     *
     * @param playerData Player JSON data containing asset stats (killstreaks/weapons etc)
     * @return Player stats
     */
    public MWPlayerAssetStats parseAssetStats(JSONObject playerData) {
        final String supersKey = "supers";

        // Contains all weapons/equipment/field upgrades
        JSONObject weapons = playerData.getJSONObject("itemData");

        // Parse field upgrades separately to weapons
        ArrayList<FieldUpgradeStats> supers = parseFieldUpgradeStats(weapons.getJSONObject(supersKey));

        // Remove as to not parse when parsing weapons
        weapons.remove(supersKey);

        PlayerWeaponStats weaponStats = new PlayerWeaponStats();
        PlayerEquipmentStats equipmentStats = new PlayerEquipmentStats();

        // Lethal, Tactical, Assault Rifle etc
        for(String categoryName : weapons.keySet()) {
            JSONObject categoryData = weapons.getJSONObject(categoryName);
            Weapon.CATEGORY category = Weapon.CATEGORY.discernCategory(categoryName);

            // Weapons within category
            for(String weaponName : categoryData.keySet()) {
                JSONObject weaponData = categoryData.getJSONObject(weaponName).getJSONObject("properties");
                Weapon weapon = getManager().getWeaponByCodename(weaponName, category);

                switch(weapon.getType()) {
                    case LETHAL:
                        equipmentStats.addLethalStats(
                                new LethalStats(
                                        weapon,
                                        weaponData.getInt("kills"),
                                        weaponData.getInt("uses")
                                )
                        );
                        break;
                    case TACTICAL:
                        equipmentStats.addTacticalStats(
                                new TacticalStats(
                                        (TacticalWeapon) weapon,
                                        weaponData.getInt("extraStat1"),
                                        weaponData.getInt("uses")
                                )
                        );
                        break;
                    default:
                        weaponStats.addWeaponStats(
                                new StandardWeaponStats.StandardWeaponStatsBuilder()
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
                parseCommendationStats(playerData.getJSONObject("accoladeData").getJSONObject("properties"))
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
                new Ratio(basicData.getInt("kills"), basicData.getInt("deaths")),
                new Ratio(basicData.getInt("wins"), basicData.getInt("losses"))
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
}
