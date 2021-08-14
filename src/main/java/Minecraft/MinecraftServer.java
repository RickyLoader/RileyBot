package Minecraft;

import Network.NetworkRequest;
import Network.NetworkResponse;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;

import static Command.Structure.ImageLoadingMessage.imageToByteArray;

/**
 * Hold Minecraft server info/players using the Minecraft Server Status API
 */
public class MinecraftServer {
    private static final int
            NO_PORT = 0,
            UNAVAILABLE = -1;
    private static final String
            API_URL = "https://api.mcsrvstat.us/",
            DEFAULT_ICON_URL = "https://i.imgur.com/hpQ2JZo.png", // Default server icon
            UNKNOWN = "?";
    private static final long CACHE_TIME = 300000; // 5 minutes
    private final String addressString;
    private final ArrayList<Player> players;

    private byte[] detailsImage;
    private long lastFetched;
    private boolean online, hasData;
    private int maxPlayers, currentPlayers;
    private String mapName, version, iconUrl, hostname;
    private String[] motd;

    /**
     * Create the Minecraft server
     *
     * @param address IP address/hostname of the server
     * @param port    Server port
     */
    public MinecraftServer(String address, int port) {
        this.addressString = buildAddressString(address, port);
        this.players = new ArrayList<>();

        // Attempt to fetch server data
        refreshServerData();
    }

    /**
     * Create the Minecraft server with a hostname
     *
     * @param hostname Hostname of the server
     */
    public MinecraftServer(String hostname) {
        this(hostname, NO_PORT);
    }

    /**
     * Build the address String, this is in the format "address:port".
     * Port is truncated if unacceptable.
     *
     * @param address IP address/hostname of the server
     * @param port    Server port
     * @return Address String
     */
    private String buildAddressString(String address, int port) {
        String addressString = address;
        if(port != NO_PORT) {
            addressString += ":" + port;
        }
        return addressString;
    }

    /**
     * Get the server image.
     * This is an image similar to how the server appears in the in-game server browser.
     * It contains the server icon, version, name, MOTD, player count, and online status.
     *
     * @return Server image
     */
    public byte[] getDetailsImage() {
        return detailsImage;
    }

    /**
     * Get the message of the day, this is either empty or a small array of Strings
     * where each String is a new line in the MOTD.
     *
     * @return Message of the day
     */
    public String[] getMotd() {
        return hasData ? motd : new String[0];
    }

    /**
     * Check if the server is online.
     *
     * @return Server is online
     */
    public boolean isOnline() {
        return online;
    }

    /**
     * Get the version String e.g "1.12"
     * This is either the version details of the server e.g "1.12" or a
     * question mark e.g "?" if the server data has not been fetched yet.
     *
     * @return Version String
     */
    public String getVersion() {
        return hasData ? version : UNKNOWN;
    }

    /**
     * Get the list of players connected to the server
     *
     * @return List of connected players
     */
    public ArrayList<Player> getPlayers() {
        return players;
    }

    /**
     * Get the map name, this is either the server's map name e.g "clobbering" or the
     * address String e.g "address:port" if the server data has not been fetched yet.
     *
     * @return Map name
     */
    public String getMapName() {
        return hasData ? mapName : getAddressString();
    }

    /**
     * Get the maximum players allowed on the server as a String.
     * This is either the maximum allowed players e.g "12" or a question mark e.g "?" if the server data has not
     * been fetched yet.
     *
     * @return Maximum players String
     */
    public String getMaxPlayersString() {
        return hasData && maxPlayers != UNAVAILABLE ? String.valueOf(maxPlayers) : UNKNOWN;
    }

    /**
     * Get the current player count as a String.
     * This is either the number of currently connected players e.g "5" or a
     * question mark e.g "?" if the server data has not been fetched yet.
     *
     * @return Current player count String
     */
    public String getCurrentPlayerCountString() {
        final int currentPlayers = getCurrentPlayerCount();
        return currentPlayers == UNAVAILABLE ? UNKNOWN : String.valueOf(currentPlayers);
    }

    /**
     * Get the current player count. This is -1 if the server data has not been fetched yet.
     *
     * @return Current player count
     */
    public int getCurrentPlayerCount() {
        return hasData ? currentPlayers : UNAVAILABLE;
    }

    /**
     * Get the URL to the server icon.
     * This will be the URL to the default server icon if the server does not have a custom icon set, or if the
     * server data has not been fetched yet.
     *
     * @return Server icon
     */
    public String getIconUrl() {
        return hasData ? iconUrl : DEFAULT_ICON_URL;
    }

    /**
     * Check if the server has a hostname.
     * Not all servers have a hostname and are instead connected to by IP address.
     *
     * @return Server has a hostname
     */
    public boolean hasHostname() {
        return hostname != null;
    }

    /**
     * Get the address String
     * This is either "address:port" or "hostname" (if available)
     *
     * @return Address String
     */
    public String getAddressString() {
        return hasHostname() ? hostname : addressString;
    }

    /**
     * Get the date that the current data will be invalidated in the server cache.
     * If the data has not been fetched, this will be null.
     *
     * @return Date of cache expiry or null
     */
    @Nullable
    public Date getCacheExpiryDate() {
        if(lastFetched == 0) {
            return null;
        }
        return new Date(lastFetched + CACHE_TIME);
    }

    /**
     * Check if the server has any data.
     * This is true if the server has been seen online at least once.
     * If not, all server values have a default value.
     *
     * @return Server has data
     */
    public boolean hasData() {
        return hasData;
    }

    /**
     * Refresh the server stats & image if it has been more than 5 minutes since the last refresh
     * (stats are cached on the server side for 5 minutes).
     * Generate/re-generate the server image
     */
    public void refreshServerData() {

        // Data is up to date (what we have is still cached on the server side)
        if(lastFetched != 0 && (System.currentTimeMillis() - lastFetched) < CACHE_TIME) {
            return;
        }

        fetchServerStats();

        // Re-generate image after each refresh
        this.detailsImage = imageToByteArray(MOTDBuilder.getInstance().buildImage(this));
    }

    /**
     * Fetch the server stats, this includes the server status, online player count, etc.
     * This may fail if the server does not exist (or the API doesn't respond), so only update the
     * timestamp of the last fetch upon a successful request (allowing another fetch to be performed immediately).
     */
    private void fetchServerStats() {
        NetworkResponse response = new NetworkRequest(API_URL + "2/" + addressString, false).get();

        // No response, leave
        if(response.code == NetworkResponse.TIMEOUT_CODE || response.code == 404) {
            return;
        }

        JSONObject info = new JSONObject(response.body);

        // Timestamp when server cached response (is 0 if the response was not fetched from cache)
        long cacheTime = info.getJSONObject("debug").getLong("cachetime") * 1000;

        // Remember now as the time of the last fetch if the response was not fetched from cache
        lastFetched = cacheTime == 0 ? System.currentTimeMillis() : cacheTime;

        this.online = info.getBoolean("online");

        // Reset player list
        players.clear();
        currentPlayers = 0;

        // First time the server is seen online the data can be grabbed
        if(!hasData && online) {
            hasData = true;
        }

        // Response when server is offline does not include any relevant info, simply indicates the server is offline
        if(!online) {
            return;
        }

        this.version = info.getString("version");

        // Sometimes the version is a massive list of supported versions
        if(version.length() > 10) {
            this.version = "Many";
        }

        // Map name isn't always included, default to "Minecraft Server"
        final String mapKey = "map";
        this.mapName = info.has(mapKey) ? info.getString(mapKey) : "Minecraft Server";

        // MOTD - Message of the day - can be multiple lines
        JSONArray motd = info.getJSONObject("motd").getJSONArray("raw");
        final int length = Math.min(motd.length(), MOTDBuilder.MAX_LINES);

        this.motd = new String[length];
        for(int i = 0; i < length; i++) {
            this.motd[i] = motd.getString(i);
        }

        // Only included when a hostname is detected
        final String hostnameKey = "hostname";
        if(info.has(hostnameKey)) {
            this.hostname = info.getString(hostnameKey);
        }

        /*
         * Server icon is only included if the server has a custom icon, and is provided in base64.
         * When included, use the API endpoint with the server address to get a URL directly to the icon.
         */
        this.iconUrl = info.has("icon")
                ? API_URL + "icon/" + addressString
                : DEFAULT_ICON_URL;

        JSONObject playersData = info.getJSONObject("players");
        this.maxPlayers = playersData.getInt("max");
        this.currentPlayers = playersData.getInt("online");

        // Only included when players are online
        final String playerListKey = "uuid";
        if(playersData.has(playerListKey)) {
            JSONObject playerList = playersData.getJSONObject(playerListKey);
            for(String name : playerList.keySet()) {
                players.add(
                        new Player(
                                name,
                                playerList.getString(name)
                        )
                );
            }
        }
    }
}
