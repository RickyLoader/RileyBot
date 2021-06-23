package Weather;

import org.jetbrains.annotations.Nullable;

/**
 * Weather extreme
 */
public class WeatherExtreme {
    private final String title, icon, value, locationName;

    /**
     * Create a weather extreme
     *
     * @param title        Extreme title - e.g "Wettest"
     * @param icon         Emote mention String for icon
     * @param value        Optional Value e.g "0.6mm" (as in rainfall) for "Wettest"
     * @param locationName Name of location where the extreme has occurred e.g "Dunedin"
     */
    public WeatherExtreme(String title, String icon, @Nullable String value, String locationName) {
        this.title = title;
        this.icon = icon;
        this.value = value;
        this.locationName = locationName;
    }

    /**
     * Get the extreme title - e.g "Wettest"
     *
     * @return Extreme title
     */
    public String getTitle() {
        return title;
    }

    /**
     * Get the emote mention String of the icon
     *
     * @return Icon
     */
    public String getIcon() {
        return icon;
    }

    /**
     * Get the name of the location where the extreme has occurred
     *
     * @return Location name
     */
    public String getLocationName() {
        return locationName;
    }

    /**
     * Get the value associated with the extreme title.
     * E.g "0.6mm" (as in rainfall) for "Wettest"
     *
     * @return Value
     */
    @Nullable
    public String getValue() {
        return value;
    }

    /**
     * Check if the extreme has an associated value.
     * Not all do
     *
     * @return Extreme has value
     */
    public boolean hasValue() {
        return value != null;
    }
}
