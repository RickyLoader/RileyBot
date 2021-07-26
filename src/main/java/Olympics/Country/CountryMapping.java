package Olympics.Country;

import Olympics.OlympicMapping;

import java.util.HashMap;

/**
 * Mapping Olympic countries
 */
public class CountryMapping extends OlympicMapping<String, Country> {

    /**
     * Create the country mapping
     *
     * @param mapping Map from country code -> country.
     */
    public CountryMapping(HashMap<String, Country> mapping) {
        super(mapping);
    }
}
