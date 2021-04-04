package Photography;

/**
 * Photo from the database
 */
public class Photo {
    private final int id;
    private final String url;

    /**
     * Create the photo
     *
     * @param id  Unique id
     * @param url URL to the photo
     */
    public Photo(int id, String url) {
        this.id = id;
        this.url = url;
    }

    /**
     * Get the URL to the photo
     *
     * @return Photo URL
     */
    public String getUrl() {
        return url;
    }

    /**
     * Get the unique id of the photo
     *
     * @return Unique id
     */
    public int getId() {
        return id;
    }
}
