package COD.Match;

import COD.Assets.Map;
import COD.Assets.Mode;
import COD.Assets.Ratio;
import Command.Structure.EmbedHelper;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Hold data on a match played
 */
public class MatchStats {
    private final Date start, end;
    private final long duration, wobblies, timePlayed;
    private final RESULT result;
    private final Map map;
    private final Mode mode;
    private final String id, nemesis, mostKilled;
    private final Ratio killDeath, accuracy;
    private final Score score;
    private final int longestStreak, damageDealt, damageReceived, xp;
    private final double distanceTravelled, percentTimeMoving;
    private final MatchPlayer player;
    private final Loadout[] loadouts;
    private Team team1, team2;
    private byte[] loadoutImage;

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
        private final long duration, timePlayed;
        private final RESULT result;
        private final String id;
        private final MatchPlayer player;
        private final Map map;
        private final Mode mode;
        private String nemesis, mostKilled;
        private Ratio killDeath, accuracy;
        private Score score;
        private int longestStreak, damageDealt, damageReceived, xp;
        private long wobblies;
        private double distanceTravelled, percentTimeMoving;
        private Loadout[] loadouts;

        /**
         * Initialise the required values of the match
         *
         * @param id         Unique match id
         * @param map        Map the match was played on
         * @param mode       Match mode
         * @param start      Start data
         * @param end        End date
         * @param timePlayed Match time played (in ms)
         * @param result     Result of match
         * @param player     Player who match stats belong to
         */
        public MatchBuilder(String id, Map map, Mode mode, Date start, Date end, long timePlayed, RESULT result, MatchPlayer player) {
            this.id = id;
            this.map = map;
            this.mode = mode;
            this.start = start;
            this.end = end;
            this.duration = end.getTime() - start.getTime();
            this.result = result;
            this.player = player;
            this.timePlayed = timePlayed;
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
         * Set the loadouts used by the player during the match
         *
         * @param loadouts Array of player loadouts
         * @return Builder
         */
        public MatchBuilder setLoadouts(Loadout[] loadouts) {
            this.loadouts = loadouts;
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
         * Set the percentage of the match spent moving by the player
         *
         * @param percentTimeMoving Percentage of match spent moving
         * @return Builder
         */
        public MatchBuilder setPercentTimeMoving(double percentTimeMoving) {
            this.percentTimeMoving = percentTimeMoving;
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
        this.percentTimeMoving = builder.percentTimeMoving;
        this.distanceTravelled = builder.distanceTravelled;
        this.wobblies = builder.wobblies;
        this.player = builder.player;
        this.loadouts = builder.loadouts;
        this.timePlayed = builder.timePlayed;
    }

    /**
     * Get the array of loadouts the player used during the match
     *
     * @return Array of player loadouts
     */
    public Loadout[] getLoadouts() {
        return loadouts;
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
    public static String formatDistance(double distance, String unit) {
        DecimalFormat df = new DecimalFormat("#,### " + unit);
        df.setMaximumFractionDigits(2);
        return df.format(distance);
    }

    /**
     * Get the distance travelled (in metres) formatted as a String
     *
     * @return Metres travelled String
     */
    public String formatMetres() {
        return formatDistance(distanceTravelled, "metres");
    }

    /**
     * Get the distance travelled (in wobblies) formatted as a String
     *
     * @return Wobblies travelled String
     */
    public String formatWobblies() {
        return formatDistance(wobblies, "wobblies");
    }

    /**
     * Get the distance travelled (in metres)
     *
     * @return Metres travelled
     */
    public double getMetres() {
        return distanceTravelled;
    }

    /**
     * Get the distance travelled (in wobblies)
     *
     * @return Wobblies travelled
     */
    public long getWobblies() {
        return wobblies;
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
     * Check if there any player loadouts available
     *
     * @return Player loadouts available
     */
    public boolean hasLoadouts() {
        return loadouts.length > 0;
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
                "\n**Duration**: " + getMatchDurationString() +
                "\n\n**Mode**: " + mode.getName() +
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
     * @return Match duration String
     */
    public String getMatchDurationString() {
        return EmbedHelper.formatDuration(duration);
    }

    /**
     * Get the time played formatted to a String
     *
     * @return Time played String
     */
    public String getTimePlayedString() {
        return EmbedHelper.formatDuration(timePlayed);
    }

    /**
     * Get the match time played by the player (in ms)
     *
     * @return Match time played
     */
    public long getTimePlayed() {
        return timePlayed;
    }

    /**
     * Get the percentage of the match spent moving as a String e.g 32.50%
     *
     * @return Percent time moving String
     */
    public String getPercentTimeMovingString() {
        return new DecimalFormat("#.##'%'").format(percentTimeMoving);
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
     * @return Player completed match
     */
    public boolean playerCompleted(){
        return result!=RESULT.FORFEIT;
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

    /**
     * Set the image for the loadout
     *
     * @param loadoutImage Loadout image
     */
    public void setLoadoutImage(byte[] loadoutImage) {
        this.loadoutImage = loadoutImage;
    }

    /**
     * Get the image for the loadout
     *
     * @return Loadout image
     */
    public byte[] getLoadoutImage() {
        return loadoutImage;
    }
}