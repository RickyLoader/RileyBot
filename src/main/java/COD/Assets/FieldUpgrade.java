package COD.Assets;

import java.awt.image.BufferedImage;
import java.util.Objects;

public class FieldUpgrade {
    private final String codename, name, propertyName;
    private final BufferedImage image;

    /**
     * Create a killstreak
     *
     * @param codename     Codename of field upgrade e.g "super_deadsilence"
     * @param name         Real name of field upgrade e.g "Dead Silence"
     * @param propertyName Property name e.g "Projectiles Destroyed"
     * @param image        Image of the field upgrade
     */
    public FieldUpgrade(String codename, String name, String propertyName, BufferedImage image) {
        this.codename = codename;
        this.name = name;
        this.propertyName = propertyName;
        this.image = image;
    }

    /**
     * Return whether field upgrade has a property
     *
     * @return Presence of property
     */
    public boolean hasProperty() {
        return propertyName != null;
    }

    /**
     * Get the name of the property
     *
     * @return Property name
     */
    public String getPropertyName() {
        return propertyName;
    }

    /**
     * Get the codename of the field upgrade e.g "super_deadsilence"
     *
     * @return Codename of field upgrade
     */
    public String getCodename() {
        return codename;
    }

    /**
     * Get the image of the field upgrade
     *
     * @return Field upgrade image
     */
    public BufferedImage getImage() {
        return image;
    }

    /**
     * Get the name of the field upgrade
     *
     * @return Name of field upgrade
     */
    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof FieldUpgrade)) {
            return false;
        }
        FieldUpgrade fieldUpgrade = (FieldUpgrade) obj;
        return fieldUpgrade.getCodename().equals(codename);
    }

    @Override
    public int hashCode() {
        return Objects.hash(codename);
    }
}