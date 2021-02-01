package COD.Assets;

import java.awt.image.BufferedImage;

/**
 * Hold weapon attachment information
 */
public class Attachment {
    private final String codename, name;
    private final BufferedImage image;
    private final CATEGORY category;

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
        EXTRAPSTL,
        PUMPGRIP,
        GUARD,
        PUMP,
        BOLT,
        CABLE,
        BOLTACTION,
        UNKNOWN
    }

    /**
     * Create a weapon attachment
     *
     * @param codename Codename of attachment e.g "gripang"
     * @param name     Real name of attachment e.g "Commando Foregrip"
     * @param category Attachment category
     * @param image    Attachment image
     */
    public Attachment(String codename, String name, CATEGORY category, BufferedImage image) {
        this.codename = codename;
        this.name = name;
        this.image = image;
        this.category = category;
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
