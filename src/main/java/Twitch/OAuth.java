package Twitch;

import Network.NetworkRequest;
import Network.Secret;
import org.json.JSONObject;

/**
 * Twitch oAuth
 */
public class OAuth {
    private final String clientId, clientSecret;
    private String accessToken;
    private long generatedAt;

    /**
     * Generate an OAuth token and remember the timestamp
     *
     * @param clientId     Twitch client id
     * @param clientSecret Twitch client secret
     */
    public OAuth(String clientId, String clientSecret) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        generateOAuthToken();
    }

    /**
     * Generate/regenerate the oAuth token
     */
    private void generateOAuthToken() {
        String url = "https://id.twitch.tv/oauth2/token?client_id="
                + Secret.TWITCH_CLIENT_ID
                + "&client_secret="
                + Secret.TWITCH_CLIENT_SECRET
                + "&grant_type=client_credentials";
        JSONObject response = new JSONObject(new NetworkRequest(url, false).post().body);
        this.accessToken = response.getString("access_token");
        this.generatedAt = System.currentTimeMillis();
    }

    /**
     * Get the oAuth access token.
     * If it has been more than an hour since the token was generated,
     * regenerate the token first before returning.
     *
     * @return oAuth access token
     */
    public String getAccessToken() {
        if(System.currentTimeMillis() - generatedAt > 3600000) {
            generateOAuthToken();
        }
        return accessToken;
    }

    /**
     * Get the Twitch client id
     *
     * @return Twitch client id
     */
    public String getClientId() {
        return clientId;
    }

    /**
     * Get the Twitch client secret
     *
     * @return Twitch client secret
     */
    public String getClientSecret() {
        return clientSecret;
    }
}
