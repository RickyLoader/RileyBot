package Valheim;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

/**
 * Valheim server pending connections
 */
public class PendingConnections {
    private final HashMap<Long, PlayerConnection> pending = new HashMap<>();
    private final ArrayList<Long> steamIds = new ArrayList<>();

    /**
     * Stop a failed connection with the given steam id
     *
     * @param steamId Steam id of failed connection
     * @return Failed player connection
     */
    public PlayerConnection failConnection(long steamId) {
        steamIds.remove(steamId);
        return pending.remove(steamId);
    }

    /**
     * Get the list of pending connections
     *
     * @return List of pending connections
     */
    public ArrayList<PlayerConnection> getConnections() {
        return new ArrayList<>(pending.values());
    }

    /**
     * Mark the earliest pending connection as completed with the given character.
     * Successful connections have no ties to pending connections, it has to be assumed that the earliest
     * pending connection is the most recent successful connection.
     * Add the character name to the known list of character names for the steam profile
     *
     * @param character Connected character
     * @param date      Date of connection
     * @return Completed connection
     */
    public PlayerConnection completeEarliestConnection(Character character, Date date) {
        PlayerConnection connected = pending.remove(steamIds.remove(0));
        connected.connect(character, date);
        connected.getSteamProfile().addCharacterName(character.getName());
        return connected;
    }

    /**
     * Add a pending connection to the server
     *
     * @param steamProfile Steam profile connecting
     * @param date         Date of pending connection start
     * @return Pending connection
     */
    public PlayerConnection addConnection(SteamProfile steamProfile, Date date) {
        PlayerConnection connection = new PlayerConnection(steamProfile, date);
        steamIds.add(steamProfile.getId());
        pending.put(steamProfile.getId(), connection);
        return connection;
    }
}
