package Instagram;

/**
 * Video media type
 */
public class VideoMedia extends Media {
    private final String thumbnailUrl, videoUrl;

    /**
     * Create the video media
     *
     * @param videoUrl     URL to the video
     * @param thumbnailUrl URL to a thumbnail of the video
     */
    public VideoMedia(String videoUrl, String thumbnailUrl) {
        super(Post.MEDIA_TYPE.VIDEO);
        this.thumbnailUrl = thumbnailUrl;
        this.videoUrl = videoUrl;
    }

    /**
     * Get the URL to the video thumbnail
     *
     * @return URL to video thumbnail
     */
    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    /**
     * Get the URL to the video
     *
     * @return URL to video
     */
    public String getVideoUrl() {
        return videoUrl;
    }
}
