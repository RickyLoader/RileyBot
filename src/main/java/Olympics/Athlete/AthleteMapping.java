package Olympics.Athlete;

import Olympics.Country.Country;
import Olympics.OlympicMapping;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.stream.Collectors;

/**
 * Mapping Olympic athletes
 */
public class AthleteMapping extends OlympicMapping<Long, Athlete> {

    /**
     * Create the athlete mapping
     *
     * @param mapping Map from athlete ID -> athlete.
     */
    public AthleteMapping(HashMap<Long, Athlete> mapping) {
        super(mapping);
    }

    /**
     * Get a list of athletes for the given country
     *
     * @param country Country to get athletes for
     * @return List of athletes for the given country
     */
    public ArrayList<Athlete> getByCountry(Country country) {
        return mapping
                .values()
                .stream()
                .filter(a -> a.getCountry() == country)
                .collect(Collectors.toCollection(ArrayList::new));
    }
}
