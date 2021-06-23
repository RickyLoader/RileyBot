package Weather;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Hold daily weather forecast data - projected min/max temp etc
 */
public class Forecast {
    private final String statement, image;
    private final double max, min;
    private final DayForecastBreakdown dayForecastBreakdown;
    private final LocalObservations localObservations;
    private final Date date, issued;

    /**
     * Create a forecast
     *
     * @param date                 Date of forecast
     * @param dateIssued           Date that forecast data was issued by MetService
     * @param statement            Statement about the forecast - e.g "Fine. Northerly breezes."
     * @param max                  Maximum projected temperature
     * @param min                  Minimum projected temperature
     * @param dayForecastBreakdown Data for each section of the day
     * @param localObservations    Local observation data for date (only if current date)
     * @param image                Rain radar
     */
    public Forecast(Date date, Date dateIssued, String statement, double max, double min, @Nullable DayForecastBreakdown dayForecastBreakdown, @Nullable LocalObservations localObservations, @Nullable String image) {
        this.date = date;
        this.issued = dateIssued;
        this.statement = statement.length() > 100 ? (statement.substring(0, 100) + "...") : statement;
        this.max = max;
        this.min = min;
        this.dayForecastBreakdown = dayForecastBreakdown;
        this.localObservations = localObservations;
        this.image = "https://www.metservice.com/" + image;
    }

    /**
     * Get the rain radar image
     *
     * @return Rain radar image
     */
    public String getImage() {
        return image;
    }

    /**
     * Forecast has local observation data
     *
     * @return Observation data exists
     */
    public boolean hasLocalObservations() {
        return localObservations != null;
    }

    /**
     * Forecast has image
     *
     * @return Image exists
     */
    public boolean hasImage() {
        return image != null;
    }

    /**
     * Get the statement about the forecast - e.g "Fine. Northerly breezes."
     *
     * @return Forecast statement
     */
    public String getStatement() {
        return statement;
    }

    /**
     * Get the day and date of forecast
     *
     * @return Day and date
     */
    public String formatDate() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        String suffix = getDaySuffix(calendar.get(Calendar.DAY_OF_MONTH));
        String[] dateString = new SimpleDateFormat("EEEE d MMMM").format(date).split(" ");
        dateString[1] = dateString[1] + suffix;
        return StringUtils.join(dateString, " ");
    }

    /**
     * Get the correct suffix for the day of month 1 -> 1st
     *
     * @return Day suffix
     */
    private String getDaySuffix(int day) {
        switch(day) {
            case 1:
            case 21:
            case 31:
                return "st";
            case 2:
            case 22:
                return "nd";
            case 3:
            case 23:
                return "rd";
            default:
                return "th";
        }
    }

    /**
     * Get date that data was issued by MetService
     *
     * @return Issue date of data
     */
    public Date getDateIssued() {
        return issued;
    }

    /**
     * Get day part data
     *
     * @return Day part data
     */
    public DayForecastBreakdown getDayData() {
        return dayForecastBreakdown;
    }

    /**
     * Get the maximum temperature
     *
     * @return Maximum temperature
     */
    public double getMaximumTemperature() {
        return max;
    }

    /**
     * Get the minimum temperature
     *
     * @return Minimum temperature
     */
    public double getMinimumTemperature() {
        return min;
    }

    /**
     * Return presence of daily breakdown data
     *
     * @return Day data exists
     */
    public boolean hasDayData() {
        return dayForecastBreakdown != null;
    }

    /**
     * Format a temperature to include the celsius symbol
     *
     * @return Formatted temperature
     */
    public static String formatTemperature(double temp) {
        return temp + " °C";
    }

    /**
     * Get local forecast observation data
     *
     * @return Local observation data
     */
    public LocalObservations getLocalObservations() {
        return localObservations;
    }
}