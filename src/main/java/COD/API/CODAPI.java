package COD.API;

import COD.API.CODStatsManager.PLATFORM;
import Command.Structure.EmbedHelper;
import Network.NetworkRequest;
import Network.Secret;
import org.jetbrains.annotations.Nullable;

public class CODAPI {
    private static final String
            BASE_URL = "http://" + Secret.LOCAL_IP + ":8080" + Secret.LOCAL_API_PATH,
            MODERN_WARFARE_URL = BASE_URL + "modernwarfare/",
            COLD_WAR_URL = BASE_URL + "coldwar/";
    public static final String API_FAILURE_MESSAGE = "Failed to communicate with API, try again later.";

    /**
     * Get a player's Modern Warfare stats JSON
     *
     * @param name     Player name
     * @param platform Player platform
     * @return Player stats JSON or null
     */
    @Nullable
    public static String getMWStats(String name, PLATFORM platform) {
        String json = null;
        try {
            String nameEncode = encodeName(name);
            json = new NetworkRequest(
                    MODERN_WARFARE_URL + nameEncode + "/" + platform.name().toLowerCase(),
                    false
            ).get().body;
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        return json;
    }

    /**
     * Get a player's Modern Warfare match history JSON
     *
     * @param name     Player name
     * @param platform Player platform
     * @return Player match history JSON or null
     */
    @Nullable
    public static String getMWMatchHistoryJson(String name, PLATFORM platform) {
        return getMatchHistoryJson(name, platform, MODERN_WARFARE_URL);
    }

    /**
     * Get a player's Cold War match history JSON
     *
     * @param name     Player name
     * @param platform Player platform
     * @return Player match history JSON or null
     */
    @Nullable
    public static String getCWMatchHistoryJson(String name, PLATFORM platform) {
        return getMatchHistoryJson(name, platform, COLD_WAR_URL);
    }

    /**
     * Get the details of a Modern Warfare match
     *
     * @param matchId  Match Id
     * @param platform Player platform
     * @return Match JSON or null
     */
    @Nullable
    public static String getMWMatch(String matchId, PLATFORM platform) {
        return getMatch(matchId, platform, MODERN_WARFARE_URL);
    }

    /**
     * Get the details of a Cold War match
     *
     * @param matchId  Match Id
     * @param platform Player platform
     * @return Match JSON or null
     */
    @Nullable
    public static String getCWMatch(String matchId, PLATFORM platform) {
        return getMatch(matchId, platform, COLD_WAR_URL);
    }

    /**
     * Get a player's match history JSON
     *
     * @param name     Player name
     * @param platform Player platform
     * @param gameUrl  Base URL for game
     * @return Player match history JSON or null
     */
    @Nullable
    private static String getMatchHistoryJson(String name, PLATFORM platform, String gameUrl) {
        String json = null;
        try {
            String nameEncode = encodeName(name);
            json = new NetworkRequest(
                    gameUrl + "history/" + nameEncode + "/" + platform.name().toLowerCase(),
                    false)
                    .get().body;
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        return json;
    }

    /**
     * Get the JSON of a match
     *
     * @param matchId  Match Id
     * @param platform Player platform
     * @param gameUrl  Base URL for game
     * @return Match JSON or null
     */
    @Nullable
    private static String getMatch(String matchId, PLATFORM platform, String gameUrl) {
        String json = null;
        try {
            json = new NetworkRequest(
                    gameUrl + "match/" + matchId + "/" + platform.name().toLowerCase(),
                    false
            ).get().body;
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        return json;
    }

    /**
     * URL encode the given name
     *
     * @param name Name to encode
     * @return URL encoded name
     */
    public static String encodeName(String name) {
        return EmbedHelper.urlEncode(name).replaceAll("\\+", "%20");
    }
}
