package Olympics.Athlete;

import Olympics.Country.Country;
import org.jetbrains.annotations.Nullable;

/**
 * Hold a country and name of a place within the country
 */
public class ResidenceInfo {
    private final Country country;
    private final String location;

    /**
     * Create residence info. This is a country paired with the name of a place within the country.
     *
     * @param country  Country - e.g Somalia
     * @param location Name of a location within the country - e.g "Mogadishu"
     */
    public ResidenceInfo(Country country, @Nullable String location) {
        this.country = country;
        this.location = location;
    }

    /**
     * Get the country
     *
     * @return Country
     */
    public Country getCountry() {
        return country;
    }

    /**
     * Check if the location within the country is available
     *
     * @return Location is available
     */
    public boolean hasLocation() {
        return location != null;
    }

    /**
     * Get the name of a location within the country
     *
     * @return Name of location within country - e.g "Mogadishu"
     */
    public String getLocation() {
        return location;
    }
}
