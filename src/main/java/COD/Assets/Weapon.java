package COD.Assets;

import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Objects;

/**
 * Hold weapon information
 */
public class Weapon {
    private final String codename, name, imageURL;
    private final TYPE type;
    private final CATEGORY category;
    private final BufferedImage image;
    private final HashMap<String, Attachment> attachments;

    public enum CATEGORY {
        SNIPER,
        LMG,
        ASSAULT_RIFLE,
        OTHER,
        SHOTGUN,
        SMG,
        MARKSMAN,
        LAUNCHER,
        PISTOL,
        MELEE,
        LETHALS,
        TACTICALS,
        UNKNOWN;

        /**
         * Get a weapon category by name
         *
         * @param category Category name e.g - "weapon_smg" or category shorthand e.g - "iw8_sn_romeo700" -> "sn"
         * @return Category e.g - SMG
         */
        public static CATEGORY discernCategory(String category) {
            switch(category) {
                case "weapon_sniper":
                case "sn":
                    return SNIPER;
                case "weapon_lmg":
                case "lm":
                    return LMG;
                case "weapon_assault_rifle":
                case "ar":
                    return ASSAULT_RIFLE;
                case "weapon_other":
                    return OTHER;
                case "weapon_shotgun":
                case "sh":
                    return SHOTGUN;
                case "weapon_smg":
                case "sm":
                    return SMG;
                case "weapon_marksman":
                    return MARKSMAN;
                case "weapon_launcher":
                case "la":
                    return LAUNCHER;
                case "weapon_pistol":
                case "pi":
                    return PISTOL;
                case "weapon_melee":
                case "":
                case "me":
                    return MELEE;
                case "lethals":
                    return LETHALS;
                case "tacticals":
                    return TACTICALS;
                default:
                    return UNKNOWN;
            }
        }
    }

    public enum TYPE {
        PRIMARY,
        SECONDARY,
        LETHAL,
        TACTICAL,
        UNKNOWN;

        /**
         * Get a weapon type for the given category
         *
         * @param category Weapon category e.g - ASSAULT_RIFLE
         * @return Weapon type e.g - PRIMARY
         */
        public static TYPE discernType(CATEGORY category) {
            if(category == CATEGORY.UNKNOWN) {
                return UNKNOWN;
            }
            switch(category) {
                case SNIPER:
                case LMG:
                case ASSAULT_RIFLE:
                case OTHER:
                case SHOTGUN:
                case SMG:
                case MARKSMAN:
                default:
                    return PRIMARY;
                case LAUNCHER:
                case PISTOL:
                case MELEE:
                    return SECONDARY;
                case LETHALS:
                    return LETHAL;
                case TACTICALS:
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
     * @param category    Weapon category e.g MELEE
     * @param imageURL    URL to image
     * @param image       Weapon image
     * @param attachments Map of attachments available for the weapon codename -> attachment
     */
    public Weapon(String codename, String name, CATEGORY category, String imageURL, BufferedImage image, HashMap<String, Attachment> attachments) {
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
    public Weapon(String codename, CATEGORY category, BufferedImage image) {
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
     * Get the weapon category
     *
     * @return Weapon category
     */
    public CATEGORY getCategory() {
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

    /**
     * Get a category from a weapon codename e.g - "iw8_sn_alpha50" -> "sn" -> SNIPER
     *
     * @param weaponCodename e.g - "iw8_sn_alpha50"
     * @return Weapon category e.g - SNIPER
     */
    public static CATEGORY getCategoryFromWeaponCodename(String weaponCodename) {
        String categoryShorthand = weaponCodename.matches(".*_.*_.*") ? weaponCodename.split("_")[1] : "";
        return CATEGORY.discernCategory(categoryShorthand);
    }

    @Override
    public int hashCode() {
        return Objects.hash(codename);
    }
}
