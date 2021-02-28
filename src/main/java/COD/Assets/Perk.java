package COD.Assets;

import java.awt.image.BufferedImage;
import java.util.Objects;

/**
 * Hold perk information
 */
public class Perk extends CODAsset {
    private final CATEGORY category;

    public enum CATEGORY {
        BLUE,
        RED,
        YELLOW,
        UNKNOWN
    }

    /**
     * Create a perk
     *
     * @param codename Codename of attachment e.g "specialty_covert_ops"
     * @param name     Real name of perk e.g "Cold Blooded"
     * @param category Perk category
     * @param image    Perk image
     */
    public Perk(String codename, String name, CATEGORY category, BufferedImage image) {
        super(codename, name, image);
        this.category = category;
    }

    /**
     * Get the perk category
     *
     * @return Perk category
     */
    public CATEGORY getCategory() {
        return category;
    }


    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof Perk)) {
            return false;
        }
        Perk perk = (Perk) obj;
        return perk.getCodename().equals(getCodename());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getCodename());
    }
}
