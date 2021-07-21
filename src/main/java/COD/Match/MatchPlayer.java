package COD.Match;

import COD.API.CODStatsManager.PLATFORM;
import COD.Assets.Ratio;
import Command.Structure.EmbedHelper;

import java.text.DecimalFormat;

/**
 * Hold information on a player in a COD match
 */
public class MatchPlayer {
    private final String name, uno, nemesis, mostKilled, team;
    private final PLATFORM platform;
    private final long wobblies, timePlayed;
    private final Ratio killDeath, accuracy;
    private final int longestStreak, damageDealt, damageReceived, xp;
    private final double distanceTravelled, percentTimeMoving;
    private final Loadout[] loadouts;
    private byte[] loadoutImage;

    /**
     * Create the match player from the given builder
     *
     * @param builder Builder to use values from
     */
    private MatchPlayer(MatchPlayerBuilder builder) {
        this.name = builder.name;
        this.platform = builder.platform;
        this.uno = builder.uno;
        this.loadouts = builder.loadouts;
        this.nemesis = builder.nemesis;
        this.mostKilled = builder.mostKilled;
        this.wobblies = builder.wobblies;
        this.timePlayed = builder.timePlayed;
        this.killDeath = builder.killDeath;
        this.accuracy = builder.accuracy;
        this.longestStreak = builder.longestStreak;
        this.damageDealt = builder.damageDealt;
        this.damageReceived = builder.damageReceived;
        this.xp = builder.xp;
        this.distanceTravelled = builder.distanceTravelled;
        this.percentTimeMoving = builder.percentTimeMoving;
        this.team = builder.team;
    }

    public static class MatchPlayerBuilder {
        private final String name;
        private String team, uno;
        private final PLATFORM platform;
        private String nemesis, mostKilled;
        private long wobblies, timePlayed;
        private Ratio killDeath, accuracy;
        private int longestStreak, damageDealt, damageReceived, xp;
        private double distanceTravelled, percentTimeMoving;
        private Loadout[] loadouts;

        /**
         * Initialise the required values for a match player
         *
         * @param name     Player name
         * @param platform Player platform
         */
        public MatchPlayerBuilder(String name, PLATFORM platform) {
            this.name = name;
            this.platform = platform;
        }

        /**
         * Set the uno identifier of the player
         *
         * @param uno Player uno identifier
         * @return Builder
         */
        public MatchPlayerBuilder setUno(String uno) {
            this.uno = uno;
            return this;
        }

        /**
         * Set the player team
         *
         * @param team Player team
         * @return Builder
         */
        public MatchPlayerBuilder setTeam(String team) {
            this.team = team;
            return this;
        }

        /**
         * Set the kill/death ratio of player during the match
         *
         * @param kd Kill/death ratio
         * @return Builder
         */
        public MatchPlayerBuilder setKD(Ratio kd) {
            this.killDeath = kd;
            return this;
        }

        /**
         * Set the accuracy of the player during the match
         *
         * @param accuracy Accuracy
         * @return Builder
         */
        public MatchPlayerBuilder setAccuracy(Ratio accuracy) {
            this.accuracy = accuracy;
            return this;
        }

        /**
         * Set the longest streak of the player during the match
         *
         * @param longestStreak Longest streak
         * @return Builder
         */
        public MatchPlayerBuilder setLongestStreak(int longestStreak) {
            this.longestStreak = longestStreak;
            return this;
        }

        /**
         * Set the total damage dealt by the player during the match
         *
         * @param damageDealt Total damage dealt
         * @return Builder
         */
        public MatchPlayerBuilder setDamageDealt(int damageDealt) {
            this.damageDealt = damageDealt;
            return this;
        }

        /**
         * Set the total received by the player during the match
         *
         * @param damageReceived Total damage received
         * @return Builder
         */
        public MatchPlayerBuilder setDamageReceived(int damageReceived) {
            this.damageReceived = damageReceived;
            return this;
        }

        /**
         * Set the distance travelled by the player during the match (in inches)
         *
         * @param distanceTravelledInches Distance travelled in inches
         * @return Builder
         */
        public MatchPlayerBuilder setDistanceTravelled(int distanceTravelledInches) {
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
        public MatchPlayerBuilder setPercentTimeMoving(double percentTimeMoving) {
            this.percentTimeMoving = percentTimeMoving;
            return this;
        }

        /**
         * Set the XP gained by the player from the match
         *
         * @param xp XP gained
         * @return Builder
         */
        public MatchPlayerBuilder setXP(int xp) {
            this.xp = xp;
            return this;
        }

        /**
         * Set the player's match nemesis (most killed by)
         *
         * @param nemesis Name of nemesis
         * @return Builder
         */
        public MatchPlayerBuilder setNemesis(String nemesis) {
            this.nemesis = nemesis;
            return this;
        }

        /**
         * Set the player's most killed player during the match
         *
         * @param mostKilled Name of most killed player
         * @return Builder
         */
        public MatchPlayerBuilder setMostKilled(String mostKilled) {
            this.mostKilled = mostKilled;
            return this;
        }

        /**
         * Set the player's match loadouts
         *
         * @param loadouts Match loadouts
         * @return Builder
         */
        public MatchPlayerBuilder setLoadouts(Loadout[] loadouts) {
            this.loadouts = loadouts;
            return this;
        }

        /**
         * Set the match time played (in ms)
         *
         * @param timePlayed Time played (in ms)
         * @return Builder
         */
        public MatchPlayerBuilder setTimePlayed(long timePlayed) {
            this.timePlayed = timePlayed;
            return this;
        }

        /**
         * Build the match player
         *
         * @return Match player
         */
        public MatchPlayer build() {
            if(loadouts == null) {
                loadouts = new Loadout[0];
            }
            return new MatchPlayer(this);
        }
    }

    /**
     * Get the player's loadouts during the match
     *
     * @return Player's match loadouts
     */
    public Loadout[] getLoadouts() {
        return loadouts;
    }

    /**
     * Get the player team name
     *
     * @return Player team name
     */
    public String getTeam() {
        return team;
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
     * Check if the player has any loadout data
     *
     * @return Player loadouts available
     */
    public boolean hasLoadouts() {
        return loadouts.length > 0;
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

    /**
     * Get the player's name
     *
     * @return Player name
     */
    public String getName() {
        return name;
    }

    /**
     * Get the uno identifier of the player
     *
     * @return Uno identifier
     */
    public String getUno() {
        return uno;
    }

    /**
     * Get the player's platform
     *
     * @return Player platform
     */
    public PLATFORM getPlatform() {
        return platform;
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

}
