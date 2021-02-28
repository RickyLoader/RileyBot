package COD.Assets;

import java.awt.image.BufferedImage;

/**
 * Hold killstreak information
 */
public class Killstreak extends CODAsset {
    private final String statName;

    /**
     * Create a killstreak
     *
     * @param codename Codename of streak e.g "radar_drone_overwatch"
     * @param name     Real name of streak e.g "Personal Radar"
     * @param statName Name of provided stat e.g kills/assists/..
     * @param image    Killstreak image
     */
    public Killstreak(String codename, String name, String statName, BufferedImage image) {
        super(codename, name, image);
        this.statName = statName;
    }

    /**
     * Return if the killstreak has an extra stat
     *
     * @return Killstreak has extra stat
     */
    public boolean hasExtraStat() {
        return statName != null;
    }

    /**
     * Get the name of the given stat
     *
     * @return Name of stat
     */
    public String getStatName() {
        return statName;
    }
}