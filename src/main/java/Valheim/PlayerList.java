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
        pendingConnections.addConnection(profile, date);
    }

    /**
     * Fetch a steam profile by the unique steam id
     *
     * @param steamId Unique steam id
     * @return Steam profile or null
     */
    private SteamProfile fetchSteamProfile(long steamId) {
        String url = SteamStore.STEAM_API_BASE_URL +"ISteamUser/GetPlayerSummaries/v2/?key="
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

    /**
     * Add a newly connected player to the connected players.
     *
     * @param zdoid         Session id of connected player
     * @param characterName Character name
     * @param date          Connection date
     */
    public void playerConnected(long zdoid, String characterName, Date date) {
        PlayerConnection connected = pendingConnections.completeEarliestConnection(
                new Character(zdoid, characterName),
                date
        );
        connectionsByZdoid.put(zdoid, connected);
        connectionsBySteamId.put(connected.getSteamProfile().getId(), connected);
    }

    /**
     * Remove a disconnected player via steam id.
     * Player may currently be online or pending connection to the server
     *
     * @param steamId Steam id of disconnected player
     */
    public void playerDisconnected(long steamId) {
        PlayerConnection connected = connectionsBySteamId.get(steamId);
        if(connected == null) {
            pendingConnections.failConnection(steamId);
            return;
        }
        connectionsBySteamId.remove(steamId);
        connectionsByZdoid.remove(connected.getCharacter().getZdoid());
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
     * @return Steam profile with given steam id
     */
    public SteamProfile getSteamProfileById(long steamId) {
        return steamProfiles.get(steamId);
    }

    /**
     * Get a connected player by their steam id
     *
     * @param steamId Player steam id
     * @return Player with steam id or null
     */
    public PlayerConnection getConnectedPlayerBySteamId(long steamId) {
        return connectionsBySteamId.get(steamId);
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
