package COVID;

/**
 * COVID country
 */
public class Country {
    private final String name, iso, flagImageUrl;
    private final long population;

    /**
     * Create a country
     *
     * @param iso          ISO code - e.g "NZ"
     * @param name         Name e.g "New Zealand"
     * @param population   Country population
     * @param flagImageUrl URL to image of country flag
     */
    public Country(String iso, String name, long population, String flagImageUrl) {
        this.iso = iso;
        this.name = name;
        this.population = population;
        this.flagImageUrl = flagImageUrl;
    }

    /**
     * Get the country name
     *
     * @return Name - e.g "New Zealand"
     */
    public String getName() {
        return name;
    }

    /**
     * Get a URL to an image of the country's flag
     *
     * @return Country flag image URL
     */
    public String getFlagImageUrl() {
        return flagImageUrl;
    }

    /**
     * Get the total population of the country
     *
     * @return Country population
     */
    public long getPopulation() {
        return population;
    }

    /**
     * Get the ISO code for the country
     *
     * @return ISO - e.g "NZ"
     */
    public String getIso() {
        return iso;
    }
}
