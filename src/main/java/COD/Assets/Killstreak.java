package COD.Assets;

import java.awt.image.BufferedImage;

/**
 * Hold killstreak information
 */
public class Killstreak {
    private final String codename, name, statName;
    private final BufferedImage image;

    /**
     * Create a killstreak
     *
     * @param codename Codename of streak e.g "radar_drone_overwatch"
     * @param name     Real name of streak e.g "Personal Radar"
     * @param statName Name of provided stat e.g kills/assists/..
     * @param image    Killstreak image
     */
    public Killstreak(String codename, String name, String statName, BufferedImage image) {
        this.codename = codename;
        this.name = name;
        this.statName = statName;
        this.image = image;
    }

    /**
     * Get the killstreak name
     *
     * @return Killstreak name
     */
    public String getName() {
        return name;
    }

    /**
     * Get the codename of the killstreak e.g "radar_drone_overwatch"
     *
     * @return Codename of killstreak
     */
    public String getCodename() {
        return codename;
    }

    /**
     * Get the killstreak image
     *
     * @return Killstreak image
     */
    public BufferedImage getImage() {
        return image;
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