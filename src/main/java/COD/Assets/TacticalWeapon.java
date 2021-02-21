package COD.Assets;

import java.awt.image.BufferedImage;
import java.util.HashMap;

/**
 * Tactical equipment has an extra property that other weapons do not have
 */
public class TacticalWeapon extends Weapon {
    private final String property;

    /**
     * Create a weapon
     *
     * @param codename Codename of weapon e.g "equip_flash"
     * @param name     Real name of weapon e.g "Flash Grenade"
     * @param imageURL URL to image
     * @param property Unique property of tactical weapon e.g "Hits"
     * @param image    Weapon image
     */
    public TacticalWeapon(String codename, String name, String imageURL, String property, BufferedImage image) {
        super(codename, name, CATEGORY.TACTICALS, imageURL, image, new HashMap<>(), new HashMap<>());
        this.property = property;
    }

    /**
     * Return if the tactical weapon has a unique property
     *
     * @return Tactical weapon has unique property
     */
    public boolean hasExtraProperty() {
        return property != null;
    }

    /**
     * Get the unique property name of the tactical weapon
     *
     * @return Unique property name
     */
    public String getProperty() {
        return property;
    }
}
