package Facebook;

import java.util.ArrayList;

/**
 * Facebook post media
 */
public class Attachments {
    private final ArrayList<String> images;
    private final ArrayList<VideoAttachment> videos;

    /**
     * Create the attachment lists
     */
    public Attachments() {
        this.images = new ArrayList<>();
        this.videos = new ArrayList<>();
    }

    /**
     * Add an image to the image attachments list
     *
     * @param image URL to image
     */
    public void addImage(String image) {
        this.images.add(image);
    }

    /**
     * Add a video attachment to the video attachments list
     *
     * @param videoAttachment Video attachment
     */
    public void addVideo(VideoAttachment videoAttachment) {
        this.videos.add(videoAttachment);
    }

    /**
     * Get the attached images
     *
     * @return Attached images
     */
    public ArrayList<String> getImages() {
        return images;
    }

    /**
     * Get the attached videos
     *
     * @return Attached videos
     */
    public ArrayList<VideoAttachment> getVideos() {
        return videos;
    }

    /**
     * Check if the attachments contain any videos
     *
     * @return Attachments contain videos
     */
    public boolean hasVideos() {
        return !videos.isEmpty();
    }

    /**
     * Check if the attachments contain any images
     *
     * @return Attachments contain images
     */
    public boolean hasImages() {
        return !images.isEmpty();
    }

    /**
     * Attachment video
     */
    public static class VideoAttachment {
        private final byte[] video;
        private final String thumbnail;

        /**
         * Create a video attachment
         *
         * @param video     Video source
         * @param thumbnail Video thumbnail URL
         */
        public VideoAttachment(byte[] video, String thumbnail) {
            this.video = video;
            this.thumbnail = thumbnail;
        }

        /**
         * Get the URL to the video thumbnail
         *
         * @return Thumbnail URL
         */
        public String getThumbnail() {
            return thumbnail;
        }

        /**
         * Check if the video attachment has the video
         * It may not if the video was too large.
         *
         * @return Attachment has video
         */
        public boolean hasVideo() {
            return video != null;
        }

        /**
         * Get the attachment video
         *
         * @return Attachment video
         */
        public byte[] getVideo() {
            return video;
        }
    }
}
