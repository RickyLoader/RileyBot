package Instagram;

/**
 * Instagram user
 */
public class User {
    private final String profileUrl, imageUrl, fullName, username;
    private final long id;

    /**
     * Create an Instagram user
     *
     * @param id       Unique ID of the user
     * @param imageUrl URL to the user's profile image
     * @param fullName Full name - e.g "Dave Dobbyn"
     * @param username @username - e.g "davedobbyn"
     */
    public User(long id, String imageUrl, String fullName, String username) {
        this.id = id;
        this.profileUrl = getProfileUrlFromUsername(username);
        this.imageUrl = imageUrl;
        this.fullName = fullName;
        this.username = username;
    }

    /**
     * Get the URL to a user's profile from their username.
     * This is just for display purposes and doesn't guarantee the user exists.
     *
     * @param username Username to append to URL
     * @return URL to user's profile
     */
    public static String getProfileUrlFromUsername(String username) {
        return Instagram.BASE_URL + username;
    }

    /**
     * Get the unique ID of the user
     *
     * @return User ID
     */
    public long getId() {
        return id;
    }

    /**
     * Get a URL to the user's profile image
     *
     * @return URL to profile image
     */
    public String getImageUrl() {
        return imageUrl;
    }

    /**
     * Get a URL to the user's profile
     *
     * @return URL to profile
     */
    public String getProfileUrl() {
        return profileUrl;
    }

    /**
     * Get the full name - this is the display name of the user.
     *
     * @return Full name - e.g "Dave Dobbyn"
     */
    public String getFullName() {
        return fullName;
    }

    /**
     * Get the username - this is the @username of the user.
     *
     * @return Username - e.g "davedobbyn"
     */
    public String getUsername() {
        return username;
    }
}
