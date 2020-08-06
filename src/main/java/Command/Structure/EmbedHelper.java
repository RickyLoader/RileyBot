package Command.Structure;

import net.dv8tion.jda.api.entities.MessageEmbed;

/**
 * Commonly used features of an embed
 */
public class EmbedHelper {

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
}
