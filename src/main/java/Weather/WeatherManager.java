package Weather;

import Command.Structure.EmbedHelper;
import Command.Structure.EmoteHelper;
import Network.NetworkRequest;
import Network.NetworkResponse;
import Network.Secret;
import Weather.DayForecastBreakdown.DaySection;
import Weather.LocalObservations.LocalObservationsBuilder;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.MessageEmbed.Field;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class WeatherManager {
    private final HashMap<String, String> iconTypes = new HashMap<>();
    private final String maxIcon, minIcon, clothing, humidity, rain, wind, gust, pressure, missing, help;
    private static final String
            ICON = "https://i.imgur.com/wDgQGMw.png",
            LOGO = "https://i.imgur.com/clXlRGC.png",
            BASE_URL = "https://www.metservice.com";

    /**
     * Create the weather manager and initialise the required emotes for building weather messages
     *
     * @param emoteHelper Emote helper
     * @param help        Help message to use in embeds
     */
    public WeatherManager(EmoteHelper emoteHelper, String help) {
        this.help = help;
        this.maxIcon = emoteHelper.getMaxTemp().getAsMention();
        this.minIcon = emoteHelper.getMinTemp().getAsMention();
        this.rain = emoteHelper.getRain().getAsMention();
        this.humidity = emoteHelper.getHumidity().getAsMention();
        this.wind = emoteHelper.getWind().getAsMention();
        this.gust = emoteHelper.getGust().getAsMention();
        this.clothing = emoteHelper.getClothing().getAsMention();
        this.missing = emoteHelper.getFail().getAsMention();
        this.pressure = emoteHelper.getPressure().getAsMention();

        iconTypes.put("DAY_FINE", emoteHelper.getDayFine().getAsMention());
        iconTypes.put("DAY_FEW_SHOWERS", emoteHelper.getDayFewShowers().getAsMention());
        iconTypes.put("DAY_PARTLY_CLOUDY", emoteHelper.getDayPartlyCloudy().getAsMention());
        iconTypes.put("DAY_THUNDER", emoteHelper.getDayThunder().getAsMention());

        iconTypes.put("NIGHT_PARTLY_CLOUDY", emoteHelper.getNightPartlyCloudy().getAsMention());
        iconTypes.put("NIGHT_FINE", emoteHelper.getNightFine().getAsMention());
        iconTypes.put("NIGHT_FOG", emoteHelper.getNightFog().getAsMention());
        iconTypes.put("NIGHT_FROST", emoteHelper.getNightFrost().getAsMention());
        iconTypes.put("NIGHT_FEW_SHOWERS", emoteHelper.getNightFewShowers().getAsMention());

        String showers = emoteHelper.getShowers().getAsMention();
        String cloudy = emoteHelper.getCloudy().getAsMention();
        String windy = emoteHelper.getWindy().getAsMention();
        String windRain = emoteHelper.getWindRain().getAsMention();
        String snow = emoteHelper.getSnow().getAsMention();
        String drizzle = emoteHelper.getNightDrizzle().getAsMention();
        String rain = emoteHelper.getRain().getAsMention();

        iconTypes.put("NIGHT_DRIZZLE", drizzle);
        iconTypes.put("DAY_DRIZZLE", drizzle);
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
        iconTypes.put("DAY_RAIN", rain);
        iconTypes.put("NIGHT_RAIN", rain);

        // Extremes
        iconTypes.put("Highest", maxIcon);
        iconTypes.put("Lowest", minIcon);
        iconTypes.put("Windiest", wind);
        iconTypes.put("Wettest", rain);
    }

    /**
     * Get a message embed detailing the current weather extremes.
     * Weather extremes are today's top locations for various weather events.
     * E.g the location with the most rainfall, wind, etc.
     *
     * @return Weather extremes message embed
     */
    public MessageEmbed getExtremeWeatherEmbed() {
        EmbedBuilder builder = getDefaultEmbedBuilder()
                .setColor(EmbedHelper.ORANGE)
                .setTitle("Current Weather Extremes");

        ArrayList<WeatherExtreme> extremes = getExtremeData();

        for(WeatherExtreme weatherExtreme : extremes) {
            String text = weatherExtreme.getLocationName();

            if(weatherExtreme.hasValue()) {
                text += "\n" + weatherExtreme.getValue();
            }

            builder.addField(
                    weatherExtreme.getTitle() + " " + weatherExtreme.getIcon(),
                    text,
                    true
            );
        }
        return builder.build();
    }

    /**
     * Get the current weather extremes data from MetService.
     * Weather extremes are today's top locations for various weather events.
     * Each extreme includes the location name, the icon key e.g "cloud-rain", the title e.g "Wettest", and the value
     * relative to the title e.g "0.6mm" (as in rainfall) for "Wettest". The value is optional as not all extremes have
     * an associated value.
     *
     * @return List of weather extremes
     */
    private ArrayList<WeatherExtreme> getExtremeData() {
        JSONArray extremesList = new JSONObject(
                new NetworkRequest(BASE_URL + "/publicData/webdata/national", false).get().body
        )
                .getJSONObject("layout")
                .getJSONObject("primary")
                .getJSONObject("slots")
                .getJSONObject("left-minor")
                .getJSONArray("modules")
                .getJSONObject(0)
                .getJSONArray("items");

        ArrayList<WeatherExtreme> weatherExtremes = new ArrayList<>();
        final String valueKey = "value";

        for(int i = 0; i < extremesList.length(); i++) {
            JSONObject extremeData = extremesList.getJSONObject(i);
            String type = extremeData.getString("title"); // e.g "Wettest"
            weatherExtremes.add(
                    new WeatherExtreme(
                            type,
                            iconTypes.getOrDefault(type, missing),
                            extremeData.has(valueKey) ? extremeData.getString(valueKey) : null, // Value is optional
                            extremeData.getString("text") // Location name
                    )
            );
        }
        return weatherExtremes;
    }

    /**
     * Get a list of locations for the given query.
     * Remove results which do not provide weather forecasts.
     *
     * @param locationQuery Location to search for e.g "Dunedin"
     * @return List of locations for the given query
     */
    public ArrayList<Location> searchLocations(String locationQuery) {
        ArrayList<Location> locations = new ArrayList<>();
        String searchLocationsUrl = addAuthenticationParameters(
                "https://qm8m7k2q6x-dsn.algolia.net/1/indexes/test_locations/query?"
        );

        JSONObject body = new JSONObject().put("params", "query=" + locationQuery);
        NetworkResponse response = new NetworkRequest(searchLocationsUrl, false).post(body.toString());
        JSONArray searchResults = new JSONObject(response.body).getJSONArray("hits");

        // Search results with types outside of these values don't have weather forecasts e.g "Weather Station"
        final List<String> acceptableTypes = Arrays.asList("Towns & Cities", "Rural", "Rural region");

        for(int i = 0; i < searchResults.length(); i++) {
            JSONObject searchResult = searchResults.getJSONObject(i);
            String type = searchResult.getString("type");

            // URL to location forecast relative to the MetService base URL e.g "/towns-cities/locations/dunedin"
            String relativeUrl = searchResult.getString("url");

            /*
             * Some locations have an acceptable type but the URL leads somewhere which does not provide a
             * forecast. E.g the "/rural/regions/dunedin" relative URL has the acceptable type "Rural region" but
             * leads to a generic forecast of every location in the Dunedin region,
             * rather than a specific Dunedin forecast.
             */
            if(!acceptableTypes.contains(type) || !relativeUrl.contains("locations")) {
                continue;
            }

            /*
             * The search result may be titled as one location, but lead to another. E.g "Omakau" results will have a
             * "/rural/regions/central-otago/locations/alexandra" relative URL. Build the actual location name from this
             * URL.
             */
            String[] nameArgs = relativeUrl.substring(relativeUrl.lastIndexOf("/") + 1).split("-");
            for(int j = 0; j < nameArgs.length; j++) {
                nameArgs[j] = StringUtils.capitalize(nameArgs[j]);
            }

            /*
             * Details on why the query returned this result e.g "<em>Toato</em>a, Ōpōtiki" where the query was "tomato".
             * Emphasis tags outline where the match took place, replace to have bold Discord markup
             */
            String matchDetails = "\"" + searchResult
                    .getJSONObject("_highlightResult")
                    .getJSONObject("title")
                    .getString("value")
                    .replace("<em>", "**")
                    .replace("</em>", "**") + "\"";

            locations.add(
                    new Location(
                            StringUtils.join(nameArgs, " "),
                            BASE_URL + "/publicData/webdata" + relativeUrl,
                            BASE_URL + relativeUrl,
                            "**Matched on**: " + matchDetails
                    )
            );
        }
        return locations;
    }

    /**
     * Add the URL parameters required to authenticate with MetService to the given URL
     *
     * @param url URL to add parameters to
     * @return URL with authentication parameters
     */
    private String addAuthenticationParameters(String url) {
        return url
                + "x-algolia-api-key=" + Secret.METSERVICE_KEY
                + "&x-algolia-application-id=" + Secret.METSERVICE_APP_ID;
    }

    /**
     * Build a message embed detailing the given weather forecast
     *
     * @param location Location of weather forecast
     * @param forecast Weather forecast details
     * @param tomorrow Displaying tomorrow's forecast (today's forecast if false)
     * @return Message embed detailing forecast
     */
    public MessageEmbed buildForecastEmbed(Location location, Forecast forecast, boolean tomorrow) {
        double maxTemp = forecast.getMaximumTemperature();
        double minTemp = forecast.getMinimumTemperature();

        EmbedBuilder builder = getDefaultEmbedBuilder()
                .setColor(EmbedHelper.BLUE)
                .setTitle(
                        location.getName() + " Weather Forecast | " + (tomorrow ? "Tomorrow" : "Today"),
                        location.getWebURL()
                )
                .setDescription("Forecast for " + forecast.formatDate() + "\n\n" + forecast.getStatement() + ".\n\n" + location.getMatchDetails())
                .addField("Max " + maxIcon, Forecast.formatTemperature(maxTemp), true)
                .addField("Min " + minIcon, Forecast.formatTemperature(minTemp), true);

        // Generate fields for the local observations - wind speed, clothing layers, etc
        if(forecast.hasLocalObservations()) {
            ArrayList<Field> observationFields = getLocalObservationFields(
                    forecast.getLocalObservations(),
                    minTemp,
                    maxTemp
            );
            for(Field field : observationFields) {
                builder.addField(field);
            }
        }

        // Max fields that can be displayed horizontally
        final int maxRowWidth = 3;

        // How many fields are required to max out the current row
        final int paddingRequired = maxRowWidth - (builder.getFields().size() % maxRowWidth);

        // Pad out the current row such that the day breakdown begins on a new row
        if(paddingRequired != maxRowWidth) {
            for(int i = 0; i < paddingRequired; i++) {
                builder.addBlankField(true);
            }
        }

        if(forecast.hasDayData()) {
            DayForecastBreakdown dayForecastBreakdown = forecast.getDayData();
            builder
                    .addField(buildForecastField(dayForecastBreakdown.getMorning()))
                    .addField(buildForecastField(dayForecastBreakdown.getAfternoon()))
                    .addField(buildForecastField(dayForecastBreakdown.getEvening()))
                    .addField(buildForecastField(dayForecastBreakdown.getOvernight()));
        }
        return builder
                .setImage(forecast.hasImage() ? forecast.getImage() : EmbedHelper.SPACER_IMAGE)
                .build();
    }

    /**
     * Create a list of message embed fields from the given local observations of a weather forecast.
     * Local observations are the observations of various weather events for a forecast, e.g wind speed, rainfall etc
     * Create a message embed field for each observation and add it to the list
     *
     * @param localObservations Forecast local observations
     * @param minTemp           Minimum temperature for the forecast
     * @param maxTemp           Maximum temperature for the forecast
     * @return List of message embed fields
     */
    private ArrayList<Field> getLocalObservationFields(LocalObservations localObservations, double minTemp, double maxTemp) {
        ArrayList<Field> fields = new ArrayList<>();

        double currentTemp = localObservations.getTemperature();

        // The current temperature is closer to the maximum temperature than the minimum temperature
        boolean closerToMax = Math.abs(currentTemp - maxTemp) < Math.abs(currentTemp - minTemp);
        fields.add(
                new Field(
                        "Now" + (closerToMax ? maxIcon : minIcon) + " Feel",
                        Forecast.formatTemperature(currentTemp)
                                + " | "
                                + Forecast.formatTemperature(localObservations.getFeelsLike()),
                        true
                )
        );

        if(localObservations.hasClothing()) {
            fields.add(
                    new Field("Clothing " + clothing, localObservations.getFormattedClothing(), true)
            );
        }

        if(localObservations.hasWind()) {
            fields.add(
                    new Field("Wind " + wind, localObservations.getFormattedWindDetails(), true)
            );
            if(localObservations.hasGustSpeed()) {
                fields.add(
                        new Field("Gust " + gust, localObservations.getFormattedGustSpeed(), true)
                );
            }
        }

        if(localObservations.hasRainfall()) {
            fields.add(
                    new Field("Rainfall " + rain, localObservations.getFormattedRainfall(), true)
            );
        }

        if(localObservations.hasHumidity()) {
            fields.add(
                    new Field("Humidity " + humidity, localObservations.getFormattedHumidity(), true)
            );
        }

        if(localObservations.hasPressure()) {
            fields.add(
                    new Field(
                            "Pressure " + pressure,
                            localObservations.getFormattedPressureDetails(),
                            true
                    )
            );
        }
        return fields;
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
     * Create a message embed showing that the weather look up failed (no results found)
     *
     * @param locationQuery Location query which returned no results
     * @return Failed weather lookup message embed
     */
    public MessageEmbed getFailedLookupEmbed(String locationQuery) {
        return getDefaultEmbedBuilder()
                .setColor(EmbedHelper.RED)
                .setDescription(
                        "I didn't find any locations for your query: **"
                                + locationQuery
                                + "**\n\nSome places don't show up because nobody likes them."
                )
                .setTitle("Weather | No locations found")
                .build();
    }

    /**
     * Get a message embed showing that the given location had no forecast data available
     *
     * @param location Location which failed to provide forecast data
     * @return No forecast data embed
     */
    public MessageEmbed getNoForecastEmbed(Location location) {
        return getDefaultEmbedBuilder()
                .setColor(EmbedHelper.RED)
                .setTitle("Weather | No forecast data")
                .setDescription(
                        "I wasn't able to find any forecast data for the location: **" + location.getName() + "**"
                )
                .build();
    }

    /**
     * Create the default embed builder
     * This is an embed builder with the MetService logo as the thumbnail, and the help text as the footer.
     *
     * @return Default embed builder
     */
    private EmbedBuilder getDefaultEmbedBuilder() {
        return new EmbedBuilder()
                .setThumbnail(LOGO)
                .setFooter(help, ICON);
    }

    /**
     * Get the weather forecast JSON data from the MetService API
     *
     * @param location Location to get forecast data for
     * @return Forecast data or null
     */
    @Nullable
    private JSONObject getForecastData(Location location) {
        String data = new NetworkRequest(location.getDataUrl(), false).get().body;
        if(data == null) {
            System.out.println(location.getDataUrl());
            return null;
        }
        return new JSONObject(data)
                .getJSONObject("layout")
                .getJSONObject("primary")
                .getJSONObject("slots");
    }

    /**
     * Get the weather forecast for the given location from the Metservice API
     *
     * @param location Location to fetch forecast for
     * @param tomorrow Use tomorrow's forecast (will use today's if false)
     * @return Weather forecast for location or null
     */
    @Nullable
    public Forecast getForecast(Location location, boolean tomorrow) {
        JSONObject forecastData = getForecastData(location);

        if(forecastData == null) {
            return null;
        }

        // Array of forecast data, each object is the forecast for a given date - starting with today at index 0
        JSONArray days = forecastData
                .getJSONObject("main")
                .getJSONArray("modules")
                .getJSONObject(0)
                .getJSONArray("days");

        // Use today or tomorrow's forecast
        JSONObject targetDay = days.getJSONObject(tomorrow ? 1 : 0);

        // Array possibly containing local observations for the forecast but may be empty
        JSONArray localObsContainer = forecastData
                .getJSONObject("left-major")
                .getJSONArray("modules");

        // Get the local observations object if it is present
        JSONObject localObs = null;
        if(!localObsContainer.isEmpty()) {
            localObs = localObsContainer.getJSONObject(0);
            if(!localObs.has("observations")) {
                localObs = null;
            }
        }

        // Array possibly containing satellite images of the forecast but may be empty
        JSONArray images = forecastData
                .getJSONObject("left-minor")
                .getJSONArray("modules");

        // Get the first available satellite image of the forecast
        final String imageKey = "image";
        String image = null;
        for(int i = 0; i < images.length(); i++) {
            JSONObject module = images.getJSONObject(i);
            if(module.has(imageKey)) {
                image = module.getString(imageKey);
                break;
            }
        }

        /*
         * Statement about the forecast - e.g "Fine. Northerly breezes".
         * Either present as a statement about the day overall, or a statement about a specific part of the day.
         */
        final String statementKey = "statement";
        String statement = targetDay.has(statementKey)
                ? targetDay.getString(statementKey) // Statement about day overall

                // No overall statement, take the statement about the forecast at sunrise (index 0)
                : targetDay.getJSONArray("forecasts").getJSONObject(0).getString(statementKey);

        final String breakdownKey = "breakdown";
        return new Forecast(
                parseDate(targetDay.getString("date")), // Date of forecast
                parseDate(targetDay.getString("issuedAt")), // Date which data was issued by MetService
                statement,
                targetDay.getDouble("highTemp"),
                targetDay.getDouble("lowTemp"),
                targetDay.has(breakdownKey) ? parseForecastBreakdown(targetDay.getJSONObject(breakdownKey)) : null,
                tomorrow || localObs == null ? null : parseLocalObservations(localObs), // Only today's forecast has observations
                image
        );
    }

    /**
     * Parse the forecast breakdown from the given JSON.
     * The JSON contains the forecast broken down by parts of the day.
     * Create a DayForecastBreakdown to hold this information.
     *
     * @param breakdown Forecast JSON data broken down by parts of the day - morning forecast, evening forecast..
     * @return Forecast breakdown
     */
    private DayForecastBreakdown parseForecastBreakdown(JSONObject breakdown) {
        return new DayForecastBreakdown(
                breakdown.getJSONObject("morning"),
                breakdown.getJSONObject("afternoon"),
                breakdown.getJSONObject("evening"),
                breakdown.getJSONObject("overnight")
        );
    }

    /**
     * Parse the given local observation data in to an object.
     * Local observations are the observations of various weather events for a forecast, e.g wind speed or rainfall
     *
     * @param localObservationsData Local observation data from the MetService API
     * @return Local observations
     */
    private LocalObservations parseLocalObservations(JSONObject localObservationsData) {
        JSONObject observationsData = localObservationsData.getJSONObject("observations");
        JSONObject temperature = observationsData.getJSONObject("temperature");

        LocalObservationsBuilder observationsBuilder = new LocalObservationsBuilder(
                temperature.getDouble("current"),
                temperature.getDouble("feelsLike")
        );

        final String clothingKey = "clothing";
        if(observationsData.has(clothingKey)) {
            JSONObject clothing = observationsData.getJSONObject("clothing");
            observationsBuilder.setClothing(
                    clothing.getString("layers"),
                    clothing.getString("windproofLayers")
            );
        }

        final String rainKey = "rain";
        if(observationsData.has(rainKey)) {
            JSONObject rain = observationsData.getJSONObject("rain");
            final String
                    humidityKey = "relativeHumidity",
                    rainfallKey = "rainfall";

            Double humidity = null;
            if(rain.has(humidityKey)) {
                humidity = parseAmbiguousValue(rain.get(humidityKey));
            }

            observationsBuilder.setRainfall(
                    rain.has(rainfallKey) ? parseAmbiguousValue(rain.get(rainfallKey)) : null,
                    humidity == null ? null : humidity.intValue()
            );
        }

        final String windKey = "wind";
        if(observationsData.has(windKey)) {
            JSONObject wind = observationsData.getJSONObject(windKey);

            String windDirection = wind.getString("direction");
            String windStrength = wind.getString("strength");
            Double avgSpeed = parseAmbiguousValue(wind.get("averageSpeed"));

            final String gustKey = "gustSpeed";
            Double gustSpeed = null;
            if(wind.has(gustKey)) {
                gustSpeed = parseAmbiguousValue(wind.get(gustKey));
            }

            // Direction and strength may both be the same value
            observationsBuilder.setWind(
                    avgSpeed == null ? null : avgSpeed.intValue(),
                    gustSpeed == null ? null : gustSpeed.intValue(),
                    windDirection.equalsIgnoreCase(windStrength) ? null : windDirection,
                    windStrength
            );
        }

        final String pressureKey = "pressure";
        if(observationsData.has(pressureKey)) {
            JSONObject pressureData = observationsData.getJSONObject(pressureKey);
            Double pressure = parseAmbiguousValue(pressureData.get("atSeaLevel"));
            observationsBuilder.setPressure(
                    pressure == null ? null : pressure.intValue(),
                    pressureData.getString("trend")
            );
        }
        return observationsBuilder.build();
    }

    /**
     * Some values can be String, Integer, or Double, convert to Double
     *
     * @param o Object value
     * @return Double
     */
    @Nullable
    private Double parseAmbiguousValue(Object o) {
        if(o instanceof String) {
            return o.equals("n/a") ? null : Double.parseDouble((String) o);
        }
        if(o instanceof Double) {
            return (double) o;
        }
        else {
            return (double) (int) o;
        }
    }

    /**
     * Parse a MetService ISO date String to a Date
     *
     * @param dateString Date String
     * @return Date of date String (or today's date if unable to parse)
     */
    private Date parseDate(String dateString) {
        try {
            String format = (dateString.contains("pm") || dateString.contains("am"))
                    ? "h:mma EEEE d MMM Y"
                    : "yyyy-MM-dd'T'HH:mm:ssX";
            return new SimpleDateFormat(format).parse(dateString);
        }
        catch(ParseException e) {
            return new Date();
        }
    }
}
