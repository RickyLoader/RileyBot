package COD.Assets;

import java.awt.image.BufferedImage;

/**
 * Hold weapon attachment information
 */
public class Attachment {
    private final String codename, name;
    private final BufferedImage image;
    private final CATEGORY category, blockedCategory;
    private final Attributes attributes;

    public enum CATEGORY {
        GUNPERK,
        OPTIC,
        BACKPIECE,
        MUZZLE,
        REARGRIP,
        UNDERMOUNT,
        FRONTPIECE,
        EXTRA,
        MAGAZINE,
        TRIGGER,
        PUMPGRIP,
        GUARD,
        PUMP,
        BOLT,
        CABLE,
        BOLTACTION,
        UNKNOWN,
        NONE
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
}
