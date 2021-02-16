package COD;

import Bot.ResourceHandler;
import COD.Assets.*;
import org.json.JSONObject;

import java.awt.image.BufferedImage;
import java.util.HashMap;

import static COD.Assets.Attachment.*;

/**
 * Manage resources, weapons, maps, and modes for COD
 */
public class CODManager {
    final ResourceHandler resourceHandler;
    final String basePath;
    private final HashMap<String, Weapon> weapons;
    private final HashMap<String, Map> maps;
    private final HashMap<String, Mode> modes;
    private final HashMap<String, Perk> perks;
    private final GAME game;

    public enum GAME {
        CW,
        MW
    }

    /**
     * Create the COD manager
     *
     * @param game COD game
     */
    public CODManager(GAME game) {
        this.game = game;
        this.basePath = "/COD/" + game.name() + "/";
        this.resourceHandler = new ResourceHandler();
        this.weapons = readWeapons();
        this.maps = readMaps();
        this.modes = readModes();
        this.perks = readPerks();
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
                        basePath + "Weapons/" + categoryName + "/" + weaponName + ".png"
                );
                String imageURL = weaponData.getString("image_url");
                Weapon weapon;
                if(categoryName.equals("tacticals")) {
                    weapon = new TacticalWeapon(
                            weaponName,
                            gameName,
                            imageURL,
                            categoryName,
                            weaponData.has("property") ? weaponData.getString("property") : null,
                            image
                    );
                }
                else {
                    HashMap<String, Attachment> attachments = new HashMap<>();
                    if(weaponData.has("attachments")) {
                        JSONObject attachmentData = weaponData.getJSONObject("attachments");
                        for(String attachmentName : attachmentData.keySet()) {
                            JSONObject attachment = attachmentData.getJSONObject(attachmentName);
                            attachments.put(
                                    attachmentName,
                                    new Attachment(
                                            attachmentName,
                                            attachment.getString("real_name"),
                                            CATEGORY.valueOf(attachment.getString("category").toUpperCase()),
                                            resourceHandler.getImageResource(
                                                    basePath
                                                            + "Attachments/"
                                                            + categoryName + "/"
                                                            + weaponName + "/"
                                                            + attachmentName + ".png"
                                            )
                                    )
                            );
                        }
                    }
                    weapon = new Weapon(
                            weaponName,
                            gameName,
                            categoryName,
                            imageURL,
                            image,
                            attachments
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
                            getMapImageUrl(name),
                            resourceHandler.getImageResource(basePath + "Maps/" + name + ".png")
                    )
            );
        }
        return maps;
    }

    /**
     * Get the image URL for a map given the codename
     *
     * @param codename Codename to retrieve
     * @return Image URL for map
     */
    private String getMapImageUrl(String codename) {
        return "https://www.callofduty.com/cdn/app/base-maps/"
                + game.name().toLowerCase() + "/" + codename + ".jpg";
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
     * Parse perk JSON in to objects and map to the codename
     *
     * @return Map of codename -> perk
     */
    private HashMap<String, Perk> readPerks() {
        HashMap<String, Perk> perks = new HashMap<>();
        JSONObject perkList = getAssetJSON("perks.json", "perks");

        for(String colour : perkList.keySet()) {
            Perk.CATEGORY category = Perk.CATEGORY.valueOf(colour.toUpperCase());
            JSONObject perkCategory = perkList.getJSONObject(colour);
            for(String perkName : perkCategory.keySet()) {
                JSONObject perk = perkCategory.getJSONObject(perkName);
                perks.put(
                        perkName,
                        new Perk(
                                perkName,
                                perk.getString("real_name"),
                                category,
                                resourceHandler.getImageResource(basePath + "Perks/" + perkName + ".png")
                        )
                );
            }
        }
        return perks;
    }

    /**
     * Read in the JSON of the given filename
     *
     * @param filename JSON filename
     * @param key      Key to JSON object
     * @return JSON object of file at given path
     */
    public JSONObject getAssetJSON(String filename, String key) {
        return new JSONObject(
                resourceHandler.getResourceFileAsString(basePath + "Data/" + filename)
        ).getJSONObject(key);
    }

    /**
     * Get a map by its codename
     *
     * @param codename Map codename
     * @return Map with codename or unknown map
     */
    public Map getMapByCodename(String codename) {
        Map map = maps.get(codename);
        if(map == null) {
            map = new Map(codename, "MISSING: " + codename, getMapImageUrl(codename), null);
        }
        return map;
    }

    /**
     * Get a mode by its codename
     *
     * @param codename Mode codename
     * @return Mode with codename or unknown mode
     */
    public Mode getModeByCodename(String codename) {
        Mode mode = modes.get(codename);
        if(mode == null) {
            mode = new Mode(codename, "MISSING: " + codename);
        }
        return mode;
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
     * Get an array of weapons by name
     *
     * @param name In-game weapon name
     * @return Array of weapons containing given name
     */
    public Weapon[] getWeaponsByName(String name) {
        return weapons
                .values()
                .stream()
                .filter(w -> w.getName().toLowerCase().contains(name.toLowerCase()))
                .toArray(Weapon[]::new);
    }

    /**
     * Get a perk by its codename
     *
     * @param codename Perk codename
     * @return Perk with codename or null
     */
    public Perk getPerkByCodename(String codename) {
        return perks.get(codename);
    }

    /**
     * Get an array of weapons by category
     *
     * @param category Weapon category
     * @return Array of weapons of given category
     */
    public Weapon[] getWeaponsByCategory(String category) {
        return weapons
                .values()
                .stream()
                .filter(w -> w.getCategory().equalsIgnoreCase(category))
                .toArray(Weapon[]::new);
    }

    /**
     * Get the COD game
     *
     * @return Game
     */
    public GAME getGame() {
        return game;
    }
}
