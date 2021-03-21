package Valheim;

import java.util.Date;

/**
 * Valheim server connection
 */
public class PlayerConnection {
    private final SteamProfile steamProfile;
    private Date date;
    private Character character;
    private STATUS status;

    public enum STATUS {
        CONNECTING,
        ONLINE
    }

    /**
     * Create the connecting player
     *
     * @param steamProfile Player connecting
     * @param date         Date of connection attempt
     */
    public PlayerConnection(SteamProfile steamProfile, Date date) {
        this.steamProfile = steamProfile;
        this.status = STATUS.CONNECTING;
        this.date = date;
    }

    /**
     * Set the player as connected
     *
     * @param character Connected character
     * @param date      Date of connection
     */
    public void connect(Character character, Date date) {
        this.character = character;
        this.status = STATUS.ONLINE;
        this.date = date;
    }

    /**
     * Get the date of the attempted/completed connection
     *
     * @return Date of connection
     */
    public Date getDate() {
        return date;
    }

    /**
     * Get the connected character
     *
     * @return Character
     */
    public Character getCharacter() {
        return character;
    }

    /**
     * Get the steam profile of the connected/connecting player
     *
     * @return Steam profile
     */
    public SteamProfile getSteamProfile() {
        return steamProfile;
    }

    /**
     * Get the connection status of the player
     *
     * @return Connection status
     */
    public STATUS getStatus() {
        return status;
    }
}
