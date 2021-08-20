package Command.Commands;

import Bot.FontManager;
import Bot.ResourceHandler;
import Command.Structure.*;
import COVID.Country;
import COVID.CovidStats;
import COVID.VirusStats;
import Network.NetworkRequest;
import Network.NetworkResponse;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONObject;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;

/**
 * View COVID cases & vaccines when Brock is busy
 */
public class CovidCommand extends BrockCommand {
    private static final String
            TRIGGER = "covid",
            YESTERDAY_HELP_TEXT = TRIGGER + " [country]",
            TODAY = "-t",
            ADD_MESSAGE = "+",
            TODAY_HELP_TEXT = YESTERDAY_HELP_TEXT + " " + TODAY,
            WHITEBOARD_HELP_TEXT = TRIGGER + " " + ADD_MESSAGE + "[message]",
            ALLOW_NULL_PARAMETER = "allowNull=true",
            BASE_URL = "https://disease.sh/",
            BASE_API_URL = BASE_URL + "v3/covid-19/",
            WHITEBOARD_FILENAME = "whiteboard.png",
            MESSAGE_KEY = "message",
            WHITEBOARD_ENDPOINT = "whiteboard/messages";

    // Location of whiteboard in image
    private static final int
            WHITEBOARD_START_X = 405,
            WHITEBOARD_END_X = 560,
            WHITEBOARD_START_Y = 230;

    private final HashMap<String, byte[]> whiteboardImages;
    private final DecimalFormat statFormat;
    private final String[] brockMessages;
    private final BufferedImage whiteboardImage;
    private final Font font;
    private final Random random;
    private String lastMessage;

    /**
     * Initialise whiteboard images & Brock messages
     */
    public CovidCommand() {
        super(
                TRIGGER,
                "View COVID cases!",
                "\nStats:\n"
                        + "\tToday:\n\t\t" + TODAY_HELP_TEXT
                        + "\n\tYesterday:\n\t\t" + YESTERDAY_HELP_TEXT
                        + "\n\nAdd whiteboard message:\n\t" + WHITEBOARD_HELP_TEXT
        );

        this.random = new Random();
        this.statFormat = new DecimalFormat("#,###");
        this.brockMessages = generateBrockMessages();

        this.whiteboardImage = new ResourceHandler().getImageResource(
                ResourceHandler.COVID_BASE_PATH + WHITEBOARD_FILENAME
        );

        // Whiteboard is rotated - rotate font
        AffineTransform affineTransform = new AffineTransform();
        affineTransform.rotate(Math.toRadians(4), 0, 0);
        this.font = FontManager.WHITEBOARD_FONT.deriveFont(20f).deriveFont(affineTransform);

        // Create and store an image for each stored message
        this.whiteboardImages = new HashMap<>();
        ArrayList<String> whiteboardMessages = fetchStoredMessages();

        for(String message : whiteboardMessages) {
            mapMessage(message);
        }
    }

    /**
     * Get an array of messages talking to Brock to be used in the COVID message embeds.
     *
     * @return Brock messages
     */
    private String[] generateBrockMessages() {
        final String mention = getUserMention();
        return new String[]{
                "I miss you " + mention,
                "It's just not the same " + mention,
                "Where are you " + mention + "?",
                "What do the numbers mean " + mention + "?",
                mention + " " + mention + " " + mention
        };
    }

    /**
     * Fetch the list of stored messages talking to Brock to be used in the whiteboard image.
     *
     * @return Stored whiteboard messages
     */
    private ArrayList<String> fetchStoredMessages() {
        NetworkResponse response = new NetworkRequest(WHITEBOARD_ENDPOINT, true).get();
        JSONArray messageArray = new JSONArray(response.body);

        ArrayList<String> storedMessages = new ArrayList<>();
        for(int i = 0; i < messageArray.length(); i++) {
            storedMessages.add(messageArray.getJSONObject(i).getString(MESSAGE_KEY));
        }
        return storedMessages;
    }

    /**
     * Map the given message to an image of Alf holding a whiteboard with the message on it
     *
     * @param message Message to map
     */
    private void mapMessage(String message) {
        byte[] image = buildWhiteboardImage(message);
        whiteboardImages.put(message, image);
    }

    /**
     * Store the given message in the database asynchronously (who cares if it fails)
     *
     * @param message Message to store
     */
    private void storeMessage(String message) {
        final String body = new JSONObject().put(MESSAGE_KEY, message).toString();
        new NetworkRequest(WHITEBOARD_ENDPOINT, true).post(body, true);
    }

    @Override
    protected void onAbsent(CommandContext context) {
        MessageChannel channel = context.getMessageChannel();
        String query = context.getLowerCaseMessage().replaceFirst(getTrigger(), "").trim();

        // No query provided
        if(query.isEmpty()) {
            channel.sendMessage(getHelpNameCoded()).queue();
            return;
        }

        // Add a message to the pool of possible whiteboard messages
        if(query.startsWith(ADD_MESSAGE)) {
            String message = query
                    .replaceFirst(Pattern.quote(ADD_MESSAGE), "")
                    .replaceAll("\\s+", " ")
                    .trim();

            // Didn't provide a message
            if(message.isEmpty()) {
                channel.sendMessage("```" + WHITEBOARD_HELP_TEXT + "```").queue();
                return;
            }

            channel.sendTyping().queue();

            String createImageDetails;

            if(whiteboardImages.containsKey(message)) {
                createImageDetails = "I've already got that message, here's what it looks like:";
            }

            // Create and map an image to this message, submit to database
            else {
                mapMessage(message);
                storeMessage(message);
                createImageDetails = "I added your message to the possibilities, here's what it will look like:";
            }

            // Show what the image looks like
            channel.sendMessage(
                    context.getMember().getAsMention()
                            + " " + createImageDetails
            )
                    .addFile(whiteboardImages.get(message), WHITEBOARD_FILENAME).queue();
            return;
        }

        boolean yesterday = true;

        // Fetch today's stats instead
        if(query.contains(TODAY)) {
            query = query.replaceFirst(TODAY, "").trim();
            yesterday = false;
        }

        channel.sendTyping().queue();

        JSONObject countryData = fetchCountryData(query, yesterday);
        final String baseResponse = "> " + query + " - Country";


        // No data/country doesn't exist
        if(countryData == null) {
            channel.sendMessage(
                    baseResponse + " not found or doesn't have any cases! Maybe " + getUserMention() + " knows?"
            ).queue();
            return;
        }

        channel.sendMessage(baseResponse + " Found...").queue(message -> channel.sendTyping().queue());

        // Parse stats (includes making a request for vaccine doses)
        CovidStats covidStats = parseCovidStats(countryData, yesterday);

        channel.sendMessage(buildCovidMessage(covidStats))
                .addFile(getRandomWhiteboardImage(), WHITEBOARD_FILENAME)
                .queue();
    }

    /**
     * Get a random whiteboard image to display. Remember the last message that was displayed as to not
     * display it twice in a row.
     *
     * @return Random whiteboard image
     */
    private byte[] getRandomWhiteboardImage() {
        final ArrayList<String> keys = new ArrayList<>(whiteboardImages.keySet());
        String message = null;

        while(message == null || message.equals(lastMessage)) {
            message = keys.get(random.nextInt(keys.size()));
        }

        lastMessage = message;
        return whiteboardImages.get(message);
    }

    /**
     * Build an image of Alf in a hazmat suit holding a whiteboard.
     * Draw the given message on to the whiteboard.
     *
     * @param message Message to draw on the whiteboard
     * @return Byte array of Alf whiteboard image
     */
    private byte[] buildWhiteboardImage(String message) {
        BufferedImage whiteboardImage = ImageBuilder.copyImage(this.whiteboardImage);
        Graphics g = whiteboardImage.getGraphics();

        g.setFont(font);
        g.setColor(Color.BLACK);
        FontMetrics fm = g.getFontMetrics();

        final int whiteboardWidth = WHITEBOARD_END_X - WHITEBOARD_START_X;
        final int whiteboardMid = WHITEBOARD_START_X + (whiteboardWidth / 2);

        final String[] messageWords = message.split(" ");

        int y = WHITEBOARD_START_Y + fm.getMaxAscent();
        String messageBuilder = "";

        for(String currentWord : messageWords) {

            // Shorten long words
            if(fm.stringWidth(currentWord) >= whiteboardWidth) {
                currentWord = fitStringToWidth(currentWord, fm, whiteboardWidth);
            }

            // Check if the current word can be added to what has built up without going off the whiteboard
            String currentLine = (messageBuilder + " " + currentWord).trim();

            // Draw what is built up so far and reset
            if(fm.stringWidth(currentLine) >= whiteboardWidth) {
                g.drawString(
                        messageBuilder,
                        whiteboardMid - (fm.stringWidth(messageBuilder) / 2),
                        y
                );

                // Reset the current line
                currentLine = currentWord;
                y += fm.getMaxAscent();
            }
            messageBuilder = currentLine;
        }

        // Draw the final line
        messageBuilder = fitStringToWidth(messageBuilder, fm, whiteboardWidth);
        g.drawString(
                messageBuilder,
                whiteboardMid - (fm.stringWidth(messageBuilder) / 2),
                y
        );


        g.dispose();

        return ImageLoadingMessage.imageToByteArray(whiteboardImage);
    }

    /**
     * Reduce the length of the given String until it is less than the given width when drawn with the provided
     * font metrics.
     *
     * @param text  Text to fit
     * @param fm    Font metrics
     * @param width Width to fit text in
     * @return Shortened text
     */
    private String fitStringToWidth(String text, FontMetrics fm, int width) {
        while(fm.stringWidth(text) >= width) {
            text = text.substring(0, text.length() - 1);
        }
        return text;
    }

    /**
     * Build a message embed displaying the given COVID stats.
     * Set the image to the filename {@code WHITEBOARD_FILENAME} - to be added when sent.
     *
     * @param stats COVID stats
     * @return Message embed displaying COVID stats
     */
    private MessageEmbed buildCovidMessage(CovidStats stats) {
        Country country = stats.getCountry();
        VirusStats virusStats = stats.getVirusStats();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

        // "5,000,000"
        String populationString = statFormat.format(country.getPopulation());

        // "5,000,000 -1 (Brock isn't here)"
        if(country.getIso().equalsIgnoreCase("nz")) {
            populationString += " - 1\n(Brock isn't here)";
        }

        final boolean isToday = virusStats.isTodayStats();

        return new EmbedBuilder()
                .setTitle(country.getName() + " COVID-19 Stats")
                .setDescription(buildDescription(isToday))
                .setFooter(
                        "Type: " + getTrigger() + " for help | Last refreshed: "
                                + dateFormat.format(virusStats.getLastUpdated()),
                        "https://i.imgur.com/KBYsapG.png"
                )
                .setThumbnail(country.getFlagImageUrl())
                .setImage("attachment://" + WHITEBOARD_FILENAME)
                .setColor(EmbedHelper.YELLOW)
                .addField(
                        getStatField(
                                "**New Cases " + (isToday ? "Today" : "Yesterday") + "**:",
                                virusStats.getNewCases()
                        )
                )
                .addField(
                        getStatField(
                                isToday ? "**Current Active Cases**:" : "**Active Cases Yesterday**:",
                                virusStats.getActiveCases())
                )
                .addField(
                        getStatField(
                                isToday ? "**New Deaths Today**:" : "**Deaths Yesterday**:",
                                virusStats.getNewDeaths()
                        )
                )
                .addField(getStatField("**Total Cases**:", virusStats.getTotalCases()))
                .addField(getStatField("**Total Recovered**:", virusStats.getTotalRecovered()))
                .addField(getStatField("**Total Deaths**:", virusStats.getTotalDeaths()))
                .addField("**Population**:", populationString, true)
                .addField(getStatField("**Vaccine Doses Administered**:", virusStats.getVaccineDoses()))
                .build();
    }

    /**
     * Get a message embed field for the given nullable stat value.
     * Set the value of the field to the either the value of the stat, or a dash (if null).
     *
     * @param name  Name for field - e.g "Total Cases:"
     * @param value Nullable stat value
     * @return Message embed field for stat value
     */
    private MessageEmbed.Field getStatField(String name, @Nullable Integer value) {
        return new MessageEmbed.Field(
                name,
                value == null ? "-" : statFormat.format(value),
                true
        );
    }

    /**
     * Build the description to use in a COVID stats message embed.
     *
     * @param forToday Description is for stats from today (default is yesterday)
     * @return Embed description
     */
    private String buildDescription(boolean forToday) {
        String description;

        if(forToday) {
            description = "Here's **today's** stats, **they may be incomplete**." +
                    "\nFor yesterday's, try: `" + YESTERDAY_HELP_TEXT + "`.";
        }
        else {
            description = "Here's the **previous day's** complete stats."
                    + "\nFor today's, try: `" + TODAY_HELP_TEXT + "`.";
        }
        return description + "\n\n" + brockMessages[random.nextInt(brockMessages.length)];
    }

    /**
     * Parse the COVID stats of a country from the given JSON data.
     * Make a further request for vaccine data.
     *
     * @param countryData COVID JSON data for country
     * @param yesterday   Data contains yesterday's stats (default is today's stats)
     * @return COVID stats for country
     */
    private CovidStats parseCovidStats(JSONObject countryData, boolean yesterday) {
        final Country country = parseCountry(countryData);

        return new CovidStats(
                country,
                parseVirusStats(countryData, fetchVaccineDoses(country), yesterday)
        );
    }

    /**
     * Parse a country's virus stats from the given JSON API response
     *
     * @param countryData  COVID JSON data for country
     * @param vaccineDoses Total vaccine doses administered by the country
     * @param yesterday    Data contains yesterday's stats (default is today's stats)
     * @return Virus stats
     */
    private VirusStats parseVirusStats(JSONObject countryData, @Nullable Integer vaccineDoses, boolean yesterday) {
        return new VirusStats.VirusStatsBuilder(
                !yesterday,
                new Date(countryData.getLong("updated"))
        )
                .setTotalCases(getOptionalStat(countryData, "cases"))
                .setTotalDeaths(getOptionalStat(countryData, "deaths"))
                .setTotalRecovered(getOptionalStat(countryData, "recovered"))
                .setActiveCases(getOptionalStat(countryData, "active"))
                .setNewCases(getOptionalStat(countryData, "todayCases"))
                .setNewDeaths(getOptionalStat(countryData, "todayDeaths"))
                .setVaccineDoses(vaccineDoses)
                .build();
    }

    /**
     * Get an optional Integer stat value from the given JSON data of a country's COVID stats.
     * Stats aren't always present depending on country/date of stats.
     *
     * @param data JSON COVID stats
     * @param key  Stat key for retrieving value
     * @return Value or null
     */
    @Nullable
    private Integer getOptionalStat(JSONObject data, String key) {
        return data.has(key) && !data.isNull(key) ? data.getInt(key) : null;
    }

    /**
     * Parse a country from the given JSON API response
     *
     * @param data JSON API response data
     * @return Country
     */
    private Country parseCountry(JSONObject data) {
        JSONObject countryData = data.getJSONObject("countryInfo");

        return new Country(
                countryData.getString("iso2"),
                data.getString("country"), // Name
                data.getLong("population"),
                countryData.getString("flag")
        );
    }

    /**
     * Fetch the total number of vaccine doses that have been administered by the given country.
     *
     * @param country Country to fetch vaccinations for
     * @return Number of vaccine doses or null (if data unavailable)
     */
    @Nullable
    private Integer fetchVaccineDoses(Country country) {
        final String url = BASE_API_URL + "vaccine/coverage/countries/" + country.getIso() + "?" + ALLOW_NULL_PARAMETER;
        NetworkResponse response = new NetworkRequest(url, false).get();

        // No data available for country
        if(response.code != 200) {
            return null;
        }

        JSONObject vaccineData = new JSONObject(response.body);
        final String timelineKey = "timeline";

        // No values
        if(!vaccineData.has(timelineKey) || vaccineData.isNull(timelineKey)) {
            return null;
        }

        // Stored by date, sort by highest
        JSONObject timeline = vaccineData.getJSONObject(timelineKey);
        int[] doses = new int[timeline.keySet().size()];

        int i = 0;
        for(String date : timeline.keySet()) {
            doses[i] = timeline.getInt(date);
            i++;
        }

        // Sort in ascending order
        Arrays.sort(doses);

        // Most recent doses value (unless they un-injected some people)
        return doses[doses.length - 1];
    }

    /**
     * Fetch COVID JSON data by country query - either a country name or ISO code.
     * The data includes details about the country and COVID infection stats etc.
     * Vaccine data is not included.
     *
     * @param query     Country query - name/ISO
     * @param yesterday Fetch yesterday's stats
     * @return COVID JSON data for country or null (if country doesn't exist/no data)
     */
    @Nullable
    private JSONObject fetchCountryData(String query, boolean yesterday) {
        final String url = BASE_API_URL + "countries/" + query + "?yesterday=" + yesterday + "&" + ALLOW_NULL_PARAMETER;
        NetworkResponse response = new NetworkRequest(url, false).get();

        // No stats/country doesn't exist
        if(response.code != 200) {
            return null;
        }

        return new JSONObject(response.body);
    }


    @Override
    public boolean matches(String query, Message message) {
        return query.startsWith(getTrigger());
    }
}
