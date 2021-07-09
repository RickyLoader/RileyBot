package Command.Structure;

import Bot.ResourceHandler;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
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
            OSRS_BANK_TITLE = 16750623,
            RUNESCAPE_RED = 11672635,
            RUNESCAPE_BLUE = 6206918,
            ROW_DARK = 1973790,
            ROW_LIGHT = 3160122,
            FIRE_ORANGE = 16544773;

    public static final String
            CLOCK_GIF = "https://i.imgur.com/v2u22T6.gif",
            CLOCK_STOPPED = "https://i.imgur.com/SnoP3Bf.png",
            SPACER_IMAGE = "https://i.imgur.com/24Xf03H.png",
            OSRS_LOGO = "https://i.imgur.com/Hoke7jA.png",
            BLANK_CHAR = "\u200e";

    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11";


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

    /**
     * Download an image from the given URL connection.
     * Returns null if the image cannot be read.
     *
     * @param connection URL connection to download image from
     * @return Downloaded image or null
     */
    @Nullable
    public static BufferedImage downloadImage(URLConnection connection) {
        try {
            connection.connect();
            return ImageIO.read(connection.getInputStream());
        }
        catch(Exception e) {
            return null;
        }
    }

    /**
     * Download a video from the given URL connection.
     * Returns null if the video size exceeds 8MB or an error occurs.
     *
     * @param connection URL connection to download video from
     * @return Video byte array or null
     */
    public static byte[] downloadVideo(URLConnection connection) {
        try {
            connection.connect();
            InputStream is = connection.getInputStream();
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            byte[] buffer = new byte[8000];
            int bytesIn;
            while((bytesIn = is.read(buffer)) != -1) {
                if((os.size() / 1000) > 8192) {
                    is.close();
                    os.close();
                    throw new IOException("Video too large!");
                }
                os.write(buffer, 0, bytesIn);
            }
            is.close();
            os.close();
            return os.toByteArray();
        }
        catch(IOException e) {
            return null;
        }
    }

    /**
     * Download a video from the given URL.
     * Returns null if the video size exceeds 8MB or an error occurs.
     *
     * @param url Video url
     * @return Video byte array or null
     */
    public static byte[] downloadVideo(String url) {
        try {
            URLConnection connection = new URL(url).openConnection();
            connection.setRequestProperty("User-Agent", USER_AGENT);
            return downloadVideo(new URL(url).openConnection());
        }
        catch(Exception e) {
            return null;
        }
    }

    /**
     * Download an image from the given URL
     *
     * @param url URL to image
     * @return Downloaded image or null
     */
    @Nullable
    public static BufferedImage downloadImage(String url) {
        try {
            URLConnection connection = new URL(url).openConnection();
            connection.setRequestProperty("User-Agent", USER_AGENT);
            return downloadImage(connection);
        }
        catch(Exception e) {
            return null;
        }
    }

    /**
     * Encode the given URL
     *
     * @param url URL to encode
     * @return Encoded URL
     */
    public static String urlEncode(String url) {
        try {
            return URLEncoder.encode(url, "UTF-8");
        }
        catch(UnsupportedEncodingException e) {
            return url;
        }
    }
}
