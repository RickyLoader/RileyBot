package Valheim;

import Network.NetworkRequest;
import Network.Secret;
import Steam.SteamStore;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

/**
 * Valheim server player list
 */
public class PlayerList {
    private final PendingConnections pendingConnections = new PendingConnections();
    private final HashMap<Long, SteamProfile> steamProfiles = new HashMap<>();
    private final HashMap<String, Character> characters = new HashMap<>();
    private final HashMap<Long, PlayerConnection>
            connectionsByZdoid = new HashMap<>(),
            connectionsBySteamId = new HashMap<>();

    /**
     * Add a connecting steam id to the pending connections
     *
     * @param steamId Steam id of connecting player
     * @param date    Date of pending connection start
     */
    public void connectionStarted(long steamId, Date date) {
        SteamProfile profile = steamProfiles.computeIfAbsent(steamId, this::fetchSteamProfile);

        // Steam API down, try again
        if(profile.isUnknown()) {
            SteamProfile fixed = fixSteamProfile(profile);
            if(fixed != null) {
                profile = fixed;
                steamProfiles.put(steamId, profile);
            }
        }
        pendingConnections.addConnection(profile, date);
    }

    /**
     * Take an unknown Steam profile (when Steam API is down) and copy the known values in to a fixed profile.
     * Return null if the profile remains unknown.
     *
     * @param profile Unknown Steam profile from when Steam API was down
     * @return Fixed steam profile or null
     */
    private SteamProfile fixSteamProfile(SteamProfile profile) {
        SteamProfile updated = fetchSteamProfile(profile.getId());
        // API still down
        if(updated.isUnknown()) {
            return null;
        }
        // New profile with the old character names still intact
        return new SteamProfile(
                profile.getId(),
                updated.getName(),
                updated.getUrl(),
                profile.getCharacterNameList(),
                profile.getCharacterNameSet()
        );
    }

    /**
     * Fetch a steam profile by the unique steam id
     *
     * @param steamId Unique steam id
     * @return Steam profile
     */
    private SteamProfile fetchSteamProfile(long steamId) {
        try {
            String url = SteamStore.STEAM_API_BASE_URL + "ISteamUser/GetPlayerSummaries/v2/?key="
                    + Secret.STEAM_KEY + "&steamids=" + steamId;

            JSONObject response = new JSONObject(
                    new NetworkRequest(url, false).get().body
            ).getJSONObject("response");

            JSONObject player = response.getJSONArray("players").getJSONObject(0);

            return new SteamProfile(
                    steamId,
                    player.getString("personaname"),
                    player.getString("profileurl")
            );
        }
        catch(Exception e) {
            return new SteamProfile(steamId);
        }
    }

    /**
     * Add a newly connected player to the connected players.
     * Map the connected character if it has not been seen before
     *
     * @param zdoid         Session id of connected player
     * @param characterName Character name
     * @param date          Connection date
     */
    public void playerConnected(long zdoid, String characterName, Date date) {
        Character character = getCharacterByName(characterName);
        if(character == null) {
            character = new Character(zdoid, characterName);
            characters.put(characterName.toLowerCase(), character);
        }
        PlayerConnection connected = pendingConnections.completeEarliestConnection(
                character,
                date
        );
        connectionsByZdoid.put(zdoid, connected);
        connectionsBySteamId.put(connected.getSteamProfile().getId(), connected);
    }

    /**
     * Get a list of known characters (characters who have connected to the server)
     *
     * @return List of characters
     */
    public ArrayList<Character> getKnownCharacters() {
        return new ArrayList<>(characters.values());
    }

    /**
     * Get a character by their name
     * Character must have connected to the server before
     *
     * @param name Character name
     * @return Character or null
     */
    public Character getCharacterByName(String name) {
        return characters.get(name.toLowerCase());
    }

    /**
     * Remove a disconnected player via steam id.
     * Player may currently be online or pending connection to the server
     *
     * @param steamId Steam id of disconnected player
     * @return Disconnected player connection
     */
    public PlayerConnection playerDisconnected(long steamId) {
        PlayerConnection connected = connectionsBySteamId.get(steamId);
        if(connected == null) {
            return pendingConnections.failConnection(steamId);
        }
        connectionsBySteamId.remove(steamId);
        connectionsByZdoid.remove(connected.getCharacter().getZdoid());
        return connected;
    }

    /**
     * Get the list of currently connected players
     *
     * @return List of currently connected players
     */
    public ArrayList<PlayerConnection> getConnectedPlayers() {
        return new ArrayList<>(connectionsBySteamId.values());
    }

    /**
     * Get the list of all currently connected and connecting players
     *
     * @return List of all current connections
     */
    public ArrayList<PlayerConnection> getAllConnections() {
        ArrayList<PlayerConnection> connections = getConnectedPlayers();
        connections.addAll(pendingConnections.getConnections());
        return connections;
    }

    /**
     * Get a steam profile by the unique steam id.
     * Profile must have connected/attempted to connect to the server before
     *
     * @param steamId Steam id of profile to get
     * @return Steam profile with given steam id or null
     */
    public SteamProfile getSteamProfileById(long steamId) {
        return steamProfiles.get(steamId);
    }

    /**
     * Get a connected player by their zdoid - session id
     *
     * @param zdoid Player session id
     * @return Player with zdoid or null
     */
    public PlayerConnection getConnectedPlayerByZdoid(long zdoid) {
        return connectionsByZdoid.get(zdoid);
    }
}
