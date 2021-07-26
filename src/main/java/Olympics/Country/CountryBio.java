package Olympics.Country;

import Olympics.Athlete.Athlete;
import Olympics.Medal.MedalStanding;
import org.jetbrains.annotations.Nullable;

/**
 * Olympic country bio - medal standing, flag bearers, etc
 */
public class CountryBio {
    private final NationalAnthem nationalAnthem;
    private final Athlete[] flagBearers;
    private final int committeeFoundedYear, firstGamesYear, joinedIOCYear, totalGames;
    private final String committeeName;
    private MedalStanding medalStanding;

    /**
     * Create a country bio
     *
     * @param nationalAnthem       National anthem of the country
     * @param medalStanding        Medal standing of the country
     * @param flagBearers          Athletes who carried the flag
     * @param committeeName        Name of the country's Olympic committee
     * @param committeeFoundedYear Year in which the Olympic committee was formed
     * @param firstGamesYear       Year in which the country first participated in the Olympic games
     * @param joinedIOCYear        Year in which the country joined the International Olympic Committee
     * @param totalGames           Total number of Olympic games the country has participated in
     */
    public CountryBio(NationalAnthem nationalAnthem, @Nullable MedalStanding medalStanding, Athlete[] flagBearers, String committeeName, int committeeFoundedYear, int firstGamesYear, int joinedIOCYear, int totalGames) {
        this.nationalAnthem = nationalAnthem;
        this.medalStanding = medalStanding;
        this.flagBearers = flagBearers;
        this.committeeName = committeeName;
        this.committeeFoundedYear = committeeFoundedYear;
        this.firstGamesYear = firstGamesYear;
        this.joinedIOCYear = joinedIOCYear;
        this.totalGames = totalGames;
    }

    /**
     * Get the year in which the country formed their Olympic committee
     *
     * @return Committee founded year
     */
    public int getCommitteeFoundedYear() {
        return committeeFoundedYear;
    }

    /**
     * Get the name of the country's Olympic committee
     *
     * @return Country Olympic committee name
     */
    public String getCommitteeName() {
        return committeeName;
    }

    /**
     * Get the total number of Olympic games that the country has participated in.
     *
     * @return Total Olympic games
     */
    public int getTotalGames() {
        return totalGames;
    }

    /**
     * Get the year in which the country joined the International Olympic Committee
     *
     * @return Joined IOC year
     */
    public int getJoinedIOCYear() {
        return joinedIOCYear;
    }

    /**
     * Get the year in which the country first participated in the Olympic games.
     *
     * @return First Olympic games year
     */
    public int getFirstGamesYear() {
        return firstGamesYear;
    }

    /**
     * Get an array of flag bearers for the country.
     * These are the athletes which carried the flag.
     *
     * @return Flag bearers
     */
    public Athlete[] getFlagBearers() {
        return flagBearers;
    }

    /**
     * Get the national anthem of the country
     *
     * @return National anthem
     */
    public NationalAnthem getNationalAnthem() {
        return nationalAnthem;
    }

    /**
     * Get the medal standing of the country.
     * This is the total number of each type of medal and rank info.
     *
     * @return Medal standing
     */
    public MedalStanding getMedalStanding() {
        return medalStanding;
    }

    /**
     * Set the medal standing of the country
     *
     * @param medalStanding Medal standing
     */
    public void setMedalStanding(MedalStanding medalStanding) {
        this.medalStanding = medalStanding;
    }

    /**
     * Check if the country has a medal standing.
     * This is false if the country hasn't been awarded any medals.
     *
     * @return Country has a medal standing
     */
    public boolean hasMedalStanding() {
        return medalStanding != null;
    }

    /**
     * Check if the country has any flag bearers
     *
     * @return Country has flag bearers
     */
    public boolean hasFlagBearers() {
        return flagBearers.length != 0;
    }
}
