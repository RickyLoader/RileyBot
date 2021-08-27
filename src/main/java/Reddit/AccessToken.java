package Reddit;

import java.util.Date;

/**
 * Reddit login access token & timeout
 */
public class AccessToken {
    private final Date expiresAt;
    private final String token;

    /**
     * Create the Reddit access token
     *
     * @param token     Access token
     * @param expiresAt Date of expiry
     */
    public AccessToken(String token, Date expiresAt) {
        this.token = token;
        this.expiresAt = expiresAt;
    }

    /**
     * Check if the token has expired
     *
     * @return Token has expired
     */
    public boolean isExpired() {
        return new Date().after(expiresAt);
    }

    /**
     * Get the access token
     *
     * @return Access token
     */
    public String getToken() {
        return token;
    }
}
