package COVID;

/**
 * COVID-19 stats for a country
 */
public class CovidStats {
    private final Country country;
    private final VirusStats virusStats;

    /**
     * Create COVID-19 stats for a country
     *
     * @param country      Country which stats pertain to
     * @param virusStats   Stats related to the virus - deaths, cases, etc
     */
    public CovidStats(Country country, VirusStats virusStats) {
        this.country = country;
        this.virusStats = virusStats;
    }

    /**
     * Get stats related to the virus - deaths, cases etc
     *
     * @return Virus stats
     */
    public VirusStats getVirusStats() {
        return virusStats;
    }

    /**
     * Get the country that the stats pertain to
     *
     * @return Country
     */
    public Country getCountry() {
        return country;
    }
}
