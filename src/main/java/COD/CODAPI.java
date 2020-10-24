package COD;

import Network.NetworkInfo;
import Network.NetworkRequest;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class CODAPI {
    private static final String MODERN_WARFARE_URL = NetworkInfo.getAddress() + ":8080/DiscordBotAPI/api/modernwarfare/";

    /**
     * Get a player's Modern Warfare stats
     *
     * @param name     Player name
     * @param platform Player platform
     * @return Player stats
     */
    public static String getMWStats(String name, String platform) {
        String json = null;
        try {
            String nameEncode = encodeName(name);
            json = new NetworkRequest(
                    MODERN_WARFARE_URL + nameEncode + "/" + platform,
                    false
            ).get();
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        return json;
    }

    /**
     * Get a player's match history
     *
     * @param name     Player name
     * @param platform Player platform
     * @return Player match history
     */
    public static String getMWMatchHistory(String name, String platform) {
        String json = null;
        try {
            String nameEncode = encodeName(name);
            json = new NetworkRequest(
                    MODERN_WARFARE_URL + "/history/" + nameEncode + "/" + platform,
                    false)
                    .get();
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
    private static String encodeName(String name) throws UnsupportedEncodingException {
        return URLEncoder.encode(name, "UTF-8").replaceAll("\\+", "%20");
    }
}
