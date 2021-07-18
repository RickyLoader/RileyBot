package Reddit;

import org.jetbrains.annotations.Nullable;

/**
 * Reddit video post content
 */
public class VideoPostContent extends PostContent {
    private final String noAudioUrl;
    private final String downloadUrl;
    private final Boolean audioStatus;

    /**
     * Create the Video post content
     *
     * @param downloadUrl Optional URL to download the video with audio included (this is for downloading and will not embed)
     * @param noAudioUrl  URL to the video (does not include audio)
     * @param audioStatus Video post had audio (if null this was unable to be determined)
     */
    public VideoPostContent(@Nullable String downloadUrl, String noAudioUrl, @Nullable Boolean audioStatus) {
        super(TYPE.VIDEO);
        this.downloadUrl = downloadUrl;
        this.noAudioUrl = noAudioUrl;
        this.audioStatus = audioStatus;
    }

    /**
     * Check if the video post had an audio track.
     * This only indicates the video had an audio track, not that it is available.
     * When null, it was unable to be determined whether the post had an audio track
     *
     * @return Video post had audio track
     */
    @Nullable
    public Boolean getAudioStatus() {
        return audioStatus;
    }

    /**
     * Get the URL to the video (does not include audio)
     * This can be downloaded or embedded.
     *
     * @return No audio URL to video
     */
    public String getNoAudioUrl() {
        return noAudioUrl;
    }

    /**
     * Get the URL to download the video (including audio).
     * This is for downloading and cannot be embedded.
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
