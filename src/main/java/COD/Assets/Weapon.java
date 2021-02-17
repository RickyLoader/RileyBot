package COD.Assets;

import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Objects;

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
        TACTICAL,
        UNKNOWN;

        /**
         * Get a weapon type for the given category
         *
         * @param category Type for given category
         * @return Weapon type by category
         */
        public static TYPE discernType(String category) {
            if(category == null) {
                return UNKNOWN;
            }
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
                    return "Primary Weapon";
                case SECONDARY:
                    return "Secondary Weapon";
                case LETHAL:
                    return "Lethal Equipment";
                default:
                    return "Tactical Equipment";
            }
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
     * Create a missing weapon
     *
     * @param codename Weapon codename
     * @param category Weapon category
     * @param image    Weapon image
     */
    public Weapon(String codename, String category, BufferedImage image) {
        this(codename, "MISSING: " + codename, category, null, image, new HashMap<>());
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

    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof Weapon)) {
            return false;
        }
        Weapon weapon = (Weapon) obj;
        return weapon.getCodename().equals(codename);
    }

    @Override
    public int hashCode() {
        return Objects.hash(codename);
    }
}
