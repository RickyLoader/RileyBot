package COD;

import Bot.ResourceHandler;
import COD.Assets.*;
import org.json.JSONObject;

import java.awt.image.BufferedImage;
import java.util.HashMap;

/**
 * Manage resources, weapons, maps, and modes for COD
 */
public class CODManager {
    private final ResourceHandler resourceHandler;
    private final HashMap<String, Weapon> weapons;
    private final HashMap<String, FieldUpgrade> supers;
    private final HashMap<String, Commendation> commendations;
    private final HashMap<String, Killstreak> killstreaks;
    private final HashMap<String, Map> maps;
    private final HashMap<String, Mode> modes;
    private final static String BASE_PATH = "/COD/MW/";

    public CODManager() {
        this.resourceHandler = new ResourceHandler();
        this.weapons = readWeapons();
        this.supers = readSupers();
        this.commendations = readCommendations();
        this.killstreaks = readKillstreaks();
        this.maps = readMaps();
        this.modes = readModes();
    }

    /**
     * Parse commendation JSON in to objects and map to the codename
     *
     * @return Map of codename -> commendation
     */
    private HashMap<String, Commendation> readCommendations() {
        HashMap<String, Commendation> commendations = new HashMap<>();
        JSONObject commendationList = getAssetJSON("commendations.json", "commendations");

        for(String name : commendationList.keySet()) {
            JSONObject commendationData = commendationList.getJSONObject(name);
            commendations.put(
                    name,
                    new Commendation(
                            name,
                            commendationData.getString("title"),
                            commendationData.getString("desc"),
                            resourceHandler.getImageResource(BASE_PATH + "Accolades/" + name + ".png")
                    )
            );
        }
        return commendations;
    }

    /**
     * Parse supers JSON in to objects and map to the codename
     *
     * @return Map of codename -> super
     */
    private HashMap<String, FieldUpgrade> readSupers() {
        HashMap<String, FieldUpgrade> supers = new HashMap<>();
        JSONObject supersList = getAssetJSON("supers.json", "supers");

        for(String name : supersList.keySet()) {
            JSONObject superData = supersList.getJSONObject(name);
            supers.put(
                    name,
                    new FieldUpgrade(
                            name,
                            superData.getString("real_name"),
                            superData.has("misc1_name")
                                    ? superData.getString("misc1_name")
                                    : null,
                            resourceHandler.getImageResource(BASE_PATH + "Supers/" + name + ".png")
                    )
            );
        }
        return supers;
    }

    /**
     * Parse weapon JSON in to objects and map to the codename
     *
     * @return Map of codename -> weapon
     */
    private HashMap<String, Weapon> readWeapons() {
        HashMap<String, Weapon> weapons = new HashMap<>();
        JSONObject weaponList = getAssetJSON("weapons.json", "weapons");

        for(String categoryName : weaponList.keySet()) {
            JSONObject categoryData = weaponList.getJSONObject(categoryName);
            for(String weaponName : categoryData.keySet()) {
                JSONObject weaponData = categoryData.getJSONObject(weaponName);
                String gameName = weaponData.getString("real_name");
                BufferedImage image = resourceHandler.getImageResource(
                        BASE_PATH + "Weapons/" + categoryName + "/" + weaponName + ".png"
                );
                Weapon weapon;
                if(categoryName.equals("tacticals")) {
                    weapon = new TacticalWeapon(
                            weaponName,
                            gameName,
                            categoryName,
                            weaponData.has("property") ? weaponData.getString("property") : null,
                            image
                    );
                }
                else {
                    weapon = new Weapon(
                            weaponName,
                            gameName,
                            categoryName,
                            image
                    );
                }
                weapons.put(
                        weaponName,
                        weapon
                );
            }
        }
        return weapons;
    }

    /**
     * Parse killstreak JSON in to objects and map to the codename
     *
     * @return Map of codename -> killstreak
     */
    private HashMap<String, Killstreak> readKillstreaks() {
        HashMap<String, Killstreak> killstreaks = new HashMap<>();
        JSONObject killstreakList = getAssetJSON("streaks.json", "streaks");

        for(String name : killstreakList.keySet()) {
            JSONObject killstreakData = killstreakList.getJSONObject(name);
            killstreaks.put(
                    name,
                    new Killstreak(
                            name,
                            killstreakData.getString("real_name"),
                            killstreakData.has("extra_stat")
                                    ? killstreakData.getString("extra_stat")
                                    : null,
                            resourceHandler.getImageResource(
                                    BASE_PATH + "Killstreaks/" + name + ".png"
                            )
                    )
            );
        }
        return killstreaks;
    }

    /**
     * Parse map JSON in to objects and map to the codename
     *
     * @return Map of codename -> map
     */
    private HashMap<String, Map> readMaps() {
        HashMap<String, Map> maps = new HashMap<>();
        JSONObject mapList = getAssetJSON("maps.json", "maps");
        for(String name : mapList.keySet()) {
            JSONObject mapData = mapList.getJSONObject(name);
            maps.put(
                    name,
                    new Map(
                            name,
                            mapData.getString("real_name"),
                            "https://www.callofduty.com/cdn/app/base-maps/mw/" + name + ".jpg"
                    )
            );
        }
        return maps;
    }

    /**
     * Parse mode JSON in to objects and map to the codename
     *
     * @return Map of codename -> mode
     */
    private HashMap<String, Mode> readModes() {
        HashMap<String, Mode> modes = new HashMap<>();
        JSONObject modeList = getAssetJSON("modes.json", "modes");
        for(String name : modeList.keySet()) {
            JSONObject modeData = modeList.getJSONObject(name);
            modes.put(
                    name,
                    new Mode(
                            name,
                            modeData.getString("real_name")
                    )
            );
        }
        return modes;
    }

    /**
     * Read in the JSON of the given filename
     *
     * @param filename JSON filename
     * @param key      Key to JSON object
     * @return JSON object of file at given path
     */
    private JSONObject getAssetJSON(String filename, String key) {
        return new JSONObject(
                resourceHandler.getResourceFileAsString(BASE_PATH + "/Data/" + filename)
        ).getJSONObject(key);
    }

    /**
     * Get a map by its codename
     *
     * @param codename Map codename
     * @return Map with codename or null
     */
    public Map getMapByCodename(String codename) {
        return maps.get(codename);
    }

    /**
     * Get a mode by its codename
     *
     * @param codename Mode codename
     * @return Mode with codename or null
     */
    public Mode getModeByCodename(String codename) {
        return modes.get(codename);
    }

    /**
     * Get a killstreak by its codename
     *
     * @param codename Killstreak codename
     * @return Killstreak with codename or null
     */
    public Killstreak getKillstreakByCodename(String codename) {
        return killstreaks.get(codename);
    }

    /**
     * Get a weapon by its codename
     *
     * @param codename Weapon codename
     * @return Weapon with codename or null
     */
    public Weapon getWeaponByCodename(String codename) {
        return weapons.get(codename);
    }

    /**
     * Get a commendation by its codename
     *
     * @param codename Commendation codename
     * @return Commendation with codename or null
     */
    public Commendation getCommendationByCodename(String codename) {
        return commendations.get(codename);
    }

    /**
     * Get a super by its codename
     *
     * @param codename Super codename
     * @return Super with codename or null
     */
    public FieldUpgrade getSuperByCodename(String codename) {
        return supers.get(codename);
    }
}
