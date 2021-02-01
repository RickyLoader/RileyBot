package COD.Assets;

import java.awt.image.BufferedImage;
import java.util.HashMap;

/**
 * Hold weapon information
 */
public class Weapon {
    private final String codename, name, category, imageURL;
    private final TYPE type;
    private final BufferedImage image;
    private final HashMap<String, Attachment> attachments;

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
     * @param codename    Codename of weapon e.g "iw8_me_akimboblunt"
     * @param name        Real name of weapon e.g "Kali Sticks"
     * @param category    Codename of weapon category e.g "weapon_melee"
     * @param imageURL    URL to image
     * @param image       Weapon image
     * @param attachments Map of attachments available for the weapon codename -> attachment
     */
    public Weapon(String codename, String name, String category, String imageURL, BufferedImage image, HashMap<String, Attachment> attachments) {
        this.codename = codename;
        this.name = name;
        this.category = category;
        this.type = TYPE.discernType(category);
        this.imageURL = imageURL;
        this.image = image;
        this.attachments = attachments;
    }

    /**
     * Get the URL to the weapon image
     *
     * @return URL to weapon image
     */
    public String getImageURL() {
        return imageURL;
    }

    /**
     * Check if the weapon has any equipable attachments
     * Some secondary weapons/all equipment have no attachments
     *
     * @return Weapon has attachments
     */
    public boolean hasEquipableAttachments() {
        return !attachments.isEmpty();
    }

    /**
     * Get an attachment for the weapon by its codename e.g "gripang" to retrieve the
     * Commando Foregrip attachment
     *
     * @param codename Attachment codename
     * @return Attachments
     */
    public Attachment getAttachmentByCodename(String codename) {
        return attachments.get(codename);
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
