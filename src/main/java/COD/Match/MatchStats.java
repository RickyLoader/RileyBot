package COD.Match;

import COD.Assets.Map;
import COD.Assets.Mode;
import Command.Structure.EmbedHelper;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Hold data on a match played
 */
public class MatchStats {
    private final Date start, end;
    private final RESULT result;
    private final Map map;
    private final Mode mode;
    private final String id;
    private final long duration;
    private final Score score;
    private final MatchPlayer mainPlayer;
    private String displayImageURL;

    public enum RESULT {
        WIN,
        LOSS,
        DRAW,
        FORFEIT
    }

    /**
     * Initialise the required values for a match
     *
     * @param id         Unique match id
     * @param map        Map the match was played on
     * @param mode       Match mode
     * @param start      Start data
     * @param end        End date
     * @param result     Result of match
     * @param mainPlayer Player who requested match stats
     * @param score      Match score
     */
    public MatchStats(String id, Map map, Mode mode, Date start, Date end, RESULT result, MatchPlayer mainPlayer, Score score) {
        this.id = id;
        this.map = map;
        this.mode = mode;
        this.start = start;
        this.end = end;
        this.duration = end.getTime() - start.getTime();
        this.result = result;
        this.mainPlayer = mainPlayer;
        this.score = score;
        this.displayImageURL = map.getLoadingImageURL();
    }

    /**
     * Get the display image URL for the match - by default the map loading screen image
     *
     * @return Display image URL
     */
    public String getDisplayImageURL() {
        return displayImageURL;
    }

    /**
     * Switch the display image URL between the map compass and map loading screen image
     */
    public void switchDisplayImageURL() {
        this.displayImageURL = displayingLoadingImage() ? map.getCompassImageURL() : map.getLoadingImageURL();
    }

    /**
     * Check if the currently displayed image URL is the map's loading image
     *
     * @return Currently displaying map loading image
     */
    public boolean displayingLoadingImage() {
        return this.displayImageURL.equals(map.getLoadingImageURL());
    }

    /**
     * Get the player who the match stats belong to
     *
     * @return Match player
     */
    public MatchPlayer getMainPlayer() {
        return mainPlayer;
    }

    /**
     * Get the match score
     *
     * @return Match score
     */
    public String getScore() {
        return score.getScore();
    }

    /**
     * Get the match gamemode
     *
     * @return Mode
     */
    public Mode getMode() {
        return mode;
    }

    /**
     * Get the map that the match was played on
     *
     * @return Map
     */
    public Map getMap() {
        return map;
    }

    /**
     * Get the match ID
     *
     * @return Match ID
     */
    public String getId() {
        return id;
    }

    /**
     * Get the date, duration, map, mode, and result of the match
     *
     * @return Date and duration
     */
    public String getMatchSummary() {
        return "**ID**: " + id +
                "\n**Date**: " + getDateString() +
                "\n**Time**: " + getTimeString() +
                "\n**Duration**: " + getMatchDurationString() +
                "\n\n**Mode**: " + mode.getName() +
                "\n**Map**: " + map.getName() +
                "\n**K/D**: " + mainPlayer.getKillDeathSummary();
    }

    /**
     * Get the date of the match formatted to a String
     *
     * @return Date String
     */
    public String getDateString() {
        return new SimpleDateFormat("dd/MM/yyyy").format(start);
    }

    /**
     * Get the time of the match formatted to a String
     *
     * @return Time String
     */
    public String getTimeString() {
        return new SimpleDateFormat("HH:mm:ss").format(start);
    }

    /**
     * Get the match duration formatted to a String
     *
     * @return Match duration String
     */
    public String getMatchDurationString() {
        return EmbedHelper.formatDuration(duration);
    }

    /**
     * Get date of match start
     *
     * @return Match start
     */
    public Date getEnd() {
        return end;
    }

    /**
     * Get date of match end
     *
     * @return Match end
     */
    public Date getStart() {
        return start;
    }

    /**
     * Get the match result - win, loss, draw, forfeit
     *
     * @return Match result
     */
    public RESULT getResult() {
        return result;
    }

    /**
     * Check if the player completed the match
     *
     * @return Player completed match
     */
    public boolean playerCompleted() {
        return result != RESULT.FORFEIT;
    }
}