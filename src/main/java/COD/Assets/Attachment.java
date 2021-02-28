package COD.Assets;

import org.jetbrains.annotations.NotNull;

import java.awt.image.BufferedImage;

/**
 * Hold weapon attachment information
 */
public class Attachment implements Comparable<Attachment> {
    private final String codename, name;
    private final BufferedImage image;
    private final CATEGORY category, blockedCategory;
    private final Attributes attributes;

    public enum CATEGORY {
        CABLE,
        MUZZLE,
        FRONTPIECE,
        GUARD,
        PUMP,
        PUMPGRIP,
        UNDERMOUNT,
        EXTRA,
        BOLT,
        MAGAZINE,
        BOLTACTION,
        TRIGGER,
        OPTIC,
        REARGRIP,
        BACKPIECE,
        GUNPERK,
        UNKNOWN,
        NONE;

        /**
         * Get the weapon position (left to right) of the attachment category
         *
         * @return Weapon position
         */
        public int getWeaponPosition() {
            switch(this) {
                case CABLE:
                case MUZZLE:
                    return 0;
                case FRONTPIECE:
                    return 1;
                case GUARD:
                case PUMP:
                case PUMPGRIP:
                case UNDERMOUNT:
                    return 2;
                case EXTRA:
                    return 3;
                case BOLT:
                case MAGAZINE:
                    return 4;
                case BOLTACTION:
                case TRIGGER:
                    return 5;
                case OPTIC:
                    return 6;
                case REARGRIP:
                    return 7;
                case BACKPIECE:
                    return 8;
                case GUNPERK:
                    return 9;
                default:
                    return 10;
            }
        }
    }

    /**
     * Create a weapon attachment
     *
     * @param codename        Codename of attachment e.g "gripang"
     * @param name            Real name of attachment e.g "Commando Foregrip"
     * @param category        Attachment category
     * @param blockedCategory Category which attachment blocks e.g silenced barrels block muzzle attachments
     * @param attributes      Attachment attributes - Increases/Decreases to weapon stats
     * @param image           Attachment image
     */
    public Attachment(String codename, String name, CATEGORY category, CATEGORY blockedCategory, Attributes attributes, BufferedImage image) {
        this.codename = codename;
        this.name = name;
        this.image = image;
        this.category = category;
        this.blockedCategory = blockedCategory;
        this.attributes = attributes;
    }

    /**
     * Get the attachment category which the attachment blocks
     *
     * @return Attachment category blocked by attachment
     */
    public CATEGORY getBlockedCategory() {
        return blockedCategory;
    }

    /**
     * Check if the attachment block another category
     *
     * @return Attachment blocks another category
     */
    public boolean blocksCategory() {
        return blockedCategory != CATEGORY.NONE && blockedCategory != CATEGORY.UNKNOWN;
    }

    /**
     * Get the attachment attributes - Increases/Decreases to weapon stats
     *
     * @return Attachment attributes
     */
    public Attributes getAttributes() {
        return attributes;
    }

    /**
     * Get the attachment category
     *
     * @return Attachment category
     */
    public CATEGORY getCategory() {
        return category;
    }

    /**
     * Get the attachment image
     *
     * @return Attachment image
     */
    public BufferedImage getImage() {
        return image;
    }

    /**
     * Get the codename of the weapon attachment e.g "gripang"
     *
     * @return Codename of attachment
     */
    public String getCodename() {
        return codename;
    }

    /**
     * Get the name of the attachment
     *
     * @return Attachment name
     */
    public String getName() {
        return name;
    }

    @Override
    public int compareTo(@NotNull Attachment o) {
        return category.getWeaponPosition() - o.getCategory().getWeaponPosition();
    }
}
