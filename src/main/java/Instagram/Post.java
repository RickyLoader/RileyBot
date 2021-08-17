package Instagram;

import Facebook.SocialResponse;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Date;

/**
 * Instagram post
 */
public class Post {
    private final User user;
    private final MEDIA_TYPE type;
    private final String caption, url;
    private final Location location;
    private final SocialResponse socialResponse;
    private final ArrayList<Media> media;
    private final Date datePosted;
    private final ArrayList<String> mentionedUsernames;

    public enum MEDIA_TYPE {
        CAROUSEL(8),
        IMAGE(1),
        VIDEO(2),
        UNKNOWN(-1);

        private final int code;

        /**
         * Create the post type
         *
         * @param code API code for post type
         */
        MEDIA_TYPE(int code) {
            this.code = code;
        }

        /**
         * Get the post type code
         *
         * @return Post type code
         */
        public int getCode() {
            return code;
        }

        /**
         * Get the post type from the given API code.
         *
         * @param code API code for post type
         * @return Post type from code
         */
        public static MEDIA_TYPE fromCode(int code) {
            for(MEDIA_TYPE type : MEDIA_TYPE.values()) {
                if(type.getCode() == code) {
                    return type;
                }
            }
            return UNKNOWN;
        }
    }

    /**
     * Create an Instagram post
     *
     * @param user               User who created the post
     * @param caption            Caption of the post (optional)
     * @param url                URL to the post
     * @param type               Post type
     * @param location           Location tag on the post
     * @param socialResponse     Social response to the post - comments & likes
     * @param datePosted         Date posted
     * @param mentionedUsernames List of mentioned usernames within the post
     * @param media              List of media within the post
     */
    public Post(User user, @Nullable String caption, String url, MEDIA_TYPE type, @Nullable Location location, SocialResponse socialResponse, Date datePosted, ArrayList<String> mentionedUsernames, ArrayList<Media> media) {
        this.user = user;
        this.caption = caption;
        this.url = url;
        this.type = type;
        this.location = location;
        this.socialResponse = socialResponse;
        this.datePosted = datePosted;
        this.mentionedUsernames = mentionedUsernames;
        this.media = media;
    }

    /**
     * Get a list of unique usernames mentioned in the post
     *
     * @return List of mentioned usernames
     */
    public ArrayList<String> getMentionedUserNames() {
        return mentionedUsernames;
    }

    /**
     * Check if the post has any mentioned users
     *
     * @return Post has mentioned users
     */
    public boolean hasMentionedUsers() {
        return !mentionedUsernames.isEmpty();
    }

    /**
     * Get the date that the post was created
     *
     * @return Date of post
     */
    public Date getDatePosted() {
        return datePosted;
    }

    /**
     * Get the social response to the post - comments & likes
     *
     * @return Social response
     */
    public SocialResponse getSocialResponse() {
        return socialResponse;
    }

    /**
     * Get the tagged location on the post
     *
     * @return Tagged location
     */
    public Location getLocation() {
        return location;
    }

    /**
     * Check if the post had a tagged location
     *
     * @return Tagged location
     */
    public boolean hasLocation() {
        return location != null;
    }

    /**
     * Get a list of media within the post
     *
     * @return List of post media
     */
    public ArrayList<Media> getMedia() {
        return media;
    }

    /**
     * Get the URL to the post
     *
     * @return URL to post
     */
    public String getUrl() {
        return url;
    }

    /**
     * Get the caption of the post
     *
     * @return Post caption
     */
    public String getCaption() {
        return caption;
    }

    /**
     * Check if the post has a caption
     *
     * @return Post has caption
     */
    public boolean hasCaption() {
        return caption != null;
    }

    /**
     * Get the media type
     *
     * @return Media type
     */
    public MEDIA_TYPE getType() {
        return type;
    }

    /**
     * Get the user who created the post
     *
     * @return Instagram user
     */
    public User getUser() {
        return user;
    }
}
