package Command.Structure;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.util.List;

/**
 * Pageable embed from a given template - table, list, etc
 */
public abstract class PageableTemplateEmbed<T> extends PageableSortEmbed<T> {
    private final String thumb, title, desc, footer;
    private final int colour;

    /**
     * Embedded message that can be paged through with emotes
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
    public PageableTemplateEmbed(CommandContext context, List<T> items, String thumb, String title, String desc, String footer, int bound, int... colour) {
        super(context, items, bound);
        this.thumb = thumb;
        this.title = title;
        this.desc = desc;
        this.footer = footer;
        this.colour = colour.length == 0 ? EmbedHelper.YELLOW : colour[0];
    }

    /**
     * Get the thumbnail image URL
     *
     * @return Thumbnail image URL
     */
    public String getThumb() {
        return thumb;
    }

    /**
     * Get the footer text
     *
     * @return Footer text
     */
    public String getFooter() {
        return footer;
    }

    @Override
    public EmbedBuilder getEmbedBuilder(String pageDetails) {
        EmbedBuilder builder = getDefaultEmbedBuilder(pageDetails).setColor(colour);
        if(desc != null) {
            builder.setDescription(desc);
        }
        return builder;
    }

    /**
     * Get the default embed builder to use initialised with all values
     * except for the description and colour.
     *
     * @param pageDetails Optional String detailing the current page - e.g "Page: 1/5"
     * @return Default embed builder
     */
    private EmbedBuilder getDefaultEmbedBuilder(String... pageDetails) {
        EmbedBuilder builder = new EmbedBuilder()
                .setTitle(title)
                .setThumbnail(thumb)
                .setImage(EmbedHelper.SPACER_IMAGE);

        if(pageDetails.length > 0) {
            builder.setDescription(pageDetails[0]);
        }
        return builder;
    }

    @Override
    public MessageEmbed getNoItemsEmbed() {
        return getDefaultEmbedBuilder().setDescription(getNoItemsDescription()).setColor(getNoItemsColour()).build();
    }

    /**
     * Get the description to use for the embed when there are no items to display
     *
     * @return No items embed description
     */
    public abstract String getNoItemsDescription();

    /**
     * Get the colour to use for the embed when there are no items to display
     *
     * @return Red colour
     */
    public int getNoItemsColour() {
        return EmbedHelper.RED;
    }
}
