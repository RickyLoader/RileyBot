package COD;

import Command.Structure.EmbedHelper;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import static COD.MWPlayer.*;

/**
 * Hold data on a match played
 */
public class MatchStats {
    private final Date start, end;
    private final long duration, wobblies;
    private final RESULT result;
    private final Map map;
    private final String id, mode, nemesis, mostKilled;
    private final Ratio killDeath, accuracy;
    private final Score score;
    private final int longestStreak, damageDealt, damageReceived, xp;
    private final double distanceTravelled;
    private final MatchPlayer player;
    private Team team1, team2;

    public enum RESULT {
        WIN,
        LOSS,
        DRAW,
        FORFEIT
    }

    /**
     * Match builder
     */
    public static class MatchBuilder {
        private final Date start, end;
        private final long duration;
        private final RESULT result;
        private final String mode, id;
        private final MatchPlayer player;
        private final Map map;
        private String nemesis, mostKilled;
        private Ratio killDeath, accuracy;
        private Score score;
        private int longestStreak, damageDealt, damageReceived, xp;
        private long wobblies;
        private double distanceTravelled;

        /**
         * Initialise the required values of the match
         *
         * @param id     Unique match id
         * @param map    Map the match was played on
         * @param mode   Match mode
         * @param start  Start data
         * @param end    End date
         * @param result Result of match
         * @param player Player who match stats belong to
         */
        public MatchBuilder(String id, Map map, String mode, Date start, Date end, RESULT result, MatchPlayer player) {
            this.id = id;
            this.map = map;
            this.mode = mode;
            this.start = start;
            this.end = end;
            this.duration = end.getTime() - start.getTime();
            this.result = result;
            this.player = player;
        }

        /**
         * Set the kill/death ratio of player during the match
         *
         * @param kd Kill/death ratio
         * @return Builder
         */
        public MatchBuilder setKD(Ratio kd) {
            this.killDeath = kd;
            return this;
        }

        /**
         * Set the accuracy of the player during the match
         *
         * @param accuracy Accuracy
         * @return Builder
         */
        public MatchBuilder setAccuracy(Ratio accuracy) {
            this.accuracy = accuracy;
            return this;
        }

        /**
         * Set the final match score
         *
         * @param score Final match score
         * @return Builder
         */
        public MatchBuilder setMatchScore(Score score) {
            this.score = score;
            return this;
        }

        /**
         * Set the longest streak of the player during the match
         *
         * @param longestStreak Longest streak
         * @return Builder
         */
        public MatchBuilder setLongestStreak(int longestStreak) {
            this.longestStreak = longestStreak;
            return this;
        }

        /**
         * Set the total damage dealt by the player during the match
         *
         * @param damageDealt Total damage dealt
         * @return Builder
         */
        public MatchBuilder setDamageDealt(int damageDealt) {
            this.damageDealt = damageDealt;
            return this;
        }

        /**
         * Set the total received by the player during the match
         *
         * @param damageReceived Total damage received
         * @return Builder
         */
        public MatchBuilder setDamageReceived(int damageReceived) {
            this.damageReceived = damageReceived;
            return this;
        }

        /**
         * Set the distance travelled by the player during the match (in inches)
         *
         * @param distanceTravelledInches Distance travelled in inches
         * @return Builder
         */
        public MatchBuilder setDistanceTravelled(int distanceTravelledInches) {
            this.wobblies = distanceTravelledInches;
            this.distanceTravelled = (distanceTravelledInches * 0.0254); // To metres
            return this;
        }

        /**
         * Set the XP gained by the player from the match
         *
         * @param xp XP gained
         * @return Builder
         */
        public MatchBuilder setXP(int xp) {
            this.xp = xp;
            return this;
        }

        /**
         * Set the player's match nemesis (most killed by)
         *
         * @param nemesis Name of nemesis
         * @return Builder
         */
        public MatchBuilder setNemesis(String nemesis) {
            this.nemesis = nemesis;
            return this;
        }

        /**
         * Set the player's most killed player during the match
         *
         * @param mostKilled Name of most killed player
         * @return Builder
         */
        public MatchBuilder setMostKilled(String mostKilled) {
            this.mostKilled = mostKilled;
            return this;
        }

        /**
         * Build the match
         *
         * @return Match
         */
        public MatchStats build() {
            return new MatchStats(this);
        }
    }

    /**
     * Create a match
     *
     * @param builder Match builder
     */
    private MatchStats(MatchBuilder builder) {
        this.id = builder.id;
        this.map = builder.map;
        this.mode = builder.mode;
        this.start = builder.start;
        this.end = builder.end;
        this.duration = builder.duration;
        this.result = builder.result;
        this.nemesis = builder.nemesis;
        this.mostKilled = builder.mostKilled;
        this.accuracy = builder.accuracy;
        this.killDeath = builder.killDeath;
        this.score = builder.score;
        this.longestStreak = builder.longestStreak;
        this.damageReceived = builder.damageReceived;
        this.damageDealt = builder.damageDealt;
        this.xp = builder.xp;
        this.distanceTravelled = builder.distanceTravelled;
        this.wobblies = builder.wobblies;
        this.player = builder.player;
    }

    /**
     * Get the player who the match stats belong to
     *
     * @return Match player
     */
    public MatchPlayer getPlayer() {
        return player;
    }

    /**
     * Format a distance
     *
     * @param distance Distance to format
     * @param unit     Unit of measurement
     * @return Distance formatted with unit
     */
    private String formatDistance(double distance, String unit) {
        DecimalFormat df = new DecimalFormat("#,### " + unit);
        df.setMaximumFractionDigits(2);
        return df.format(distance);
    }

    /**
     * Get the distance travelled (in metres)
     *
     * @return Metres travelled
     */
    public String getDistanceTravelled() {
        return formatDistance(distanceTravelled, "metres");
    }

    /**
     * Get the distance travelled (in wobblies)
     *
     * @return Wobblies travelled
     */
    public String getWobblies() {
        return formatDistance(wobblies, "wobblies");
    }

    /**
     * Get the total match experience
     *
     * @return Match XP (comma formatted)
     */
    public String getExperience() {
        return commaFormat(xp);
    }

    /**
     * Get the total damage dealt during the match by the player
     *
     * @return Damage dealt (comma formatted)
     */
    public String getDamageDealt() {
        return commaFormat(damageDealt);
    }

    /**
     * Get the total damage the player received during the match
     *
     * @return Damage received (comma formatted)
     */
    public String getDamageReceived() {
        return commaFormat(damageReceived);
    }

    /**
     * Comma format the given value
     *
     * @param value Integer value to format
     * @return Value formatted with comma - 1234 -> 1,234
     */
    private String commaFormat(int value) {
        return new DecimalFormat("#,###").format(value);
    }

    /**
     * Get the longest killstreak the player obtained during the match
     *
     * @return Longest killstreak
     */
    public int getLongestStreak() {
        return longestStreak;
    }

    /**
     * Get the name of the enemy who killed the player the most
     *
     * @return Nemesis
     */
    public String getNemesis() {
        return nemesis;
    }

    /**
     * Get the name of the enemy who was killed by the player the most
     *
     * @return Most killed enemy
     */
    public String getMostKilled() {
        return mostKilled;
    }

    /**
     * Get a String displaying shots hit/shots fired
     *
     * @return Accuracy summary
     */
    public String getShotSummary() {
        if(accuracy == null) {
            return "-";
        }
        return getShotsFired() + "/" + getShotsHit();
    }

    /**
     * Get a String displaying the player accuracy during the match
     *
     * @return Player accuracy
     */
    public String getAccuracySummary() {
        return hasAccuracy() ? accuracy.getRatioPercentage() : "-";
    }

    /**
     * Get the total number of shots fired
     *
     * @return Shots fired
     */
    public int getShotsFired() {
        return accuracy.getDenominator();
    }

    /**
     * Get the total number of shots hit
     *
     * @return Shots hit
     */
    public int getShotsHit() {
        return accuracy.getNumerator();
    }

    /**
     * Return presence of accuracy data
     *
     * @return Accuracy exists
     */
    public boolean hasAccuracy() {
        return accuracy != null;
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
    public String getMode() {
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
     * Get the match kills
     *
     * @return Match kills
     */
    public int getKills() {
        return killDeath.getNumerator();
    }

    /**
     * Get the match deaths
     *
     * @return Match deaths
     */
    public int getDeaths() {
        return killDeath.getDenominator();
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
                "\n**Duration**: " + getDurationString() +
                "\n\n**Mode**: " + mode +
                "\n**Map**: " + map.getName() +
                "\n**K/D**: " + getKillDeathSummary();
    }

    /**
     * Get the kill/death ratio summary
     *
     * @return K/D Ratio
     */
    public String getKillDeathSummary() {
        return killDeath.getNumerator()
                + "/" + killDeath.getDenominator()
                + " (" + killDeath.formatRatio(killDeath.getRatio()) + ")";
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
     * @return Match String
     */
    public String getDurationString() {
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
     * Get team 1
     *
     * @return Team 1
     */
    public Team getTeam1() {
        return team1;
    }

    /**
     * Get team 2
     *
     * @return Team 2
     */
    public Team getTeam2() {
        return team2;
    }

    /**
     * Set the match teams
     *
     * @param team1 Team1
     * @param team2 Team2
     */
    public void setTeams(Team team1, Team team2) {
        this.team1 = team1;
        this.team2 = team2;
    }

    /**
     * Check if the match has team information
     *
     * @return Match has team information
     */
    public boolean hasTeams() {
        return team1 != null && team2 != null;
    }
}