package Runescape.OSRS.League;

import Bot.ResourceHandler;

/**
 * Hold data on a relic from OSRS Trailblazer League
 */
public class Relic {
    private final int id, index;
    private final String name, imagePath;
    public static final String
            LOCKED_RELIC_FILENAME = "locked.png",
            RES = ResourceHandler.OSRS_LEAGUE_PATH + "Relics/";

    /**
     * Create a relic
     *
     * @param id    Relic id
     * @param index Relic index within tier
     * @param name  Relic name
     */
    public Relic(int id, int index, String name) {
        this.id = id;
        this.imagePath = RES + id + ".png";
        this.index = index;
        this.name = name;
    }

    /**
     * Get the path to an image of the relic
     *
     * @return Image path
     */
    public String getImagePath() {
        return imagePath;
    }

    /**
     * Get the name of the relic
     *
     * @return Relic name
     */
    public String getName() {
        return name;
    }

    /**
     * Get the id of the relic
     *
     * @return Relic id
     */
    public int getId() {
        return id;
    }

    /**
     * Get the index of the relic within the tier
     *
     * @return Relic tier index
     */
    public int getTierIndex() {
        return index;
    }
}
