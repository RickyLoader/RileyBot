package Twitch;

import java.text.NumberFormat;
import java.util.Date;

/**
 * Twitch.tv stream
 */
public class Stream {
    private final String title, thumbnail;
    private final Game game;
    private final Date started;
    private final int viewers;

    /**
     * Create a stream
     *
     * @param title     Stream title
     * @param game      Game being played in stream
     * @param started   Date of stream start
     * @param viewers   Number of viewers watching the stream
     * @param thumbnail URL to the stream thumbnail
     */
    public Stream(String title, Game game, Date started, int viewers, String thumbnail) {
        this.title = title;
        this.game = game;
        this.started = started;
        this.viewers = viewers;
        this.thumbnail = thumbnail;
    }

    /**
     * Get a stream thumbnail from the streamer login name
     *
     * @param loginName Streamer login name
     * @return URL to stream thumbnail
     */
    public static String getThumbnail(String loginName) {
        return "https://static-cdn.jtvnw.net/previews-ttv/live_user_" + loginName + "-440x248.jpg";
    }

    /**
     * Get the total number of viewers for the stream formatted as a String
     *
     * @return Formatted viewer String
     */
    public String formatViewers() {
        NumberFormat format = NumberFormat.getInstance();
        format.setGroupingUsed(true);
        return format.format(viewers);
    }

    /**
     * Get the URL to the stream thumbnail
     *
     * @return Stream thumbnail URL
     */
    public String getThumbnail() {
        return thumbnail;
    }

    /**
     * Get the title of the stream
     *
     * @return Stream title
     */
    public String getTitle() {
        return title;
    }

    /**
     * Get the start date of the stream
     *
     * @return Start date of stream
     */
    public Date getStarted() {
        return started;
    }

    /**
     * Get the game being played on stream
     *
     * @return Game being played on stream
     */
    public Game getGame() {
        return game;
    }
}
