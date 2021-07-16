package News;

import org.jetbrains.annotations.Nullable;

/**
 * News author
 */
public class Author {
    private final String name, imageUrl, profileUrl;
    private static final String DEFAULT_AUTHOR_IMAGE_URL = "https://i.imgur.com/0wdF0nI.jpg";

    /**
     * Create a news author
     *
     * @param name       Name - e.g "Dave Dobbyn"
     * @param imageUrl   Optional URL to an image of the author (set to a default image if not provided)
     * @param profileUrl Optional URL to the author's page
     */
    public Author(String name, @Nullable String imageUrl, @Nullable String profileUrl) {
        this.name = name;
        this.imageUrl = (imageUrl == null || imageUrl.isEmpty()) ? DEFAULT_AUTHOR_IMAGE_URL : imageUrl;
        this.profileUrl = profileUrl;
    }

    /**
     * Get a URL to the author's profile
     *
     * @return URL to author's profile
     */
    public String getProfileUrl() {
        return profileUrl;
    }

    /**
     * Check if the author has a URL to their profile
     *
     * @return Author has profile URL
     */
    public boolean hasProfileUrl() {
        return profileUrl != null;
    }

    /**
     * Get the name of the author
     *
     * @return Author name
     */
    public String getName() {
        return name;
    }

    /**
     * Get the URL to an image of the author (if the provided URL was null, this is a URL to a default image)
     *
     * @return URL to author image
     */
    public String getImageUrl() {
        return imageUrl;
    }
}
