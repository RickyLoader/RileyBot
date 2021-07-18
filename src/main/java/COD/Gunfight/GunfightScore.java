package COD.Gunfight;

import Command.Structure.EmbedHelper;

import java.util.Random;

/**
 * Score for gunfight
 */
public class GunfightScore {
    private int wins, losses, currentStreak, rank, longestStreak;
    private long lastUpdate;

    /**
     * Default constructor - initialise values to zero
     */
    public GunfightScore() {
        this.wins = 0;
        this.losses = 0;
        this.currentStreak = 0;
        this.rank = 0;
        this.longestStreak = 0;
        this.lastUpdate = 0;
    }

    /**
     * Create the score
     *
     * @param score Score to use values from
     */
    public GunfightScore(GunfightScore score) {
        replaceValues(score);
    }

    /**
     * Get a thumbnail image based on the current score performance
     *
     * @return Thumbnail URL
     */
    public String getThumbnail() {
        String[] goodThumb = new String[]{
                "https://bit.ly/2YTzfTQ", // Default price
                "https://bnetcmsus-a.akamaihd.net/cms/blog_header/pv/PV106AQCOXG41591752563326.jpg", // Price in smoke
                "https://i.imgur.com/W3nY6AF.jpg", // Happy price
                "https://static1.gamerantimages.com/wordpress/wp-content/uploads/2020/05/call-of-duty-modern-warfare-nuke-victory-screen.jpg", // VICTORY
        };

        String[] badThumb = new String[]{
                "https://i.imgur.com/AHtBYyn.jpg", // Sad price
                "https://i.ytimg.com/vi/ONzIHOxtQws/maxresdefault.jpg", // Ghost dying
                "https://i.imgur.com/ZgHmHY2.png" // DEFEAT
        };

        Random rand = new Random();
        if(wins == 0 && losses == 0) {
            return "https://bit.ly/2YTzfTQ";// Going dark cunt
        }
        if(wins > losses) {
            return goodThumb[rand.nextInt(goodThumb.length)];
        }
        return badThumb[rand.nextInt(badThumb.length)];
    }

    /**
     * Get the timestamp of the last score update
     *
     * @return Timestamp of the last score update
     */
    public long getLastUpdate() {
        return lastUpdate;
    }

    /**
     * Get the current longest win streak of the session
     *
     * @return Longest win streak
     */
    public int getLongestStreak() {
        return longestStreak;
    }

    /**
     * Get the number of wins
     *
     * @return Number of wins
     */
    public int getWins() {
        return wins;
    }

    /**
     * Get the leaderboard rank
     *
     * @return Leaderboard rank
     */
    public int getRank() {
        return rank;
    }

    /**
     * Get the number of losses
     *
     * @return Number of losses
     */
    public int getLosses() {
        return losses;
    }

    /**
     * Get the current streak
     * This is negative for losses and positive for wins
     *
     * @return Current streak
     */
    public int getCurrentStreak() {
        return currentStreak;
    }

    /**
     * Get the colour to use in the embedded message based on score
     *
     * @return int decimal colour
     */
    public int getColour() {
        if(wins == losses) {
            return EmbedHelper.YELLOW;
        }
        else if(wins < losses) {
            return EmbedHelper.RED;
        }
        else {
            return EmbedHelper.GREEN;
        }
    }

    /**
     * Set the timestamp of the last update
     *
     * @param lastUpdate Timestamp to set
     */
    public void setLastUpdate(long lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    /**
     * Set the leaderboard rank
     *
     * @param rank Leaderboard rank
     */
    public void setRank(int rank) {
        this.rank = rank;
    }

    /**
     * Add a win to the scoreboard, reset streak if on a loss streak.
     */
    public void addWin() {
        if(currentStreak < 0) {
            currentStreak = 0;
        }
        wins++;
        currentStreak++;

        // Keep track of the largest win streak of the session
        if(currentStreak > longestStreak) {
            longestStreak = currentStreak;
        }
    }

    /**
     * Add a loss to the scoreboard, reset streak if on a win streak.
     */
    public void addLoss() {
        if(currentStreak > 0) {
            currentStreak = 0;
        }
        losses++;
        currentStreak--;
    }

    /**
     * Replace the score values with the values of the given score
     *
     * @param score Score to replace values with
     */
    public void replaceValues(GunfightScore score) {
        this.wins = score.getWins();
        this.losses = score.getLosses();
        this.currentStreak = score.getCurrentStreak();
        this.longestStreak = score.getLongestStreak();
        this.lastUpdate = score.getLastUpdate();
        this.rank = score.getRank();
    }
}
