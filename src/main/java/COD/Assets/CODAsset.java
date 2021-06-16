package COD.Assets;

import java.awt.image.BufferedImage;

/**
 * Base COD asset details
 */
public class CODAsset {
    private final String codename, name;
    private final BufferedImage image;

    /**
     * Create a COD asset
     *
     * @param codename Asset codename - as named in files
     * @param name     Asset name - as named in-game
     * @param image    Asset image
     */
    public CODAsset(String codename, String name, BufferedImage image) {
        this.codename = codename;
        this.name = name;
        this.image = image;
    }

    /**
     * Get the asset image
     *
     * @return Asset image
     */
    public BufferedImage getImage() {
        return image;
    }

    /**
     * Get the codename of the asset - how the asset is named in the files
     *
     * @return Codename of asset
     */
    public String getCodename() {
        return codename;
    }

    /**
     * Get the name of the asset - how the asset is named in-game
     *
     * @return Asset name
     */
    public String getName() {
        return name;
    }

    /**
     * Compare the given query String to the codename of the asset
     * Return true if the query matches the codename.
     *
     * @param query Query to match to the asset codename
     * @return Query matches the asset codename
     */
    public boolean isExactMatch(String query) {
        return codename.equalsIgnoreCase(query);
    }

    /**
     * Compare the given query String to the name/codename of the asset
     * Return true if the query is found in the name or codename.
     *
     * @param query Query to check for in the asset name/codename
     * @return Query is in the asset name/codename
     */
    public boolean isPartialMatch(String query) {
        return name.toLowerCase().contains(query.toLowerCase()) || codename.toLowerCase().contains(query.toLowerCase());
    }
}
