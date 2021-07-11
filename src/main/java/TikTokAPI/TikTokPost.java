package TikTokAPI;

import org.jetbrains.annotations.Nullable;

import java.util.Date;

/**
 * TikTok video & creator data
 */
public class TikTokPost {
    private final byte[] video, previewImage;
    private final String description, postUrl;
    private final SocialResponse socialResponse;
    private final Creator creator;
    private final Date date;
    private final Music music;

    /**
     * Create a TikTok post
     *
     * @param video          Video byte array data
     * @param description    Optional video description
     * @param postUrl        URL to the post on TikTok
     * @param previewImage   Preview image of the video
     * @param socialResponse Likes/comments/shares/plays
     * @param creator        Creator of the post
     * @param music          Music used in the post
     * @param date           Date of the post
     */
    public TikTokPost(byte[] video, @Nullable String description, String postUrl, byte[] previewImage, SocialResponse socialResponse, Creator creator, Music music, Date date) {
        this.video = video;
        this.description = description;
        this.postUrl = postUrl;
        this.previewImage = previewImage;
        this.socialResponse = socialResponse;
        this.creator = creator;
        this.music = music;
        this.date = date;
    }

    /**
     * Get the music used in the post
     *
     * @return Music used
     */
    public Music getMusic() {
        return music;
    }

    /**
     * Get the URL to the post on TikTok
     *
     * @return URL to post
     */
    public String getUrl() {
        return postUrl;
    }

    /**
     * Get the creator of the post
     *
     * @return Post creator
     */
    public Creator getCreator() {
        return creator;
    }

    /**
     * Get the video description
     * This may be null if the video does not have a description.
     *
     * @return Video description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Check if the post has a description for the video
     *
     * @return Post has video description
     */
    public boolean hasDescription() {
        return description != null;
    }

    /**
     * Get the social response to the post - likes/comments/shares/plays
     *
     * @return Social response
     */
    public SocialResponse getSocialResponse() {
        return socialResponse;
    }

    /**
     * Get a preview image of the video
     * This may be null if the image was unable to be downloaded.
     *
     * @return Preview image for video
     */
    public byte[] getPreviewImage() {
        return previewImage;
    }

    /**
     * Get the video data
     * This may be null if the video was unable to be downloaded.
     *
     * @return Video data
     */
    public byte[] getVideo() {
        return video;
    }

    /**
     * Check if the video data exists
     *
     * @return Video data exists
     */
    public boolean hasVideo() {
        return video != null;
    }

    /**
     * Check if the video has a preview image
     *
     * @return Video has preview image
     */
    public boolean hasPreviewImage() {
        return previewImage != null;
    }

    /**
     * Get the date that the post was created
     *
     * @return Creation date
     */
    public Date getDate() {
        return date;
    }
}
