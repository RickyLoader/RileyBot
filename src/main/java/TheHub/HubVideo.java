package TheHub;

import COD.Assets.Ratio;

import java.util.Arrays;

/**
 * Hold details on a hub video
 */
public class HubVideo {
    private final String date;
    private final VideoInfo videoInfo;
    private final long views;
    private final Ratio rating;
    private final Channel channel;
    private final String[] cast, categories;

    /**
     * Create the video
     *
     * @param videoInfo  Video info - title & URL details
     * @param date       Upload date -  expressed as a past tense e.g - "4 years ago"
     * @param views      Number of views
     * @param rating     Like/dislike ratio
     * @param channel    Channel where video is uploaded
     * @param cast       Array of cast names
     * @param categories Array of video categories
     */
    public HubVideo(VideoInfo videoInfo, String date, long views, Ratio rating, Channel channel, String[] cast, String[] categories) {
        this.videoInfo = videoInfo;
        this.date = date;
        this.views = views;
        this.rating = rating;
        this.channel = channel;
        this.cast = cast;
        this.categories = categories;
    }

    /**
     * Get an array of video categories
     *
     * @param number Number of categories to retrieve
     * @return Array of video categories
     */
    public String[] getCategories(int number) {
        return getArray(categories, number);
    }

    /**
     * Check if the video has an array of categories
     *
     * @return Video has categories
     */
    public boolean hasCategories() {
        return categories.length != 0;
    }

    /**
     * Check if the video has cast names
     *
     * @return Video has cast names
     */
    public boolean hasCast() {
        return cast.length != 0;
    }

    /**
     * Get the video info - title and URL details
     *
     * @return Video info
     */
    public VideoInfo getVideoInfo() {
        return videoInfo;
    }

    /**
     * Get the given number of elements from the given array
     *
     * @param array Array to get elements from
     * @param num   Number of elements to retrieve (will default to array size if out of bounds)
     * @return Given number of elements from the array
     */
    private String[] getArray(String[] array, int num) {
        num = num < 0 ? array.length : Math.min(array.length, num);
        return Arrays.copyOfRange(array, 0, num);
    }

    /**
     * Get the date of the video upload - expressed as a past tense e.g - "4 years ago"
     *
     * @return Date of video upload
     */
    public String getDate() {
        return date;
    }

    /**
     * Get the channel where the video is uploaded
     *
     * @return Video upload channel
     */
    public Channel getChannel() {
        return channel;
    }

    /**
     * Get the number of views on the video
     *
     * @return Video views
     */
    public long getViews() {
        return views;
    }

    /**
     * Get the number of likes on the video
     *
     * @return Video likes
     */
    public int getLikes() {
        return rating.getNumerator();
    }

    /**
     * Get the number of dislikes on the video
     *
     * @return Video dislikes
     */
    public int getDislikes() {
        return rating.getDenominator();
    }

    /**
     * Get the formatted like/dislike ratio e.g 89%
     *
     * @return Ratio of likes to dislikes
     */
    public String getLikeRatio() {
        double percent = ((double) rating.getNumerator() + (double) rating.getDenominator()) / 100;
        return (int) ((double) rating.getNumerator() / percent) + "%";
    }

    /**
     * Get an array of cast names
     *
     * @param number Number of cast names to retrieve
     * @return Array of cast names
     */
    public String[] getCast(int number) {
        return getArray(cast, number);
    }

    /**
     * Channel holding video
     */
    public static class Channel {
        private final String name, url, imageUrl;

        /**
         * Create the video channel
         *
         * @param name     Channel name
         * @param url      URL to channel
         * @param imageUrl URL to channel image
         */
        public Channel(String name, String url, String imageUrl) {
            this.name = name;
            this.url = url;
            this.imageUrl = imageUrl;
        }

        /**
         * Get the URL to the channel image
         *
         * @return URL to channel image
         */
        public String getImageUrl() {
            return imageUrl;
        }

        /**
         * Get the URL to the channel
         *
         * @return URL to channel
         */
        public String getUrl() {
            return url;
        }

        /**
         * Get the name of the channel
         *
         * @return Channel name
         */
        public String getName() {
            return name;
        }
    }

    /**
     * Video title & URL details
     */
    public static class VideoInfo {
        private final String title, url, thumbnailUrl;

        /**
         * Create the video info
         *
         * @param title        Video title
         * @param url          URL to video on website
         * @param thumbnailUrl URL to video thumbnail
         */
        public VideoInfo(String title, String url, String thumbnailUrl) {
            this.title = title;
            this.url = url;
            this.thumbnailUrl = thumbnailUrl;
        }

        /**
         * Get the URL to the video thumbnail image
         *
         * @return URL to thumbnail image
         */
        public String getThumbnailUrl() {
            return thumbnailUrl;
        }

        /**
         * Get the URL to the video on the website
         *
         * @return URL to video on website
         */
        public String getUrl() {
            return url;
        }

        /**
         * Get the video title
         *
         * @return Video title
         */
        public String getTitle() {
            return title;
        }
    }
}
