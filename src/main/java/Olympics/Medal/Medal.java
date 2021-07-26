package Olympics.Medal;

import Olympics.Sport.Event;
import Olympics.Sport.Sport;

/**
 * Olympic medal
 */
public class Medal {
    private final Sport sport;
    private final Event event;
    private final TYPE type;

    public enum TYPE {
        BRONZE,
        SILVER,
        GOLD
    }

    /**
     * Create an Olympic medal
     *
     * @param sport Sport where medal was awarded
     * @param event Event within sport where medal was awarded
     * @param type  Type of medal - BRONZE etc
     */
    public Medal(Sport sport, Event event, TYPE type) {
        this.sport = sport;
        this.event = event;
        this.type = type;
    }

    /**
     * Get the type of medal
     *
     * @return Medal type - e.g BRONZE etc
     */
    public TYPE getType() {
        return type;
    }

    /**
     * Get the event within the sport in which medal was awarded
     *
     * @return Event
     */
    public Event getEvent() {
        return event;
    }

    /**
     * Get the sport in which the medal was awarded
     *
     * @return Sport
     */
    public Sport getSport() {
        return sport;
    }
}
