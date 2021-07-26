package Olympics.Sport;

import Olympics.OlympicData;

/**
 * Sporting event
 */
public class Event extends OlympicData {

    /**
     * Create an Olympic sporting event
     *
     * @param code Event code - e.g "ATHM4X400M------------------------" TODO strip the 50000 dashes they put on
     * @param name Event name - e.g "Men's 4 x 400m Relay"
     */
    public Event(String code, String name) {
        super(code, name);
    }
}
