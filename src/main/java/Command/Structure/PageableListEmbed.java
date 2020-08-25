package Command.Structure;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageChannel;

import java.util.List;

public abstract class PageableListEmbed extends PageableEmbed {

    /**
     * Embedded message that can be paged through with emotes and displays as a list of fields
     *
     * @param channel Channel to send embed to
     * @param guild   Guild to find emotes
     * @param items   List of items to be displayed
     * @param thumb   Thumbnail to use for embed
     * @param title   Title to use for embed
     * @param desc    Description to use for embed
     * @param colour  Optional colour to use for embed
     */
    public PageableListEmbed(MessageChannel channel, Guild guild, List<?> items, String thumb, String title, String desc, int... colour) {
        super(channel, guild, items, thumb, title, desc, colour);
    }

    /**
     * Add a field for the current item
     *
     * @param builder      Embed builder to add fields to
     * @param currentIndex Current index within list of items
     */
    @Override
    public void addFields(EmbedBuilder builder, int currentIndex) {
        builder.addField(getName(currentIndex, getItems()), getValue(currentIndex, getItems()), false);
    }

    /**
     * Get the name to use in a field for the item at the current index
     *
     * @param currentIndex Current index within list of items
     * @param items        List of items
     * @return Name to use in field
     */
    public abstract String getName(int currentIndex, List<?> items);

    /**
     * Get the value to use in a field for the item at the current index
     *
     * @param currentIndex Current index within list of items
     * @param items        List of items
     * @return Name to use in field
     */
    public abstract String getValue(int currentIndex, List<?> items);
}
