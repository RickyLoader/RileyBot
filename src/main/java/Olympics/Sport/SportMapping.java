package Olympics.Sport;

import Olympics.OlympicMapping;

import java.util.HashMap;

/**
 * Mapping Olympic sports
 */
public class SportMapping extends OlympicMapping<String, Sport> {

    /**
     * Create the sport mapping.
     *
     * @param mapping Map from sport code -> sport
     */
    public SportMapping(HashMap<String, Sport> mapping) {
        super(mapping);
    }
}
