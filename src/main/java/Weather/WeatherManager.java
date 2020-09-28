package Weather;

import Command.Structure.EmbedHelper;
import Command.Structure.EmoteHelper;
import Network.NetworkRequest;
import Network.Secret;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.MessageEmbed.Field;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class WeatherManager {
    private final HashMap<String, String> iconTypes = new HashMap<>();
    private final String maxIcon, minIcon, clothing, humidity, rain, wind, pressure, missing;

    /**
     * Create the weather manager
     *
     * @param emoteHelper Emote helper
     */
    public WeatherManager(EmoteHelper emoteHelper) {
        this.maxIcon = EmoteHelper.formatEmote(emoteHelper.getMaxTemp());
        this.minIcon = EmoteHelper.formatEmote(emoteHelper.getMinTemp());
        this.rain = EmoteHelper.formatEmote(emoteHelper.getRain());
        this.humidity = EmoteHelper.formatEmote(emoteHelper.getHumidity());
        this.wind = EmoteHelper.formatEmote(emoteHelper.getWind());
        this.clothing = EmoteHelper.formatEmote(emoteHelper.getClothing());
        this.missing = EmoteHelper.formatEmote(emoteHelper.getFail());
        this.pressure = EmoteHelper.formatEmote(emoteHelper.getPressure());

        iconTypes.put("DAY_FINE", EmoteHelper.formatEmote(emoteHelper.getDayFine()));
        iconTypes.put("DAY_FEW_SHOWERS", EmoteHelper.formatEmote(emoteHelper.getDayFewShowers()));
        iconTypes.put("DAY_PARTLY_CLOUDY", EmoteHelper.formatEmote(emoteHelper.getDayPartlyCloudy()));
        iconTypes.put("DAY_RAIN", EmoteHelper.formatEmote(emoteHelper.getDayRain()));

        iconTypes.put("NIGHT_PARTLY_CLOUDY", EmoteHelper.formatEmote(emoteHelper.getNightPartlyCloudy()));
        iconTypes.put("NIGHT_FINE", EmoteHelper.formatEmote(emoteHelper.getNightFine()));
        iconTypes.put("NIGHT_FEW_SHOWERS", EmoteHelper.formatEmote(emoteHelper.getNightFewShowers()));
        iconTypes.put("NIGHT_DRIZZLE", EmoteHelper.formatEmote(emoteHelper.getNightDrizzle()));

        String showers = EmoteHelper.formatEmote(emoteHelper.getShowers());
        String cloudy = EmoteHelper.formatEmote(emoteHelper.getCloudy());
        String windy = EmoteHelper.formatEmote(emoteHelper.getWindy());
        String windRain = EmoteHelper.formatEmote(emoteHelper.getWindRain());
        String snow = EmoteHelper.formatEmote(emoteHelper.getSnow());
        iconTypes.put("DAY_SHOWERS", showers);
        iconTypes.put("NIGHT_SHOWERS", showers);
        iconTypes.put("DAY_CLOUDY", cloudy);
        iconTypes.put("NIGHT_CLOUDY", cloudy);
        iconTypes.put("NIGHT_WINDY", windy);
        iconTypes.put("DAY_WINDY", windy);
        iconTypes.put("DAY_WIND_RAIN", windRain);
        iconTypes.put("NIGHT_WIND_RAIN", windRain);
        iconTypes.put("DAY_SNOW", snow);
        iconTypes.put("NIGHT_SNOW", snow);
    }

    /**
     * Get the weather forecast for a NZ location
     *
     * @param location Location to get weather for
     * @param tomorrow Tomorrow's forecast
     * @param help     Help message
     * @return Weather forecast message embed
     */
    public MessageEmbed getForecast(String location, boolean tomorrow, String help) {
        EmbedBuilder builder = getEmbedBuilder(help);
        MessageEmbed failEmbed = createFailEmbed(builder, location);
        SearchResult correctedLocation = discernBestLocation(location);

        if(correctedLocation == null) {
            return failEmbed;
        }

        Forecast forecast = getForecastData(correctedLocation, tomorrow);

        if(forecast == null) {
            return failEmbed;
        }

        builder.setColor(EmbedHelper.getBlue());
        builder.setTitle(
                correctedLocation.getLocation()
                        + " Weather Forecast | "
                        + (tomorrow ? "Tomorrow" : "Today")
        );
        builder.setDescription(
                EmbedHelper.embedURL("Forecast", correctedLocation.getWebURL())
                        + " for "
                        + forecast.formatDate()
                        + "\n\n"
                        + forecast.getForecast()
                        + "."
        );
        double maxTemp = forecast.getMax();
        double minTemp = forecast.getMin();
        builder.addField("Max " + maxIcon, Forecast.formatTemperature(maxTemp), true);
        builder.addField("Min " + minIcon, Forecast.formatTemperature(minTemp), true);
        if(forecast.hasLocalObs()) {
            LocalObs localObs = forecast.getLocalObs();
            if(localObs.hasTemp()) {
                double currentTemp = localObs.getTemp();
                builder.addField(
                        "Now" + ((Math.abs(currentTemp - maxTemp) < Math.abs(currentTemp - minTemp)) ? maxIcon : minIcon) + " Feel",
                        Forecast.formatTemperature(currentTemp) + " | " + Forecast.formatTemperature(localObs.getFeelsLike()),
                        true
                );
            }
            else {
                builder.addBlankField(true);
            }

            if(localObs.hasClothing()) {
                builder.addField("Clothing " + clothing, localObs.formatClothing(), true);
            }
            if(localObs.hasWind()) {
                builder.addField("Wind " + wind, localObs.formatWindDetails(), true);
            }
            if(localObs.hasRainfall()) {
                builder.addField("Rainfall " + rain, localObs.formatRainfall(), true);
            }
            if(localObs.hasHumidity()) {
                builder.addField("Humidity " + humidity, localObs.formatHumidity(), true);
            }
            if(localObs.hasPressure()) {
                builder.addField("Pressure " + pressure, localObs.formatPressureDetails(), true);
            }
        }

        int padding = 3 - (builder.getFields().size() % 3);
        if(padding < 3) {
            for(int i = 0; i < padding; i++) {
                builder.addBlankField(true);
            }
        }

        if(forecast.hasDayData()) {
            DayData dayData = forecast.getDayData();
            builder.addField(buildForecastField(dayData.getMorning()));
            builder.addField(buildForecastField(dayData.getAfternoon()));
            builder.addField(buildForecastField(dayData.getEvening()));
            builder.addField(buildForecastField(dayData.getOvernight()));
        }
        builder.setImage(forecast.hasImage() ? forecast.getImage() : EmbedHelper.getSpacerImage());
        return builder.build();
    }

    /**
     * Build a field displaying the forecast summary and icon for a section of the day
     *
     * @param section Section of the day
     * @return MessageEmbed field displaying forecast and icon
     */
    private Field buildForecastField(DaySection section) {
        String key = section.getIconKey();
        String icon = iconTypes.getOrDefault(key, missing);
        if(!iconTypes.containsKey(key)) {
            System.out.println(section.getName() + " " + key + ": MISSING");
        }
        return new Field(section.getName() + " " + icon, section.getForecast(), true);
    }

    /**
     * Create an embed showing that the weather look up failed
     *
     * @param builder  Default embed builder
     * @param location Requested location
     * @return Failure detail embed
     */
    private MessageEmbed createFailEmbed(EmbedBuilder builder, String location) {
        return builder
                .setColor(EmbedHelper.getRed())
                .setDescription("I didn't anything for **" + location + "**\n\nSome places don't show up because nobody likes them.")
                .setTitle("Weather Forecast")
                .build();
    }

    /**
     * Create the default embed builder
     *
     * @param help Help message to display
     * @return Default embed builder
     */
    private EmbedBuilder getEmbedBuilder(String help) {
        String metServiceIcon = "https://i.imgur.com/clXlRGC.png";
        return new EmbedBuilder()
                .setThumbnail(metServiceIcon)
                .setFooter("Try: " + help, metServiceIcon);
    }

    /**
     * Parse the forecast from the Metservice API
     *
     * @param location Location to fetch weather for
     * @param tomorrow Tomorrow's forecast
     * @return Weather forecast for location
     */
    private Forecast getForecastData(SearchResult location, boolean tomorrow) {
        try {
            String data = new NetworkRequest(location.getDataUrl(), false).get();
            System.out.println(location.getDataUrl());
            if(data == null) {
                return null;
            }
            JSONObject forecastOverview = new JSONObject(data)
                    .getJSONObject("layout")
                    .getJSONObject("primary")
                    .getJSONObject("slots");

            JSONArray days = forecastOverview
                    .getJSONObject("main")
                    .getJSONArray("modules")
                    .getJSONObject(0)
                    .getJSONArray("days");

            int index = tomorrow ? 1 : 0;
            JSONObject targetDay = days.getJSONObject(index);

            JSONArray localObsContainer = forecastOverview
                    .getJSONObject("left-major")
                    .getJSONArray("modules");

            JSONObject localObs = null;
            if(!localObsContainer.isEmpty()) {
                localObs = localObsContainer.getJSONObject(0);
                if(!localObs.has("observations")) {
                    localObs = null;
                }
            }

            JSONArray images = forecastOverview
                    .getJSONObject("left-minor")
                    .getJSONArray("modules");

            String image = null;
            for(int i = 0; i < images.length(); i++) {
                JSONObject module = images.getJSONObject(i);
                if(module.has("image")) {
                    image = module.getString("image");
                    break;
                }
            }

            JSONObject forecastStats = targetDay
                    .getJSONArray("forecasts")
                    .getJSONObject(0);

            return new Forecast(
                    parseDate(targetDay.getString("date")),
                    parseDate(targetDay.getString("issuedAt")),
                    targetDay.has("statement") ? targetDay.getString("statement") : forecastStats.getString("statement"),
                    targetDay.getDouble("highTemp"),
                    targetDay.getDouble("lowTemp"),
                    targetDay.has("breakdown") ? parseDayData(targetDay.getJSONObject("breakdown")) : null,
                    tomorrow || localObs == null ? null : parseLocalObs(localObs),
                    image
            );
        }
        catch(JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Parse the daily breakdown data to a DayData object
     *
     * @param breakdown Daily breakdown data - morning forecast, evening forecast..
     * @return DayData object
     */
    private DayData parseDayData(JSONObject breakdown) {
        return new DayData(
                breakdown.getJSONObject("morning"),
                breakdown.getJSONObject("afternoon"),
                breakdown.getJSONObject("evening"),
                breakdown.getJSONObject("overnight")
        );
    }

    /**
     * Parse the local observation data to an object
     *
     * @param localObs Local observation data - clothing layers, wind speed etc
     * @return LocalObs object
     */
    private LocalObs parseLocalObs(JSONObject localObs) {
        try {
            List<String> found = Arrays.asList("temperature", "clothing", "rain", "wind", "pressure");
            JSONObject observations = localObs.getJSONObject("observations");
            JSONObject temperature = observations.getJSONObject("temperature");

            JSONObject clothing = observations.has("clothing") ? observations.getJSONObject("clothing") : null;
            JSONObject rain = observations.has("rain") ? observations.getJSONObject("rain") : null;
            JSONObject wind = observations.has("wind") ? observations.getJSONObject("wind") : null;
            JSONObject pressureData = observations.has("pressure") ? observations.getJSONObject("pressure") : null;
            for(String key : observations.keySet()) {
                if(!found.contains(key)) {
                    System.out.println(key + ": MISSING");
                }
            }
            String layers = null, windProof = null;
            double rainFall = -1;
            int humidity = -1, windSpeed = -1, pressure = -1;
            String windStrength = null, windDirection = null, trend = null;
            double current = parseAmbiguousValue(temperature.get("current"));
            double feelsLike = parseAmbiguousValue(temperature.get("feelsLike"));

            if(clothing != null) {
                layers = clothing.getString("layers");
                windProof = clothing.getString("windproofLayers");
            }
            if(rain != null) {
                if(rain.has("relativeHumidity")) {
                    humidity = (int) parseAmbiguousValue(rain.get("relativeHumidity"));
                }
                if(rain.has("rainfall")) {
                    rainFall = parseAmbiguousValue(rain.get("rainfall"));
                }
            }
            if(wind != null) {
                windDirection = wind.getString("direction");
                windSpeed = (int) parseAmbiguousValue(wind.get("averageSpeed"));
                windStrength = wind.getString("strength");
                if(windDirection.equalsIgnoreCase(windStrength)) {
                    windDirection = null;
                }
            }
            if(pressureData != null) {
                pressure = (int) parseAmbiguousValue(pressureData.get("atSeaLevel"));
                trend = pressureData.getString("trend");
            }
            return new LocalObs(
                    layers,
                    windProof,
                    humidity,
                    localObs.has("issuedAt") ? parseDate(localObs.getString("issuedAt")) : null,
                    current,
                    feelsLike,
                    rainFall,
                    windDirection,
                    windSpeed,
                    windStrength,
                    pressure,
                    trend
            );
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Some values can be String or number, convert to double
     *
     * @param o Object number or String
     * @return double
     */
    private double parseAmbiguousValue(Object o) {
        if(o instanceof String) {
            return o.equals("n/a") ? -1 : Double.parseDouble((String) o);
        }
        if(o instanceof Double) {
            return (double) o;
        }
        return (int) o;
    }

    /**
     * Parse an ISO date to formatted date String
     *
     * @param date Date String
     * @return Formatted date
     */
    private Date parseDate(String date) {
        try {
            String format = (date.contains("pm") || date.contains("am")) ? "h:mma EEEE d MMM Y" : "yyyy-MM-dd'T'HH:mm:ssX";
            return new SimpleDateFormat(format).parse(date);
        }
        catch(ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Discern the best location to use for weather data.
     * <p>
     * Areas such as Omakau do not provide data through the API,
     * mimic the request Metservice makes when searching on the website to find the best
     * location to use (Omakau = Alexandra).
     * <p>
     * Also can be used to make a partial match such as "Dun" for "Dunedin".
     *
     * @param location Location to correct if required
     * @return Corrected location
     */
    private SearchResult discernBestLocation(String location) {
        JSONArray data = new JSONObject(
                new NetworkRequest(
                        "https://qm8m7k2q6x-dsn.algolia.net/1/indexes/test_locations/query?" + Secret.getMetServiceAppID() + "&" + Secret.getMetServiceKey(),
                        false
                )
                        .post(new JSONObject().put("params", "query=" + location).toString())

        ).getJSONArray("hits");
        SearchResults results = new SearchResults(data);
        return results.hasResults() ? results.getBestResult() : null;
    }

    /**
     * Hold daily forecast data - projected min/max temp etc
     */
    private static class Forecast {
        private final String forecast, image;
        private final double max, min;
        private final DayData dayData;
        private final LocalObs localObs;
        private final Date date, issued;

        /**
         * Create a forecast
         *
         * @param date     Date of forecast
         * @param issued   Time data was fetched
         * @param forecast Forecast summary
         * @param max      Maximum projected temperature
         * @param min      Minimum projected temperature
         * @param dayData  Data for each section of the day
         * @param localObs Local observation data for date (only if current date)
         * @param image    Rain radar
         */
        public Forecast(Date date, Date issued, String forecast, double max, double min, DayData dayData, LocalObs localObs, String image) {
            this.date = date;
            this.issued = issued;
            this.forecast = forecast.length() > 100 ? (forecast.substring(0, 100) + "...") : forecast;
            this.max = max;
            this.min = min;
            this.dayData = dayData;
            this.localObs = localObs;
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
        public boolean hasLocalObs() {
            return localObs != null;
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
         * Get forecast summary
         *
         * @return Forecast summary
         */
        public String getForecast() {
            return forecast;
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
            switch(day % 10) {
                case 1:
                    return "st";
                case 2:
                    return "nd";
                case 3:
                    return "rd";
                default:
                    return "th";
            }
        }

        /**
         * Get date that data was issued
         *
         * @return Issue date of data
         */
        public Date getIssued() {
            return issued;
        }

        /**
         * Get day part data
         *
         * @return Day part data
         */
        public DayData getDayData() {
            return dayData;
        }

        /**
         * Get maximum temperature
         *
         * @return Maximum temperature
         */
        public double getMax() {
            return max;
        }

        /**
         * Return presence of daily breakdown data
         *
         * @return Day data exists
         */
        public boolean hasDayData() {
            return dayData != null;
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
         * Get minimum temperature
         *
         * @return Minimum temperature
         */
        public double getMin() {
            return min;
        }

        /**
         * Get local forecast observation data
         *
         * @return Local observation data
         */
        public LocalObs getLocalObs() {
            return localObs;
        }
    }

    /**
     * Hold local observation data - current temperature etc
     */
    private static class LocalObs {
        private final String clothing, windDirection, windClothing, windStrength, pressureTrend;
        private final double temp, feelsLike, rainFall;
        private final int windSpeed, humidity, pressure;
        private final Date dateIssued;

        /**
         * Create a Local observation
         *
         * @param clothing      Number of clothing layers
         * @param windClothing  Number of windproof clothing layers
         * @param humidity      Humidity percentage
         * @param dateIssued    Date of observation
         * @param temp          Current temperature
         * @param feelsLike     Temperature feels like
         * @param rainFall      Rainfall in mm
         * @param windDirection Compass direction of wind
         * @param windSpeed     km/h wind speed
         * @param windStrength  Description of wind strength - "Light Winds"
         * @param pressure      Pressure
         * @param pressureTrend Trend of pressure - "rising"
         */
        public LocalObs(String clothing, String windClothing, int humidity, Date dateIssued, double temp, double feelsLike, double rainFall, String windDirection, int windSpeed, String windStrength, int pressure, String pressureTrend) {
            this.clothing = clothing;
            this.windClothing = windClothing;
            this.humidity = humidity;
            this.dateIssued = dateIssued;
            this.temp = temp;
            this.feelsLike = feelsLike;
            this.rainFall = rainFall;
            this.windDirection = windDirection;
            this.windSpeed = windSpeed;
            this.windStrength = windStrength;
            this.pressure = pressure;
            this.pressureTrend = StringUtils.capitalize(pressureTrend);
        }

        /**
         * Format the wind direction and speed
         *
         * @return Wind information String
         */
        public String formatWindDetails() {
            StringBuilder builder = new StringBuilder();
            if(windSpeed > 0) {
                builder.append(windSpeed).append(" ").append("km/h ");
            }
            if(windDirection != null) {
                builder.append(windDirection);
            }
            if(builder.length() > 0) {
                builder.append("\n");
            }
            return builder.append(windStrength).toString();
        }

        /**
         * Format the pressure and pressure trend
         *
         * @return Pressure information String
         */
        public String formatPressureDetails() {
            return pressure + " hPa\n" + pressureTrend;
        }

        /**
         * Get the rainfall in mm
         *
         * @return Rainfall
         */
        public String formatRainfall() {
            return rainFall + " mm";
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
         * Get date of local observation
         *
         * @return Date
         */
        public Date getDateIssued() {
            return dateIssued;
        }

        /**
         * Get the current humidity percentage
         *
         * @return Humidity
         */
        public String formatHumidity() {
            return humidity + "%";
        }

        /**
         * Get the current temperature
         *
         * @return Current temperature
         */
        public double getTemp() {
            return temp;
        }

        /**
         * Format clothing to a String
         *
         * @return Formatted clothing String
         */
        public String formatClothing() {
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
            return pressureTrend != null && pressure > -1;
        }

        /**
         * Return presence of wind data
         *
         * @return Wind data exists
         */
        public boolean hasWind() {
            return windStrength != null;
        }

        /**
         * Return presence of current temperature data
         *
         * @return Temperature data exists
         */
        public boolean hasTemp() {
            return temp > -1 && feelsLike > -1;
        }

        /**
         * Return presence of rainfall data
         *
         * @return Rainfall data exists
         */
        public boolean hasRainfall() {
            return rainFall > 0;
        }

        /**
         * Return presence of humidity data
         * -
         *
         * @return Humidity data exists
         */
        public boolean hasHumidity() {
            return humidity > 0;
        }
    }

    /**
     * Hold forecast summary for each section of a day
     */
    private static class DayData {
        private final DaySection morning, afternoon, evening, overnight;

        /**
         * Create Day data
         *
         * @param morning   Morning forecast summary
         * @param afternoon Afternoon forecast summary
         * @param evening   Evening forecast summary
         * @param overnight Overnight forecast summary
         */
        public DayData(JSONObject morning, JSONObject afternoon, JSONObject evening, JSONObject overnight) {
            String CONDITION = "condition", NIGHT = "NIGHT", DAY = "DAY";
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
    }

    /**
     * Hold a section of a day
     */
    private static class DaySection {
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

    /**
     * Wrap the MetService location search results
     */
    private static class SearchResults {
        private final ArrayList<SearchResult> results = new ArrayList<>();

        /**
         * Hold a list of search results
         *
         * @param results List of search results
         */
        public SearchResults(JSONArray results) {
            List<String> acceptable = Arrays.asList("Towns & Cities", "Rural", "Rural region");
            for(int i = 0; i < results.length(); i++) {
                JSONObject result = results.getJSONObject(i);
                if(!acceptable.contains(result.getString("type")) || !result.getString("url").contains("locations")) {
                    continue;
                }
                this.results.add(new SearchResult(result));
            }
        }

        /**
         * Return presence of results
         *
         * @return Results exist
         */
        public boolean hasResults() {
            return !results.isEmpty();
        }

        /**
         * Get the best result
         *
         * @return Best weighted search result
         */
        public SearchResult getBestResult() {
            return results.isEmpty() ? null : results.get(0);
        }
    }

    /**
     * Wrap a location search result
     */
    private static class SearchResult {
        private final String location, dataURL, webURL;

        /**
         * Create a search result
         *
         * @param result Location search result
         */
        public SearchResult(JSONObject result) {
            String url = result.getString("url");
            String metService = "https://www.metservice.com";
            String prefix = "/publicData/webdata";
            this.dataURL = metService + prefix + url;
            this.webURL = metService + url;
            String[] title = url.substring(url.lastIndexOf("/") + 1).split("-");
            for(int i = 0; i < title.length; i++) {
                title[i] = StringUtils.capitalize(title[i]);
            }
            this.location = StringUtils.join(title, " ");
        }

        /**
         * Get the location
         *
         * @return Location
         */
        public String getLocation() {
            return location;
        }

        /**
         * Get the URL to the location data
         *
         * @return Location data URL
         */
        public String getDataUrl() {
            return dataURL;
        }

        /**
         * Get the URL to the web view of location
         *
         * @return Location view URL
         */
        public String getWebURL() {
            return webURL;
        }
    }
}
