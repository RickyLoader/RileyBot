package Riot.Valorant.Assets;

import java.awt.image.BufferedImage;

/**
 * Valorant game asset - agents, weapons, etc
 */
public class ValorantAsset {
    private final String id, name;
    private final BufferedImage image;

    /**
     * Create a Valorant asset
     *
     * @param id    Unique ID of the asset, usually just the name in lowercase - e.g "bucky"
     * @param name  Name of the asset - e.g "Bucky"
     * @param image Image of the asset
     */
    public ValorantAsset(String id, String name, BufferedImage image) {
        this.id = id;
        this.name = name;
        this.image = image;
    }

    /**
     * Create a Valorant asset without an ID (use the lowercase name as the ID)
     *
     * @param name  Name of the asset - e.g "Bucky"
     * @param image Image of the asset
     */
    public ValorantAsset(String name, BufferedImage image) {
        this(name.toLowerCase(), name, image);
    }

    /**
     * Get an image of the asset
     *
     * @return Asset image
     */
    public BufferedImage getImage() {
        return image;
    }

    /**
     * Get the name of the asset - e.g "Bucky"
     *
     * @return Asset name
     */
    public String getName() {
        return name;
    }

    /**
     * Get the unique ID of the asset, usually just the name in lowercase - e.g "bucky"
     *
     * @return Asset ID
     */
    public String getId() {
        return id;
    }
}
