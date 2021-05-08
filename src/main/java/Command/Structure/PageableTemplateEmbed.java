package Command.Structure;

import net.dv8tion.jda.api.EmbedBuilder;

import java.util.List;

/**
 * Pageable embed from a given template - table, list, etc
 */
public abstract class PageableTemplateEmbed extends PageableSortEmbed {
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
    public PageableTemplateEmbed(CommandContext context, List<?> items, String thumb, String title, String desc, String footer, int bound, int... colour) {
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
        EmbedBuilder builder = new EmbedBuilder()
                .setTitle(title)
                .setThumbnail(thumb)
                .setColor(colour)
                .setImage(EmbedHelper.SPACER_IMAGE)
                .setFooter(pageDetails + " | " + footer);
        if(desc != null) {
            builder.setDescription(desc);
        }
        return builder;
    }
}
