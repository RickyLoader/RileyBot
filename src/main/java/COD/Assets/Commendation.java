package COD.Assets;

import java.awt.image.BufferedImage;

/**
 * Hold commendation information
 */
public class Commendation {
    private final String codename, title, desc;
    private final BufferedImage image;

    /**
     * Create a commendation
     *
     * @param codename Codename of commendation e.g "noDeathsFromBehind"
     * @param title    Real name of commendation e.g "Sixth Sense"
     * @param desc     Description of commendation e.g "No deaths from behind"
     * @param image    Image of commendation
     */
    public Commendation(String codename, String title, String desc, BufferedImage image) {
        this.codename = codename;
        this.title = title;
        this.desc = desc;
        this.image = image;
    }

    /**
     * Get the image of the commendation
     *
     * @return Commendation image
     */
    public BufferedImage getImage() {
        return image;
    }

    /**
     * Get the description of the commendation (what it is awarded for)
     *
     * @return Description of commendation
     */
    public String getDesc() {
        return desc;
    }

    /**
     * Get the codename of the commendation e.g "noDeathsFromBehind"
     *
     * @return Codename of commendation
     */
    public String getCodename() {
        return codename;
    }

    /**
     * Get the title of the commendation (The name)
     *
     * @return Name of commendation
     */
    public String getTitle() {
        return title;
    }
}