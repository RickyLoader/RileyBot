package TikTokAPI;

import org.jetbrains.annotations.Nullable;

/**
 * TikTok creator
 */
public class Creator {
    private final String id, name, signature, profileUrl;
    private final SocialStats stats;
    private final byte[] thumbnailImage;
    public static final String DEFAULT_THUMBNAIL_URL = "https://i.imgur.com/0wdF0nI.jpg";

    /**
     * Create a creator.
     *
     * @param id             Unique ID of the creator - e.g "davedobbyn"
     * @param name           Display name of the creator - e.g "Dave Dobbyn"
     * @param signature      Profile signature - e.g "Check out these really cool TikToks!"
     * @param profileUrl     URL to the creator's profile page
     * @param thumbnailImage Creator's profile thumbnail (may be null)
     * @param stats          Creator's social stats - followers, likes, etc
     */
    public Creator(String id, String name, @Nullable String signature, String profileUrl, byte[] thumbnailImage, SocialStats stats) {
        this.id = id;
        this.name = name;
        this.signature = signature;
        this.profileUrl = profileUrl;
        this.thumbnailImage = thumbnailImage;
        this.stats = stats;
    }

    /**
     * Get the social stats of the creator - followers, likes, etc.
     *
     * @return Creator social stats
     */
    public SocialStats getStats() {
        return stats;
    }

    /**
     * Get the unique ID of the creator - e.g "davedobbyn"
     *
     * @return Unique ID
     */
    public String getId() {
        return id;
    }

    /**
     * Get the the creator's profile thumbnail
     * This may be null if the thumbnail was unable to be downloaded.
     *
     * @return Profile thumbnail
     */
    public byte[] getThumbnailImage() {
        return thumbnailImage;
    }

    /**
     * Check if the creator has a profile thumbnail
     *
     * @return Creator has profile thumbnail
     */
    public boolean hasThumbnailImage() {
        return thumbnailImage != null;
    }

    /**
     * Check if the creator has a profile signature
     *
     * @return Creator has a profile signature
     */
    public boolean hasSignature() {
        return signature != null;
    }

    /**
     * Get the display name of the creator - e.g "Dave Dobbyn"
     *
     * @return Creator display name
     */
    public String getName() {
        return name;
    }

    /**
     * Get the URL to the creator's profile page
     *
     * @return Profile page URL
     */
    public String getProfileUrl() {
        return profileUrl;
    }

    /**
     * Get the profile signature - e.g "Check out these really cool TikToks!"
     * This may be null if the creator has not set a profile signature.
     *
     * @return Profile signature
     */
    public String getSignature() {
        return signature;
    }
}
