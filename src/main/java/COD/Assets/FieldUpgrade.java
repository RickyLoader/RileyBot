package COD.Assets;

import java.awt.image.BufferedImage;
import java.util.Objects;

public class FieldUpgrade extends CODAsset {
    private final String propertyName;

    /**
     * Create a killstreak
     *
     * @param codename     Codename of field upgrade e.g "super_deadsilence"
     * @param name         Real name of field upgrade e.g "Dead Silence"
     * @param propertyName Property name e.g "Projectiles Destroyed"
     * @param image        Image of the field upgrade
     */
    public FieldUpgrade(String codename, String name, String propertyName, BufferedImage image) {
        super(codename, name, image);
        this.propertyName = propertyName;
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

    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof FieldUpgrade)) {
            return false;
        }
        FieldUpgrade fieldUpgrade = (FieldUpgrade) obj;
        return fieldUpgrade.getCodename().equals(getCodename());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getCodename());
    }
}