package Valheim;

/**
 * Valheim character
 */
public class Character {
    private final long zdoid;
    private final String name;

    /**
     * Create the Valheim server character
     *
     * @param zdoid Character id on server - generated at connect and used as a session id (destroyed at disconnect)
     * @param name  Character name
     */
    public Character(long zdoid, String name) {
        this.zdoid = zdoid;
        this.name = name;
    }

    /**
     * Get the character name
     *
     * @return Character name
     */
    public String getName() {
        return name;
    }

    /**
     * Get the player session id - Randomly generated at connection to the server and destroyed at disconnect
     *
     * @return Player session id
     */
    public long getZdoid() {
        return zdoid;
    }
}
