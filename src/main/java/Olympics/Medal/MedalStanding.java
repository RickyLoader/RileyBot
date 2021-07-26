package Olympics.Medal;

import Olympics.Country.Country;

/**
 * Country medal standing
 */
public class MedalStanding {
    private final int rank;
    private final Country country;
    private final MedalCount medalCount;

    /**
     * Create a medal standing for a country
     *
     * @param country    Country which medal standing represents
     * @param medalCount Count of each type of medal for the country
     * @param rank       Rank of medal standing amongst all countries
     */
    public MedalStanding(Country country, MedalCount medalCount, int rank) {
        this.country = country;
        this.medalCount = medalCount;
        this.rank = rank;
    }

    /**
     * Get the count of each type of medal for the country
     *
     * @return Medal count
     */
    public MedalCount getMedalCount() {
        return medalCount;
    }

    /**
     * Get the rank of the medal standing amongst all countries
     *
     * @return Medal standing rank
     */
    public int getRank() {
        return rank;
    }

    /**
     * Get the country that the medal standing represents
     *
     * @return Medal standing country
     */
    public Country getCountry() {
        return country;
    }
}
