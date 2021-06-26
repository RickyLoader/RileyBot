package COD;

import Command.Structure.EmbedHelper;
import Network.NetworkRequest;

import static Command.Structure.CODLookupCommand.*;

public class CODAPI {
    private static final String BASE_URL = "http:192.168.1.110:8080/DiscordBotAPI/api/";
    private static final String MODERN_WARFARE_URL = BASE_URL + "modernwarfare/";
    private static final String COLD_WAR_URL = BASE_URL + "coldwar/";

    /**
     * Get a player's Modern Warfare stats
     *
     * @param name     Player name
     * @param platform Player platform
     * @return Player stats
     */
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
     * Get a player's Modern Warfare match history
     *
     * @param name     Player name
     * @param platform Player platform
     * @return Player match history
     */
    public static String getMWMatchHistory(String name, PLATFORM platform) {
        return getMatchHistory(name, platform, MODERN_WARFARE_URL);
    }

    /**
     * Get a player's Cold War match history
     *
     * @param name     Player name
     * @param platform Player platform
     * @return Player match history
     */
    public static String getCWMatchHistory(String name, PLATFORM platform) {
        return getMatchHistory(name, platform, COLD_WAR_URL);
    }

    /**
     * Get the details of a Modern Warfare match
     *
     * @param matchId  Match Id
     * @param platform Player platform
     * @return Player match
     */
    public static String getMWMatch(String matchId, PLATFORM platform) {
        return getMatch(matchId, platform, MODERN_WARFARE_URL);
    }

    /**
     * Get the details of a Cold War match
     *
     * @param matchId  Match Id
     * @param platform Player platform
     * @return Player match
     */
    public static String getCWMatch(String matchId, PLATFORM platform) {
        return getMatch(matchId, platform, COLD_WAR_URL);
    }

    /**
     * Get a player's match history
     *
     * @param name     Player name
     * @param platform Player platform
     * @param gameUrl  Base URL for game
     * @return Player match history
     */
    private static String getMatchHistory(String name, PLATFORM platform, String gameUrl) {
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
     * @return Match JSON
     */
    public static String getMatch(String matchId, PLATFORM platform, String gameUrl) {
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
     * Get the list of players in a modern warfare match
     *
     * @param matchID  Match id
     * @param platform Match platform
     * @return Match players
     */
    public static String getMWMatchPlayers(String matchID, PLATFORM platform) {
        return new NetworkRequest(
                MODERN_WARFARE_URL + "match/" + matchID + "/" + platform.name().toLowerCase(),
                false
        ).get().body;
    }

    /**
     * Get the list of players in a cold war match
     *
     * @param matchID  Match id
     * @param platform Match platform
     * @return Match players
     */
    public static String getCWMatchPlayers(String matchID, PLATFORM platform) {
        return new NetworkRequest(
                COLD_WAR_URL + "match/" + matchID + "/" + platform.name().toLowerCase(),
                false
        ).get().body;
    }

    /**
     * URL encode the given name
     *
     * @param name Name to encode
     * @return URL encoded name
     */
    private static String encodeName(String name) {
        return EmbedHelper.urlEncode(name).replaceAll("\\+", "%20");
    }
}
