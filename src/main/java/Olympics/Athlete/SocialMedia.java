package Olympics.Athlete;

/**
 * Athlete social media
 */
public class SocialMedia {
    private final PLATFORM platform;
    private final String url;

    public enum PLATFORM {
        FACEBOOK,
        TWITTER,
        WEBSITE,
        INSTAGRAM,
        UNKNOWN;

        /**
         * Get a platform by name
         *
         * @param name Name of platform e.g "FACEBOOK"
         * @return Platform from name or UNKNOWN
         */
        public static PLATFORM fromName(String name) {
            try {
                return PLATFORM.valueOf(name);
            }
            catch(IllegalArgumentException e) {
                switch(name) {
                    case "FACEBOOK_PAGE":
                    case "FACEBOOK_TEAM":
                        return FACEBOOK;
                    case "INSTAGRAM_TEAM":
                        return INSTAGRAM;
                    default:
                        return UNKNOWN;
                }
            }
        }
    }

    /**
     * Create a social media connection for an athlete.
     *
     * @param platform Platform - e.g INSTAGRAM
     * @param url      URL to the athlete's profile on the platform
     */
    public SocialMedia(PLATFORM platform, String url) {
        this.platform = platform;
        this.url = url;
    }

    /**
     * Get the URL to the athlete's profile on the platform
     *
     * @return Athlete profile URL
     */
    public String getUrl() {
        return url;
    }

    /**
     * Get the platform - e.g INSTAGRAM
     *
     * @return Platform
     */
    public PLATFORM getPlatform() {
        return platform;
    }
}
