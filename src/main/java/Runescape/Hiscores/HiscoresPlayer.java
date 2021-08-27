package Runescape.Hiscores;

/**
 * Hiscores player listing
 */
public class HiscoresPlayer {
    private final String name;
    private final int rank;

    /**
     * Create a hiscores player listing
     *
     * @param name Player name
     * @param rank Player rank - e.g 123
     */
    public HiscoresPlayer(String name, int rank) {
        this.name = name;
        this.rank = rank;
    }

    /**
     * Get the name of the player
     *
     * @return Player name
     */
    public String getName() {
        return name;
    }

    /**
     * Get the rank of the player - e.g 123
     *
     * @return Player rank
     */
    public int getRank() {
        return rank;
    }
}
