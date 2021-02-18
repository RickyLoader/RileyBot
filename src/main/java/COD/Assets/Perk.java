package COD.Assets;

import java.awt.image.BufferedImage;
import java.util.Objects;

/**
 * Hold perk information
 */
public class Perk {

    private final String name, codename;
    private final BufferedImage image;
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
        this.codename = codename;
        this.name = name;
        this.category = category;
        this.image = image;
    }

    /**
     * Get the perk category
     *
     * @return Perk category
     */
    public CATEGORY getCategory() {
        return category;
    }

    /**
     * Get the image of the perk
     *
     * @return Perk image
     */
    public BufferedImage getImage() {
        return image;
    }

    /**
     * Get the name of the perk
     *
     * @return Perk name
     */
    public String getName() {
        return name;
    }

    /**
     * Get the codename of the perk e.g "specialty_covert_ops"
     *
     * @return Codename of perk
     */
    public String getCodename() {
        return codename;
    }

    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof Perk)) {
            return false;
        }
        Perk perk = (Perk) obj;
        return perk.getCodename().equals(codename);
    }

    @Override
    public int hashCode() {
        return Objects.hash(codename);
    }
}
