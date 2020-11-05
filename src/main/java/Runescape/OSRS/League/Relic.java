package Runescape.OSRS.League;

/**
 * Hold data on a relic from OSRS Trailblazer League
 */
public class Relic {
    private final int id, index;
    private final String name, imagePath;
    public static final String lockedRelic = "locked.png", res = "/Runescape/OSRS/League/Relics/";

    /**
     * Create a relic
     *
     * @param id    Relic id
     * @param index Relic index within tier
     * @param name  Relic name
     */
    public Relic(int id, int index, String name) {
        this.id = id;
        this.imagePath = res + id + ".png";
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
