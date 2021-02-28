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
}
