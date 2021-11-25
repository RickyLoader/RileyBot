package Command.Structure;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.requests.restaction.MessageAction;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Pageable embed from a given template - table, list, etc
 */
public abstract class PageableTemplateEmbed<T> extends PageableSortEmbed<T> {
    private static final String IMAGE_FILENAME = "image.png";
    private final String thumb, title, titleUrl, desc, footer;
    private final byte[] imageFile;
    private final int colour;

    /**
     * Embedded message that can be paged through with buttons
     * Takes an image file to use as the embed image.
     *
     * @param context   Command context
     * @param items     List of items to be displayed
     * @param thumb     Thumbnail to use for embed
     * @param imageFile Image file to use as embed image
     * @param title     Title to use for embed
     * @param titleUrl  Optional URL to use in embed title
     * @param desc      Description to use for embed
     * @param footer    Footer to use in the embed
     * @param bound     Maximum items to display
     * @param colour    Optional colour to use for embed
     */
    public PageableTemplateEmbed(CommandContext context, List<T> items, String thumb, byte[] imageFile, String title, @Nullable String titleUrl, String desc, String footer, int bound, int... colour) {
        super(context, items, bound);
        this.thumb = thumb;
        this.title = title;
        this.titleUrl = titleUrl;
        this.desc = desc;
        this.footer = footer;
        this.colour = colour.length == 0 ? EmbedHelper.YELLOW : colour[0];
        this.imageFile = imageFile;
    }

    /**
     * Embedded message that can be paged through with buttons.
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
        this(context, items, thumb, null, title, null, desc, footer, bound, colour);
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

    /**
     * Get the colour to use in the embed
     *
     * @return Embed colour
     */
    public int getColour() {
        return colour;
    }

    /**
     * Get the description to use in the embed
     *
     * @return Embed description
     */
    public String getDescription() {
        return desc;
    }

    @Override
    public EmbedBuilder getEmbedBuilder(String pageDetails) {
        return getDefaultEmbedBuilder(pageDetails).setColor(colour).setDescription(desc);
    }

    @Override
    protected MessageAction getMessageAction() {
        MessageAction sendPageableMessage = super.getMessageAction();
        return imageFile == null ? sendPageableMessage : sendPageableMessage.addFile(imageFile, IMAGE_FILENAME);
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
                .setTitle(title, titleUrl)
                .setThumbnail(thumb)
                .setImage(imageFile == null ? EmbedHelper.SPACER_IMAGE : "attachment://" + IMAGE_FILENAME);

        boolean hasPageDetails = pageDetails.length > 0;
        String footer = hasPageDetails ? pageDetails[0] : "";

        if(getFooter() != null) {
            if(hasPageDetails) {
                footer += " | ";
            }
            footer += getFooter();
        }
        if(!footer.isEmpty()) {
            builder.setFooter(footer);
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
