package COD.Assets;

import java.awt.image.BufferedImage;

public class TacticalWeapon extends Weapon {
    private final String property;

    /**
     * Create a weapon
     *
     * @param codename Codename of weapon e.g "iw8_me_akimboblunt"
     * @param name     Real name of weapon e.g "Kali Sticks"
     * @param category Codename of weapon category e.g "weapon_melee"
     * @param property Unique property of tactical weapon e.g "Hits"
     * @param image    Weapon image
     */
    public TacticalWeapon(String codename, String name, String category, String property, BufferedImage image) {
        super(codename, name, category, image);
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
