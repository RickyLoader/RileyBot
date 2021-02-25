package Command.Structure;

import Bot.ResourceHandler;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.json.JSONArray;
import org.json.JSONObject;

import java.awt.*;
import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * Commonly used features, colours, and images of an embedded message
 */
public class EmbedHelper {
    public static final int
            BLUE = 1942002,
            GREEN = 65280,
            PURPLE = 16711935,
            YELLOW = 16776960,
            RED = 16711680,
            ORANGE = 0xd89620,
            URBAN_DICT_BLUE = 741095,
            RUNESCAPE_YELLOW = 16747520,
            RUNESCAPE_ORANGE = 13937190,
            RUNESCAPE_RED = 11672635,
            RUNESCAPE_BLUE = 6206918,
            FIRE_ORANGE = 16544773;

    public static final String
            CLOCK_GIF = "https://i.imgur.com/v2u22T6.gif",
            CLOCK_STOPPED = "https://i.imgur.com/SnoP3Bf.png",
            SPACER_IMAGE = "https://i.imgur.com/24Xf03H.png",
            OSRS_LOGO = "https://i.imgur.com/Hoke7jA.png",
            BLANK_CHAR = "\u200e";


    public static final HashMap<String, String> languages = parseLanguages();

    /**
     * Parse the language JSON in to a map of ISO code -> Language name
     *
     * @return Map of ISO code -> Language name
     */
    private static HashMap<String, String> parseLanguages() {
        HashMap<String, String> languages = new HashMap<>();
        JSONArray allLanguages = new JSONArray(
                new ResourceHandler().getResourceFileAsString("/Movie/languages.json")
        );
        for(int i = 0; i < allLanguages.length(); i++) {
            JSONObject lang = allLanguages.getJSONObject(i);
            languages.put(lang.getString("iso_639_1"), lang.getString("english_name"));
        }
        return languages;
    }

    /**
     * Get a random colour
     *
     * @return Random colour
     */
    public static Color getRandomColour() {
        Random rand = new Random();
        return new Color(
                rand.nextFloat(),
                rand.nextFloat(),
                rand.nextFloat()
        );
    }

    /**
     * Get a language full name - e.g "English" from the ISO code - e.g "en"
     *
     * @param iso Language ISO code - e.g "en"
     * @return Language name - e.g "English"
     */
    public static String getLanguageFromISO(String iso) {
        return languages.get(iso);
    }

    /**
     * Create a field with an invisible character for a title, and the value as a value.
     * Allows a column-esq appearance without having a title above every value
     *
     * @param value Value to use
     * @return Value field
     */
    public static MessageEmbed.Field getValueField(String value) {
        return new MessageEmbed.Field(BLANK_CHAR, value, true);
    }

    /**
     * Create a field with a title and value
     *
     * @param title Title to use
     * @param value Value to use
     * @return Title field
     */
    public static MessageEmbed.Field getTitleField(String title, String value) {
        return new MessageEmbed.Field("**" + title + "**", value, true);
    }

    /**
     * Get n blank characters
     *
     * @param n Quantity
     * @return N Blank characters
     */
    public static String getBlankChar(int n) {
        StringBuilder builder = new StringBuilder();
        for(int i = 0; i < n; i++) {
            builder.append(BLANK_CHAR);
        }
        return builder.toString();
    }

    /**
     * Format the given time in ms to HH:MM:SS
     *
     * @param time Time in ms
     * @return Formatted time String
     */
    public static String formatDuration(long time) {
        return String.format("%02d:%02d:%02d",
                TimeUnit.MILLISECONDS.toHours(time),
                TimeUnit.MILLISECONDS.toMinutes(time) -
                        TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(time)),
                TimeUnit.MILLISECONDS.toSeconds(time) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(time)));
    }

    /**
     * Return the String required to create a hyperlink in an embed
     *
     * @param text Text to display
     * @param url  URL to link to
     * @return Hyperlink
     */
    public static String embedURL(String text, String url) {
        return "[" + text + "](" + url + ")";
    }
}
