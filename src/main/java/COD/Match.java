package COD;

import Command.Structure.EmbedHelper;
import Command.Structure.EmoteHelper;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import static COD.MWPlayer.*;

/**
 * Hold data on a match played
 */
public class Match {
    private final Date start, end;
    private final long duration;
    private final RESULT result;
    private final String winEmote, lossEmote, drawEmote, mode, id, nemesis, mostKilled;
    private final Map map;
    private final Ratio killDeath, accuracy;
    private final Score score;
    private final int longestStreak, damageDealt, damageReceived, xp, distanceTravelled;

    public enum RESULT {
        WIN,
        LOSS,
        DRAW,
        FORFEIT
    }

    /**
     * Create a match
     *
     * @param id                Match id
     * @param start             Date of match start
     * @param end               Date of match end
     * @param result            Match result
     * @param map               Map the match was played on
     * @param mode              Name of mode
     * @param killDeath         Kill/Death ratio
     * @param accuracy          Shots hit/Shots fired ratio
     * @param score             Match score
     * @param nemesis           Most killed by player
     * @param mostKilled        Most killed player
     * @param longestStreak     Longest killstreak
     * @param damageDealt       Total damage dealt by the player
     * @param damageReceived    Total damage received by the player
     * @param xp                Total match xp
     * @param distanceTravelled Distance travelled
     * @param helper            Emote Helper
     */
    public Match(String id, Date start, Date end, RESULT result, Map map, String mode, Ratio killDeath, Ratio accuracy, Score score, String nemesis, String mostKilled, int longestStreak, int damageDealt, int damageReceived, int xp, int distanceTravelled, EmoteHelper helper) {
        this.id = id;
        this.start = start;
        this.end = end;
        this.duration = end.getTime() - start.getTime();
        this.result = result;
        this.winEmote = EmoteHelper.formatEmote(helper.getComplete());
        this.lossEmote = EmoteHelper.formatEmote(helper.getFail());
        this.drawEmote = EmoteHelper.formatEmote(helper.getDraw());
        this.map = map;
        this.mode = mode;
        this.killDeath = killDeath;
        this.accuracy = accuracy;
        this.score = score;
        this.mostKilled = mostKilled;
        this.nemesis = nemesis;
        this.longestStreak = longestStreak;
        this.damageDealt = damageDealt;
        this.damageReceived = damageReceived;
        this.xp = xp;
        this.distanceTravelled = distanceTravelled;
    }

    /**
     * Get the distance travelled TODO WHAT IS THE UNIT OF MEASUREMENT
     *
     * @return Distance travelled
     */
    public String getDistanceTravelled() {
        return new DecimalFormat("#,### wobblies").format(distanceTravelled);
    }

    /**
     * Get the total match experience
     *
     * @return Match XP
     */
    public int getExperience() {
        return xp;
    }

    /**
     * Get the total damage dealt during the match by the player
     *
     * @return Damage dealt
     */
    public int getDamageDealt() {
        return damageDealt;
    }

    /**
     * Get the total damage the player received during the match
     *
     * @return Damage received
     */
    public int getDamageReceived() {
        return damageReceived;
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
        if(accuracy == null) {
            return "-";
        }
        return accuracy.getRatioPercentage();
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
        return EmbedHelper.formatTime(duration);
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
     * Get the result formatted for use in a message embed with an emote and score
     *
     * @return Formatted result
     */
    public String getFormattedResult() {
        return result.toString() + " " + getResultEmote() + "\n(" + score.getScore() + ")";
    }

    /**
     * Get the embed formatted emote associated with the result
     *
     * @return Result emote
     */
    public String getResultEmote() {
        switch(result) {
            case WIN:
                return winEmote;
            case LOSS:
                return lossEmote;
            default:
                return drawEmote;
        }
    }
}