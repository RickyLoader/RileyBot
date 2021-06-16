package COD.Assets;

import java.awt.image.BufferedImage;

/**
 * Hold commendation information
 */
public class Commendation extends CODAsset {
    private final String desc;

    /**
     * Create a commendation
     *
     * @param codename Codename of commendation e.g "noDeathsFromBehind"
     * @param name     Real name of commendation e.g "Sixth Sense"
     * @param desc     Description of commendation e.g "No deaths from behind"
     * @param image    Image of commendation
     */
    public Commendation(String codename, String name, String desc, BufferedImage image) {
        super(codename, name, image);
        this.desc = desc;
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
     * Check if the query is found in the commendation name or description
     *
     * @param query Query to check for in the commendation name or description
     * @return Query found in name or description
     */
    @Override
    public boolean isPartialMatch(String query) {
        return super.isPartialMatch(query) || desc.toLowerCase().contains(query);
    }
}