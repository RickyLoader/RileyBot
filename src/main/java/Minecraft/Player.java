package Minecraft;

/**
 * Minecraft player
 */
public class Player {
    private final String name, id;

    /**
     * Create a Minecraft player
     *
     * @param name Player name
     * @param id   Player UUID
     */
    public Player(String name, String id) {
        this.name = name;
        this.id = id;
    }

    /**
     * Get the player UUID
     *
     * @return Player UUID
     */
    public String getId() {
        return id;
    }

    /**
     * Get the player name
     *
     * @return Player name
     */
    public String getName() {
        return name;
    }

    /**
     * Get the URL to the player's profile details on NameMC.
     *
     * @return URL to player's profile details
     * @see <a href="https://namemc.com/">For details</a>
     */
    public String getDetailsUrl() {
        return "https://namemc.com/profile/" + id;
    }
}
