package Olympics.Sport;

import Olympics.OlympicData;

import java.util.HashMap;

/**
 * Olympic sport details
 */
public class Sport extends OlympicData {
    private final String iconImageUrl;
    private final HashMap<String, Event> events;

    /**
     * Create an Olympic sport
     *
     * @param code         Sport code - e.g "CTR" (always 3 characters)
     * @param name         Sport name - e.g "Cycling Track"
     * @param iconImageUrl URL to the Olympic icon for the sport
     */
    public Sport(String code, String name, String iconImageUrl) {
        super(code, name);
        this.iconImageUrl = iconImageUrl;
        this.events = new HashMap<>();
    }

    /**
     * Get an event within the sport by its code
     *
     * @param eventCode Event code
     * @return Event from code
     */
    public Event getEventByCode(String eventCode) {
        return events.get(eventCode);
    }

    /**
     * Add the given event to the events for the sport
     *
     * @param event Event to add
     */
    public void addEvent(Event event) {
        this.events.put(event.getCode(), event);
    }

    /**
     * Get the URL to the Olympic icon for the sport
     *
     * @return URL to Olympic icon
     */
    public String getIconImageUrl() {
        return iconImageUrl;
    }
}
