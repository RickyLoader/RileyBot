package Command.Structure;

import net.dv8tion.jda.api.entities.Emote;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.util.concurrent.TimeUnit;

/**
 * Commonly used features of an embed
 */
public class EmbedHelper {

    public static int getBlue() {
        return 1942002;
    }

    public static int getGreen() {
        return 65280;
    }

    public static int getPurple() {
        return 16711935;
    }

    public static int getYellow() {
        return 16776960;
    }

    public static int getRed() {
        return 16711680;
    }

    public static int getOrange() {
        return 0xd89620;
    }

    /**
     * Create a field with an invisible character for a title, and the value as a value.
     * Allows a column-esq appearance without having a title above every value
     *
     * @param value Value to use
     * @return Value field
     */
    public static MessageEmbed.Field getValueField(String value) {
        return new MessageEmbed.Field(getBlankChar(), value, true);
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
     * Invisible character allows no value to be given to fields
     *
     * @return Invisible character
     */
    public static String getBlankChar() {
        return "\u200e";
    }

    /**
     * Get URL to a transparent image used to keep embeds at a constant width
     *
     * @return Transparent image url
     */
    public static String getSpacerImage() {
        return "https://i.imgur.com/24Xf03H.png";
    }

    /**
     * Get the String form of an emote to display inside an embed
     *
     * @param e Emote to convert
     * @return String version of emote
     */
    public static String formatEmote(Emote e) {
        return "<:" + e.getName() + ":" + e.getId() + "> ";
    }

    /**
     * Format the given time in ms to HH:MM:SS
     *
     * @param time Time in ms
     * @return Formatted time String
     */
    public static String formatTime(long time) {
        return String.format("%02d:%02d:%02d",
                TimeUnit.MILLISECONDS.toHours(time),
                TimeUnit.MILLISECONDS.toMinutes(time) -
                        TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(time)),
                TimeUnit.MILLISECONDS.toSeconds(time) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(time)));
    }
}
