package Riot.Valorant.Assets;

import Bot.ResourceHandler;

import java.awt.image.BufferedImage;

/**
 * Valorant weapon
 */
public class Weapon extends ValorantAsset {
    private final int cost;
    private final CATEGORY category;
    private final TYPE type;

    // Base path to weapon images
    public static final String BASE_PATH = ResourceHandler.VALORANT_BASE_PATH + "Weapons/";

    // Weapon IDs
    public static final String
            SPECTRE_ID = "spectre",
            STINGER_ID = "stinger",
            PHANTOM_ID = "phantom",
            BULLDOG_ID = "bulldog",
            VANDAL_ID = "vandal",
            GUARDIAN_ID = "guardian",
            BUCKY_ID = "bucky",
            JUDGE_ID = "judge",
            ARES_ID = "ares",
            ODIN_ID = "odin",
            MARSHAL_ID = "marshal",
            OPERATOR_ID = "operator",
            FRENZY_ID = "frenzy",
            GHOST_ID = "ghost",
            MELEE_ID = "melee",
            CLASSIC_ID = "classic",
            SHORTY_ID = "shorty",
            SHERIFF_ID = "sheriff";

    public enum CATEGORY {
        SMGS,
        MELEE,
        ASSAULT_RIFLES,
        SHOTGUNS,
        HEAVY_WEAPONS,
        SNIPER_RIFLES,
        SIDEARMS;

        /**
         * Get the weapon category by its name
         *
         * @param categoryName Name of category - e.g "Assault Rifles"
         * @return CATEGORY - e.g ASSAULT_RIFLES
         */
        public static CATEGORY fromName(String categoryName) {
            return CATEGORY.valueOf(categoryName.toUpperCase().replace(" ", "_"));
        }
    }

    public enum TYPE {
        PRIMARY,
        SECONDARY;

        /**
         * Get a weapon type for the given category
         *
         * @param category Weapon category e.g - MELEE
         * @return Weapon type e.g - SECONDARY
         */
        public static TYPE fromCategory(CATEGORY category) {
            switch(category) {
                case SMGS:
                case ASSAULT_RIFLES:
                case SHOTGUNS:
                case HEAVY_WEAPONS:
                case SNIPER_RIFLES:
                    return PRIMARY;
                case SIDEARMS:
                default:
                    return SECONDARY;
            }
        }
    }

    /**
     * Create a Valorant weapon
     *
     * @param id       Unique ID of the weapon, usually just the name in lowercase - e.g "bucky"
     * @param name     Name of the weapon - e.g "Bucky"
     * @param category Weapon category e.g SHOTGUNS
     * @param cost     In-game cost of purchasing the weapon e.g 850
     * @param image    Image of the weapon
     */
    public Weapon(String id, String name, CATEGORY category, int cost, BufferedImage image) {
        super(id, name, image);
        this.category = category;
        this.cost = cost;
        this.type = TYPE.fromCategory(category);
    }

    /**
     * Get the weapon type - e.g PRIMARY
     *
     * @return Weapon type
     */
    public TYPE getType() {
        return type;
    }

    /**
     * Get the in-game cost of purchasing the weapon e.g 850
     *
     * @return Weapon cost
     */
    public int getCost() {
        return cost;
    }

    /**
     * Get the category that the weapon belongs to - e.g SHOTGUNS
     *
     * @return Weapon category
     */
    public CATEGORY getCategory() {
        return category;
    }
}
