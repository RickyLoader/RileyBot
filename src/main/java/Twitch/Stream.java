package Twitch;

import java.text.NumberFormat;
import java.util.Date;

/**
 * Twitch.tv stream
 */
public class Stream {
    private final String title;
    private final Game game;
    private final Date started;
    private final int viewers;

    /**
     * Create a stream
     *
     * @param title   Stream title
     * @param game    Game being played in stream
     * @param started Date of stream start
     * @param viewers Number of viewers watching the stream
     */
    public Stream(String title, Game game, Date started, int viewers) {
        this.title = title;
        this.game = game;
        this.started = started;
        this.viewers = viewers;
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
