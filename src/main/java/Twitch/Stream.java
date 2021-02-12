package Twitch;

import java.util.Date;

/**
 * Twitch.tv stream
 */
public class Stream {
    private final String title;
    private final Game game;
    private final Date started;

    /**
     * Create a stream
     *
     * @param title   Stream title
     * @param game    Game being played in stream
     * @param started Date of stream start
     */
    public Stream(String title, Game game, Date started) {
        this.title = title;
        this.game = game;
        this.started = started;
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
