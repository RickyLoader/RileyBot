package Twitch;

import java.text.NumberFormat;

/**
 * Twitch.tv streamer
 */
public class Streamer {
    private final String loginName, displayName, id, language, thumbnail, url;
    private final Stream stream;
    private final int followers;

    /**
     * Create a Streamer from the builder values
     *
     * @param builder Builder to use values from
     */
    private Streamer(StreamerBuilder builder) {
        this.loginName = builder.loginName;
        this.displayName = builder.displayName;
        this.id = builder.id;
        this.language = builder.language;
        this.thumbnail = builder.thumbnail;
        this.stream = builder.stream;
        this.url = builder.url;
        this.followers = builder.followers;
    }

    public static class StreamerBuilder {
        private String loginName, displayName, id, language, thumbnail, url;
        private Stream stream;
        private int followers;

        /**
         * Set the streamer login name - e.g "loserfruit".
         * This is the name of the streamer used to login.
         *
         * @param loginName Login name
         * @return Builder
         */
        public StreamerBuilder setLoginName(String loginName) {
            this.loginName = loginName;
            this.url = "https://www.twitch.tv/" + loginName;
            return this;
        }

        /**
         * Set the current livestream being broadcast by the streamer
         *
         * @param stream Stream
         * @return Builder
         */
        public StreamerBuilder setStream(Stream stream) {
            this.stream = stream;
            return this;
        }

        /**
         * Set the streamer display name - e.g "Loserfruit"
         * This is the name of the streamer as displayed on Twitch (custom capitalisation)
         *
         * @param displayName Display name
         * @return Builder
         */
        public StreamerBuilder setDisplayName(String displayName) {
            this.displayName = displayName;
            return this;
        }

        /**
         * Set the unique id of the streamer
         *
         * @param id Unique id
         * @return Builder
         */
        public StreamerBuilder setId(String id) {
            this.id = id;
            return this;
        }

        /**
         * Set the streamer language code - e.g "en"
         *
         * @param language Language code
         * @return Builder
         */
        public StreamerBuilder setLanguage(String language) {
            this.language = language;
            return this;
        }

        /**
         * Set the streamer profile thumbnail URL
         *
         * @param thumbnail Profile thumbnail URL
         * @return Builder
         */
        public StreamerBuilder setThumbnail(String thumbnail) {
            this.thumbnail = thumbnail;
            return this;
        }

        /**
         * Set the total number of followers for the streamer
         *
         * @param followers Total number of followers
         * @return Builder
         */
        public StreamerBuilder setFollowers(int followers) {
            this.followers = followers;
            return this;
        }

        /**
         * Build a Streamer from the builder values
         *
         * @return Streamer from builder values
         */
        public Streamer build() {
            return new Streamer(this);
        }
    }

    /**
     * Get the URL to the streamer's Twitch.tv page
     *
     * @return URL to streamer's Twitch.tv page
     */
    public String getUrl() {
        return url;
    }

    /**
     * Get the unique id of the streamer
     *
     * @return Unique streamer id
     */
    public String getId() {
        return id;
    }

    /**
     * Get the total number of followers for the streamer formatted as a String
     *
     * @return Formatted follower String
     */
    public String formatFollowers() {
        NumberFormat format = NumberFormat.getInstance();
        format.setGroupingUsed(true);
        return format.format(followers);
    }

    /**
     * Get the total number of followers for the streamer
     *
     * @return Total number of followers
     */
    public int getFollowers() {
        return followers;
    }

    /**
     * Get the name of the streamer as displayed on Twitch - e.g "Loserfruit"
     *
     * @return Display name
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Get the language code of the streamer
     *
     * @return Language code
     */
    public String getLanguage() {
        return language;
    }

    /**
     * Get the login name of the streamer  - e.g "loserfruit".
     *
     * @return Login name
     */
    public String getLoginName() {
        return loginName;
    }

    /**
     * Get the thumbnail URL of the streamer profile
     *
     * @return Profile thumbnail URL
     */
    public String getThumbnail() {
        return thumbnail;
    }

    /**
     * Check whether the streamer is currently streaming
     *
     * @return Streamer is livestreaming
     */
    public boolean isStreaming() {
        return stream != null;
    }

    /**
     * Get the stream that is currently being broadcast by the streamer
     *
     * @return Stream being broadcast
     */
    public Stream getStream() {
        return stream;
    }
}
