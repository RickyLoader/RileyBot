package COD.Assets;

import java.awt.image.BufferedImage;
import java.util.Objects;

/**
 * COD weapon variant
 */
public class Variant {
    private final String name;
    private final int id;
    private final BufferedImage image;

    /**
     * Create a weapon variant
     *
     * @param id    Weapon variant id - e.g 21
     * @param name  Weapon variant name - e.g "Espionage"
     * @param image Weapon variant image
     */
    public Variant(int id, String name, BufferedImage image) {
        this.id = id;
        this.name = name;
        this.image = image;
    }

    /**
     * Get the image of the variant
     *
     * @return Variant image
     */
    public BufferedImage getImage() {
        return image;
    }

    /**
     * Get the variant name - e.g "Espionage"
     *
     * @return Variant name
     */
    public String getName() {
        return name;
    }

    /**
     * Get the variant id - e.g 21
     * Placed on weapon name to identify variant - e.g "iw8_knife" -> "iw8_knife_v21"
     *
     * @return Variant id
     */
    public int getId() {
        return id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof Variant)) {
            return false;
        }
        Variant variant = (Variant) obj;
        return id == variant.getId();
    }
}
