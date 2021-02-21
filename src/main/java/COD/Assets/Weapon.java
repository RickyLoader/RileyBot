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
    private final HashMap<Integer, Variant> variants;

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

        public static final String
                CODENAME_SNIPER = "weapon_sniper",
                CODENAME_LMG = "weapon_lmg",
                CODENAME_ASSAULT_RIFLE = "weapon_assault_rifle",
                CODENAME_OTHER = "weapon_other",
                CODENAME_SHOTGUN = "weapon_shotgun",
                CODENAME_SMG = "weapon_smg",
                CODENAME_MARKSMAN = "weapon_marksman",
                CODENAME_LAUNCHER = "weapon_launcher",
                CODENAME_PISTOL = "weapon_pistol",
                CODENAME_MELEE = "weapon_melee",
                CODENAME_TACTICALS = "tacticals",
                CODENAME_LETHALS = "lethals",
                CODENAME_UNKNOWN = "unknown";

        /**
         * Get the codename for the category
         *
         * @return Category codename
         */
        public String getCodename() {
            switch(this) {
                case LMG:
                    return CODENAME_LMG;
                case SMG:
                    return CODENAME_SMG;
                case MELEE:
                    return CODENAME_MELEE;
                case OTHER:
                    return CODENAME_OTHER;
                case PISTOL:
                    return CODENAME_PISTOL;
                case SNIPER:
                    return CODENAME_SNIPER;
                case LETHALS:
                    return CODENAME_LETHALS;
                case SHOTGUN:
                    return CODENAME_SHOTGUN;
                case LAUNCHER:
                    return CODENAME_LAUNCHER;
                case MARKSMAN:
                    return CODENAME_MARKSMAN;
                case TACTICALS:
                    return CODENAME_TACTICALS;
                case ASSAULT_RIFLE:
                    return CODENAME_ASSAULT_RIFLE;
                default:
                    return CODENAME_UNKNOWN;
            }
        }

        /**
         * Get a weapon category by name
         *
         * @param category Category name e.g - "weapon_smg" or category shorthand e.g - "iw8_sn_romeo700" -> "sn"
         * @return Category e.g - SMG
         */
        public static CATEGORY discernCategory(String category) {
            switch(category) {
                case CODENAME_SNIPER:
                case "sn":
                    return SNIPER;
                case CODENAME_LMG:
                case "lm":
                    return LMG;
                case CODENAME_ASSAULT_RIFLE:
                case "ar":
                    return ASSAULT_RIFLE;
                case CODENAME_OTHER:
                    return OTHER;
                case CODENAME_SHOTGUN:
                case "sh":
                    return SHOTGUN;
                case CODENAME_SMG:
                case "sm":
                    return SMG;
                case CODENAME_MARKSMAN:
                    return MARKSMAN;
                case CODENAME_LAUNCHER:
                case "la":
                    return LAUNCHER;
                case CODENAME_PISTOL:
                case "pi":
                    return PISTOL;
                case CODENAME_MELEE:
                case "":
                case "me":
                    return MELEE;
                case CODENAME_LETHALS:
                    return LETHALS;
                case CODENAME_TACTICALS:
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
     * @param attachments Map of attachments available for the weapon. Weapon codename -> attachment
     * @param variants    Map of variants available for the weapon. Variant id -> variant
     */
    public Weapon(String codename, String name, CATEGORY category, String imageURL, BufferedImage image, HashMap<String, Attachment> attachments, HashMap<Integer, Variant> variants) {
        this.codename = codename;
        this.name = name;
        this.category = category;
        this.type = TYPE.discernType(category);
        this.imageURL = imageURL;
        this.image = image;
        this.attachments = attachments;
        this.variants = variants;
    }

    /**
     * Create a missing weapon
     *
     * @param codename Weapon codename
     * @param category Weapon category
     * @param image    Weapon image
     */
    public Weapon(String codename, CATEGORY category, BufferedImage image) {
        this(codename, "MISSING: " + codename, category, null, image, new HashMap<>(), new HashMap<>());
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
     * @return Attachment
     */
    public Attachment getAttachmentByCodename(String codename) {
        return attachments.get(codename);
    }

    /**
     * Get a variant of the weapon by its variant id e.g 21 to retrieve the
     * "Espionage" variant of the combat knife
     *
     * @param variantId Variant id
     * @return Variant
     */
    public Variant getVariantById(int variantId) {
        return variants.get(variantId);
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
