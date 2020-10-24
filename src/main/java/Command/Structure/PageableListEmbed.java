package Command.Structure;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.MessageChannel;

import java.util.List;

public abstract class PageableListEmbed extends PageableEmbed {

    /**
     * Embedded message that can be paged through with emotes and displays as a list of fields
     *
     * @param jda         JDA for listener
     * @param channel     Channel to send embed to
     * @param emoteHelper Emote helper
     * @param items       List of items to be displayed
     * @param thumb       Thumbnail to use for embed
     * @param title       Title to use for embed
     * @param desc        Description to use for embed
     * @param bound       Maximum items to display
     * @param colour      Optional colour to use for embed
     */
    public PageableListEmbed(JDA jda, MessageChannel channel, EmoteHelper emoteHelper, List<?> items, String thumb, String title, String desc, int bound, int... colour) {
        super(jda, channel, emoteHelper, items, thumb, title, desc, bound, colour);
    }

    /**
     * Add a field for the current item
     *
     * @param builder      Embed builder to add fields to
     * @param currentIndex Current index within list of items
     */
    @Override
    public void addFields(EmbedBuilder builder, int currentIndex) {
        builder.addField(getName(currentIndex), getValue(currentIndex), false);
    }

    /**
     * Get the name to use in a field for the item at the current index
     *
     * @param currentIndex Current index within list of items
     * @return Name to use in field
     */
    public abstract String getName(int currentIndex);

    /**
     * Get the value to use in a field for the item at the current index
     *
     * @param currentIndex Current index within list of items
     * @return Name to use in field
     */
    public abstract String getValue(int currentIndex);
}
