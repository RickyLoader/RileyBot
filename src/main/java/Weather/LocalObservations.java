package Weather;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

/**
 * Local observations are the observations of various weather events for a forecast, e.g wind speed, rainfall etc
 */
public class LocalObservations {
    private final String clothing, windDirection, windClothing, windStrength, pressureTrend;
    private final Integer humidity, windSpeed, gustSpeed, pressure;
    private final Double rainfall;
    private final double temperature, feelsLike;

    /**
     * Create a Local observation
     */
    private LocalObservations(LocalObservationsBuilder builder) {
        this.clothing = builder.clothing;
        this.windClothing = builder.windClothing;
        this.humidity = builder.humidity;
        this.temperature = builder.temperature;
        this.feelsLike = builder.feelsLike;
        this.rainfall = builder.rainfall;
        this.windDirection = builder.windDirection;
        this.windSpeed = builder.windSpeed;
        this.gustSpeed = builder.gustSpeed;
        this.windStrength = builder.windStrength;
        this.pressure = builder.pressure;
        this.pressureTrend = builder.pressureTrend;
    }

    public static class LocalObservationsBuilder {
        private final double temperature, feelsLike;
        private String clothing, windDirection, windClothing, windStrength, pressureTrend;
        private Integer humidity, windSpeed, gustSpeed, pressure;
        private Double rainfall;

        /**
         * Initialise the builder with the required temperature values
         *
         * @param temperature Current temperature (in celsius)
         * @param feelsLike   Temperature feels like (in celsius)
         */
        public LocalObservationsBuilder(double temperature, double feelsLike) {
            this.temperature = temperature;
            this.feelsLike = feelsLike;
        }

        /**
         * Create the local observations from the builder values
         *
         * @return Local observations from builder values
         */
        public LocalObservations build() {
            return new LocalObservations(this);
        }

        /**
         * Set the rainfall observations
         *
         * @param humidity Humidity percentage
         * @param rainFall Rainfall (mm)
         * @return Builder
         */
        public LocalObservationsBuilder setRainfall(@Nullable Double rainFall, @Nullable Integer humidity) {
            this.humidity = humidity;
            this.rainfall = rainFall;
            return this;
        }

        /**
         * Set the pressure observations.
         *
         * @param pressure      Pressure
         * @param pressureTrend Trend of pressure - "rising"
         * @return Builder
         */
        public LocalObservationsBuilder setPressure(@Nullable Integer pressure, String pressureTrend) {
            this.pressure = pressure;
            this.pressureTrend = StringUtils.capitalize(pressureTrend);
            return this;
        }

        /**
         * Set the clothing observations
         *
         * @param clothing     Number of clothing layers - e.g "2 Layers"
         * @param windClothing Number of windproof clothing layers - e.g "2 Windproof"
         * @return Builder
         */
        public LocalObservationsBuilder setClothing(String clothing, String windClothing) {
            this.clothing = clothing;
            this.windClothing = windClothing;
            return this;
        }


        /**
         * Set the wind observations.
         *
         * @param windSpeed Wind speed (km/h)
         * @param gustSpeed Max gust speed (km/h)
         * @param direction Compass direction of wind e.g "NE"
         * @param strength  Description of wind strength - "Light Winds"
         * @return Builder
         */
        public LocalObservationsBuilder setWind(@Nullable Integer windSpeed, @Nullable Integer gustSpeed, @Nullable String direction, String strength) {
            this.windSpeed = windSpeed;
            this.gustSpeed = gustSpeed;
            this.windDirection = direction;
            this.windStrength = strength;
            return this;
        }
    }

    /**
     * Get the wind observations as a String
     *
     * @return Wind observations String
     */
    public String getFormattedWindDetails() {
        StringBuilder builder = new StringBuilder();
        if(hasWind()) {
            builder.append(formatSpeed(windSpeed));
        }
        if(windDirection != null) {
            builder.append(" ").append(windDirection);
        }
        if(builder.length() > 0) {
            builder.append("\n");
        }
        return builder.append(windStrength).toString();
    }

    /**
     * Format the maximum wind gust speed
     *
     * @return Gust information String
     */
    public String getFormattedGustSpeed() {
        return formatSpeed(gustSpeed);
    }

    /**
     * Format the given speed to a String containing km/h
     *
     * @param speed Speed to format
     * @return Speed km/h
     */
    private String formatSpeed(int speed) {
        return speed + " km/h";
    }

    /**
     * Format the pressure and pressure trend
     *
     * @return Pressure information String
     */
    public String getFormattedPressureDetails() {
        return pressure + " hPa\n" + pressureTrend;
    }

    /**
     * Get the rainfall as a String with the mm quantifier - e.g 5 mm
     *
     * @return Rainfall
     */
    public String getFormattedRainfall() {
        return rainfall + "mm";
    }

    /**
     * Get the current temperature feels like value
     *
     * @return Temperature feels like
     */
    public double getFeelsLike() {
        return feelsLike;
    }

    /**
     * Return presence of wind data
     *
     * @return Wind data exists
     */
    public boolean hasWind() {
        return windSpeed != null && windSpeed > 0;
    }

    /**
     * Get the current humidity percentage
     *
     * @return Humidity
     */
    public String getFormattedHumidity() {
        return humidity + "%";
    }

    /**
     * Get the current temperature
     *
     * @return Current temperature
     */
    public double getTemperature() {
        return temperature;
    }

    /**
     * Format clothing to a String
     *
     * @return Formatted clothing String
     */
    public String getFormattedClothing() {
        return clothing + " Layers\n" + windClothing + " Windproof";
    }

    /**
     * Return presence of clothing data
     *
     * @return Clothing data exists
     */
    public boolean hasClothing() {
        return clothing != null;
    }

    /**
     * Return presence of pressure data
     *
     * @return Pressure data exists
     */
    public boolean hasPressure() {
        return pressureTrend != null && pressure != null && pressure > 0;
    }

    /**
     * Return presence of maximum wind gust speed
     *
     * @return Gust speed exists
     */
    public boolean hasGustSpeed() {
        return gustSpeed != null && gustSpeed > 0;
    }

    /**
     * Return presence of rainfall data
     *
     * @return Rainfall data exists
     */
    public boolean hasRainfall() {
        return rainfall != null && rainfall > 0;
    }

    /**
     * Return presence of humidity data
     *
     * @return Humidity data exists
     */
    public boolean hasHumidity() {
        return humidity != null && humidity > 0;
    }
}
