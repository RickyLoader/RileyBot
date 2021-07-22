package R34;

/**
 * R34 image tag
 */
public class Tag {
    private final int posts;
    private final String name;
    private final String type;

    /**
     * Create an image tag
     *
     * @param name  Name of the tag - eg "cool_tag"
     * @param type  Type of the tag - e.g "general"
     * @param posts Number of posts with the tag
     */
    public Tag(String name, String type, int posts) {
        this.name = name;
        this.type = type;
        this.posts = posts;
    }

    /**
     * Get the name of the tag
     *
     * @return Tag name - e.g "cool_tag"
     */
    public String getName() {
        return name;
    }

    /**
     * Get the tag type
     *
     * @return Tag type - e.g "general"
     */
    public String getType() {
        return type;
    }

    /**
     * Get the total number of posts with this tag.
     *
     * @return Number of posts with this tag
     */
    public int getTotalPosts() {
        return posts;
    }
}
