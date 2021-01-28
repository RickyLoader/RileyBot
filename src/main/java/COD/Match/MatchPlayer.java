package COD.Match;

import Command.Structure.CODLookupCommand.PLATFORM;

/**
 * Hold information on a player in a COD match
 */
public class MatchPlayer {
    private final String name;
    private final PLATFORM platform;
    private String uno;

    /**
     * Create the match player
     *
     * @param name     Player name
     * @param platform Player platform
     */
    public MatchPlayer(String name, PLATFORM platform) {
        this.name = name;
        this.platform = platform;
    }

    /**
     * Get the player's name
     *
     * @return Player name
     */
    public String getName() {
        return name;
    }

    /**
     * Set the uno identifier of the player
     *
     * @param uno Uno identifier
     */
    public void setUno(String uno) {
        this.uno = uno;
    }

    /**
     * Get the uno identifier of the player
     *
     * @return Uno identifier
     */
    public String getUno() {
        return uno;
    }

    /**
     * Get the player's platform
     *
     * @return Player platform
     */
    public PLATFORM getPlatform() {
        return platform;
    }
}
