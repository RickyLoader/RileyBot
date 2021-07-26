package Olympics.Country;

import Olympics.OlympicData;

/**
 * Olympic competing country details
 */
public class Country extends OlympicData {
    private final String flagImageUrl;
    private CountryBio bio;

    /**
     * Create an Olympic competing country.
     *
     * @param code         Country ISO code - e.g "NZL"
     * @param name         Country name - e.g "New Zealand"
     * @param flagImageUrl URL to an image of the country's flag
     */
    public Country(String code, String name, String flagImageUrl) {
        super(code, name);
        this.flagImageUrl = flagImageUrl;
    }

    /**
     * Get the URL to an image of the country's flag
     *
     * @return URL to country flag image
     */
    public String getFlagImageUrl() {
        return flagImageUrl;
    }

    /**
     * Get the country bio.
     * This contains their medal standing, flag bearers, etc
     *
     * @return Country bio
     */
    public CountryBio getBio() {
        return bio;
    }

    /**
     * Set the country bio
     *
     * @param bio Country bio
     */
    public void setBio(CountryBio bio) {
        this.bio = bio;
    }

    /**
     * Check if the country has a bio
     *
     * @return Country has a bio
     */
    public boolean hasBio() {
        return bio != null;
    }
}
