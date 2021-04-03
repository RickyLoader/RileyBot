package COD;

import Command.Structure.EmbedHelper;
import Network.NetworkInfo;
import Network.NetworkRequest;

import java.io.UnsupportedEncodingException;

import static Command.Structure.CODLookupCommand.*;

public class CODAPI {
    private static final String BASE_URL = NetworkInfo.getAddress() + ":8080/DiscordBotAPI/api/";
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
     * Get a player's match history
     *
     * @param name     Player name
     * @param platform Player platform
     * @param gameURL  Base URL for game
     * @return Player match history
     */
    private static String getMatchHistory(String name, PLATFORM platform, String gameURL) {
        String json = null;
        try {
            String nameEncode = encodeName(name);
            json = new NetworkRequest(
                    gameURL + "history/" + nameEncode + "/" + platform.name().toLowerCase(),
                    false)
                    .get().body;
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
    private static String encodeName(String name) throws UnsupportedEncodingException {
        return EmbedHelper.urlEncode(name).replaceAll("\\+", "%20");
    }
}
