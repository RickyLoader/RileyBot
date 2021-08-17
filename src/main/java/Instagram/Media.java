package Instagram;

import Instagram.Post.MEDIA_TYPE;

/**
 * Instagram media
 */
public class Media {
    private final MEDIA_TYPE type;

    /**
     * Create the Instagram media
     *
     * @param type    Media type
     */
    public Media(MEDIA_TYPE type) {
        this.type = type;
    }

    /**
     * Get the media type
     *
     * @return Media  type
     */
    public MEDIA_TYPE getType() {
        return type;
    }
}
