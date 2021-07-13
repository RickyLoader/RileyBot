package Reddit;

import org.jetbrains.annotations.Nullable;

/**
 * Reddit video post content
 */
public class VideoPostContent extends PostContent {
    private final String noAudioUrl;
    private final String downloadUrl;

    /**
     * Create the Video post content
     *
     * @param downloadUrl Optional URL to download the video with audio included
     * @param noAudioUrl  URL to the video (does not include audio)
     */
    public VideoPostContent(@Nullable String downloadUrl, String noAudioUrl) {
        super(TYPE.VIDEO);
        this.downloadUrl = downloadUrl;
        this.noAudioUrl = noAudioUrl;
    }

    /**
     * Get the URL to the video (does not include audio)
     *
     * @return No audio URL to video
     */
    public String getNoAudioUrl() {
        return noAudioUrl;
    }

    /**
     * Get the URL to download the video (including audio)
     *
     * @return URL to download video (with audio)
     */
    public String getDownloadUrl() {
        return downloadUrl;
    }

    /**
     * Check if there is a URL available to download the video with audio.
     *
     * @return Download URL available
     */
    public boolean hasDownloadUrl() {
        return downloadUrl != null;
    }
}
