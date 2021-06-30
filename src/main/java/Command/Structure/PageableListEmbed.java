package Command.Structure;

import net.dv8tion.jda.api.EmbedBuilder;

import java.util.List;

public abstract class PageableListEmbed<T> extends PageableTemplateEmbed<T> {

    /**
     * Embedded message that can be paged through with emotes and displays as a list of fields
     *
     * @param context Command context
     * @param items   List of items to be displayed
     * @param thumb   Thumbnail to use for embed
     * @param title   Title to use for embed
     * @param desc    Description to use for embed
     * @param footer  Footer to use in the embed
     * @param bound   Maximum items to display
     * @param colour  Optional colour to use for embed
     */
    public PageableListEmbed(CommandContext context, List<T> items, String thumb, String title, String desc, String footer, int bound, int... colour) {
        super(context, items, thumb, title, desc, footer, bound, colour);
    }

    @Override
    public void displayItem(EmbedBuilder builder, int currentIndex, T item) {
        final String title = getName(item);
        try {
            builder.addField(title, getValue(item), false);
        }
        catch(IllegalArgumentException e) {
            builder.addField(getName(item), "Failed to display field value!", false);
        }
    }

    /**
     * Get the name to use in a field for the item at the current index
     *
     * @param item Item at the current index
     * @return Name to use in field
     */
    public abstract String getName(T item);

    /**
     * Get the value to use in a field for the item at the current index
     *
     * @param item Item at the current index
     * @return Name to use in field
     */
    public abstract String getValue(T item);
}
