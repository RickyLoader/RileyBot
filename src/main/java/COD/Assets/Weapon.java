package COD.Assets;

import java.awt.image.BufferedImage;

/**
 * Hold weapon information
 */
public class Weapon {
    private final String codename, name, category;
    private final TYPE type;
    private final BufferedImage image;

    public enum TYPE {
        PRIMARY,
        SECONDARY,
        LETHAL,
        TACTICAL;

        /**
         * Get a weapon type for the given category
         *
         * @param category Type for given category
         * @return Weapon type by category
         */
        public static TYPE discernType(String category) {
            switch(category) {
                case "weapon_sniper":
                case "weapon_lmg":
                case "weapon_assault_rifle":
                case "weapon_other":
                case "weapon_shotgun":
                case "weapon_smg":
                case "weapon_marksman":
                default:
                    return PRIMARY;
                case "weapon_launcher":
                case "weapon_pistol":
                case "weapon_melee":
                    return SECONDARY;
                case "lethals":
                    return LETHAL;
                case "tacticals":
                    return TACTICAL;
            }
        }

        /**
         * Get the weapon TYPE as a title
         *
         * @return String title
         */
        public String getTitle() {
            switch(this) {
                case PRIMARY:
                    return "Primary Weapon of Choice";
                case SECONDARY:
                    return "Secondary Weapon of Choice";
                case LETHAL:
                    return "Lethal Equipment of Choice";
                case TACTICAL:
                    return "Tactical Equipment of Choice";
            }
            return "Weapon of Choice";
        }
    }

    /**
     * Create a weapon
     *
     * @param codename Codename of weapon e.g "iw8_me_akimboblunt"
     * @param name     Real name of weapon e.g "Kali Sticks"
     * @param category Codename of weapon category e.g "weapon_melee"
     * @param image    Weapon image
     */
    public Weapon(String codename, String name, String category, BufferedImage image) {
        this.codename = codename;
        this.name = name;
        this.category = category;
        this.type = TYPE.discernType(category);
        this.image = image;
    }

    /**
     * Get the codename of the weapon e.g "iw8_me_akimboblunt"
     *
     * @return Codename of weapon
     */
    public String getCodename() {
        return codename;
    }

    /**
     * Get the weapon image
     *
     * @return Weapon image
     */
    public BufferedImage getImage() {
        return image;
    }

    /**
     * Get the name of the weapon
     *
     * @return Weapon name
     */
    public String getName() {
        return name;
    }

    /**
     * Get the weapon type
     *
     * @return Weapon type
     */
    public TYPE getType() {
        return type;
    }

    /**
     * Get the name of the weapon category
     *
     * @return Weapon category name
     */
    public String getCategory() {
        return category;
    }
}
