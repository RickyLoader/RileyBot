package Weather;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

/**
 * Hold weather forecast summaries for each section of a day
 */
public class DayForecastBreakdown {
    private static final String
            CONDITION = "condition",
            NIGHT = "NIGHT",
            DAY = "DAY";
    private final DaySection morning, afternoon, evening, overnight;

    /**
     * Initialise the weather forecast for each section of a day.
     * Each section
     *
     * @param morning   Morning forecast summary
     * @param afternoon Afternoon forecast summary
     * @param evening   Evening forecast summary
     * @param overnight Overnight forecast summary
     */
    public DayForecastBreakdown(JSONObject morning, JSONObject afternoon, JSONObject evening, JSONObject overnight) {
        this.morning = new DaySection("Morning", morning.getString(CONDITION), DAY);
        this.afternoon = new DaySection("Afternoon", afternoon.getString(CONDITION), DAY);
        this.evening = new DaySection("Evening", evening.getString(CONDITION), NIGHT);
        this.overnight = new DaySection("Overnight", overnight.getString(CONDITION), NIGHT);
    }

    /**
     * Get the afternoon forecast data
     *
     * @return Afternoon forecast data
     */
    public DaySection getAfternoon() {
        return afternoon;
    }

    /**
     * Get the evening forecast data
     *
     * @return Evening forecast data
     */
    public DaySection getEvening() {
        return evening;
    }

    /**
     * Get the morning forecast data
     *
     * @return Morning forecast data
     */
    public DaySection getMorning() {
        return morning;
    }

    /**
     * Get the overnight forecast data
     *
     * @return overnight forecast data
     */
    public DaySection getOvernight() {
        return overnight;
    }


    /**
     * Hold the forecast information for a section of the day
     */
    public static class DaySection {
        private final String key, forecast, name;

        /**
         * Create a day section
         *
         * @param name     Section name - Morning, Evening..
         * @param forecast Forecast summary
         * @param iconType Type of icon
         */
        public DaySection(String name, String forecast, String iconType) {
            this.name = name;
            this.forecast = StringUtils.capitalize(forecast.replace("-", " "));
            this.key = iconType + "_" + forecast.replaceAll("-", "_").toUpperCase();
        }

        /**
         * Get the forecast summary for the day section
         *
         * @return Forecast summary
         */
        public String getForecast() {
            return forecast;
        }

        /**
         * Get the day section name
         *
         * @return Section name
         */
        public String getName() {
            return name;
        }

        /**
         * Get the icon key to resolve the appropriate icon to use for the forecast
         *
         * @return Icon key
         */
        public String getIconKey() {
            return key;
        }
    }
}