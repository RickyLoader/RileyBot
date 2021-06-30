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
public abstract class CODManager {
    final ResourceHandler resourceHandler;
    final String basePath;
    private final HashMap<String, Weapon> weapons;
    private final HashMap<String, Map> maps;
    private final HashMap<String, Mode> modes;
    private final HashMap<String, Perk> perks;
    private final GAME game;
    private final HashMap<Weapon.CATEGORY, BufferedImage> missingWeaponImages = new HashMap<>();
    private final BufferedImage missingPerkImage, missingModeImage;

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
        this.missingPerkImage = resourceHandler.getImageResource(basePath + "Perks/missing.png");
        this.missingModeImage = resourceHandler.getImageResource(basePath + "Modes/missing.png");
    }

    /**
     * Get the URL to an image representing a missing mode
     *
     * @return Missing mode image URL
     */
    public abstract String getMissingModeImageURL();

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
            Weapon.CATEGORY category = Weapon.CATEGORY.discernCategory(categoryName);
            for(String weaponName : categoryData.keySet()) {
                JSONObject weaponData = categoryData.getJSONObject(weaponName);
                String gameName = weaponData.getString("real_name");
                String path = basePath + "Weapons/" + categoryName + "/";
                if(!missingWeaponImages.containsKey(category)) {
                    missingWeaponImages.put(category, resourceHandler.getImageResource(path + "missing.png"));
                }
                BufferedImage image = resourceHandler.getImageResource(path + weaponName + ".png");
                String imageURL = weaponData.getString("image_url");
                Weapon weapon;
                if(category == Weapon.CATEGORY.TACTICALS) {
                    weapon = new TacticalWeapon(
                            weaponName,
                            gameName,
                            imageURL,
                            weaponData.has("property") ? weaponData.getString("property") : null,
                            image
                    );
                }
                else {
                    HashMap<String, Attachment> attachments = parseAttachments(weaponData, weaponName, categoryName);
                    HashMap<Integer, Variant> variants = parseVariants(weaponData, weaponName, categoryName);
                    weapon = new Weapon(
                            weaponName,
                            gameName,
                            category,
                            imageURL,
                            image,
                            attachments,
                            variants
                    );
                }
                weapons.put(weaponName, weapon);
            }
        }
        missingWeaponImages.put(
                Weapon.CATEGORY.UNKNOWN,
                resourceHandler.getImageResource(basePath + "Weapons/unknown_category.png")
        );
        return weapons;
    }

    /**
     * Parse the weapon variants from the given weapon JSON to a map of variant id -> variant
     *
     * @param weaponData     Weapon JSON
     * @param weaponName     Weapon codename
     * @param weaponCategory Weapon category codename
     * @return Map of weapon variant id -> weapon variant
     */
    private HashMap<Integer, Variant> parseVariants(JSONObject weaponData, String weaponName, String weaponCategory) {
        HashMap<Integer, Variant> variants = new HashMap<>();
        if(!weaponData.has("variants")) {
            return variants;
        }
        JSONObject variantData = weaponData.getJSONObject("variants");
        for(String variantIdString : variantData.keySet()) {
            JSONObject variant = variantData.getJSONObject(variantIdString);
            int variantId = Integer.parseInt(variantIdString);
            String imagePath = basePath + "Weapons/" + weaponCategory + "/" + weaponName + "_v" + variantId + ".png";
            variants.put(
                    variantId,
                    new Variant(
                            variantId,
                            variant.getString("real_name"),
                            resourceHandler.getImageResource(imagePath)
                    )
            );
        }
        return variants;
    }

    /**
     * Parse the attachments from the given weapon JSON to a map of attachment codename -> attachment
     *
     * @param weaponData     Weapon JSON
     * @param weaponName     Weapon codename
     * @param weaponCategory Weapon category codename
     * @return Map of attachment codename -> attachment
     */
    private HashMap<String, Attachment> parseAttachments(JSONObject weaponData, String weaponName, String weaponCategory) {
        HashMap<String, Attachment> attachments = new HashMap<>();
        if(!weaponData.has("attachments")) {
            return attachments;
        }
        JSONObject attachmentData = weaponData.getJSONObject("attachments");
        for(String attachmentName : attachmentData.keySet()) {
            JSONObject attachment = attachmentData.getJSONObject(attachmentName);
            String imagePath = basePath + "Attachments/"
                    + weaponCategory + "/"
                    + weaponName + "/"
                    + attachmentName + ".png";

            JSONObject stats = attachment.getJSONObject("stats");
            Attributes attributes = new Attributes.AttributesBuilder()
                    .setAccuracyStat(stats.getInt("accuracy"))
                    .setDamageStat(stats.getInt("damage"))
                    .setFireRateStat(stats.getInt("firerate"))
                    .setMobilityStat(stats.getInt("mobility"))
                    .setRangeStat(stats.getInt("range"))
                    .setControlStat(stats.getInt("control"))
                    .build();

            CATEGORY blockedCategory = attachment.has("blocksCategory")
                    ? CATEGORY.valueOf(attachment.getString("blocksCategory").toUpperCase())
                    : CATEGORY.NONE;

            attachments.put(
                    attachmentName,
                    new Attachment(
                            attachmentName,
                            attachment.getString("real_name"),
                            CATEGORY.valueOf(attachment.getString("category").toUpperCase()),
                            blockedCategory,
                            attributes,
                            resourceHandler.getImageResource(imagePath)
                    )
            );
        }
        return attachments;
    }

    /**
     * Parse map JSON in to objects and map to the codename
     *
     * @return Map of codename -> map
     */
    private HashMap<String, Map> readMaps() {
        HashMap<String, Map> maps = new HashMap<>();
        JSONObject mapList = getAssetJSON("maps.json", "maps");
        final String imageKey = "loading_image_url";

        for(String name : mapList.keySet()) {
            String imagePath = basePath + "Maps/" + name;
            JSONObject mapData = mapList.getJSONObject(name);
            Map map = new Map(
                    name,
                    mapData.getString("real_name"),
                    game,
                    resourceHandler.getImageResource(imagePath + ".png"),
                    mapData.has(imageKey) ? mapData.getString(imageKey) : null
            );
            maps.put(name, map);
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
        String imagePath = basePath + "Modes/";
        for(String name : modeList.keySet()) {
            JSONObject modeData = modeList.getJSONObject(name);
            BufferedImage modeIcon = resourceHandler.getImageResource(imagePath + name + ".png");
            modes.put(
                    name,
                    new Mode(
                            name,
                            modeData.getString("real_name"),
                            modeIcon,
                            modeData.getString("image_url")
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
            map = new Map(codename, game);
            maps.put(codename, map);
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
            mode = new Mode(
                    codename,
                    "MISSING: " + codename,
                    missingModeImage,
                    getMissingModeImageURL()
            );
            modes.put(codename, mode);
        }
        return mode;
    }

    /**
     * Get a perk by its codename
     *
     * @param codename Perk codename
     * @return Perk with codename or unknown perk
     */
    public Perk getPerkByCodename(String codename) {
        Perk perk = perks.get(codename);
        if(perk == null) {
            perk = new Perk(codename, "Missing: " + codename, Perk.CATEGORY.UNKNOWN, missingPerkImage);
        }
        return perk;
    }

    /**
     * Get an array of perks by colour category
     *
     * @param colour Colour category
     * @return Array of perks of given colour
     */
    public Perk[] getPerksByColour(Perk.CATEGORY colour) {
        return perks
                .values()
                .stream()
                .filter(p -> p.getCategory() == colour)
                .toArray(Perk[]::new);
    }

    /**
     * Get a weapon by its codename
     *
     * @param codename        Weapon codename
     * @param defaultCategory Default weapon category (If weapon is missing a default category image is used)
     * @return Weapon with codename or unknown weapon
     */
    public Weapon getWeaponByCodename(String codename, Weapon.CATEGORY defaultCategory) {
        if(weapons.containsKey(codename)) {
            return weapons.get(codename);
        }
        return new Weapon(
                codename,
                defaultCategory,
                missingWeaponImages.get(defaultCategory)
        );
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
     * Get an array of weapons by category
     *
     * @param category Weapon category
     * @return Array of weapons of given category
     */
    public Weapon[] getWeaponsByCategory(Weapon.CATEGORY category) {
        return weapons
                .values()
                .stream()
                .filter(w -> w.getCategory() == category)
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
