package Command.Structure;

import TrademeAPI.Listing;
import TrademeAPI.Member;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Emote;
import net.dv8tion.jda.api.entities.MessageEmbed;

import javax.annotation.Nullable;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;

import static TrademeAPI.Trademe.*;

/**
 * Page through images in a Trademe listing
 */
public class PageableTrademeListing extends PageableEmbed {
    private final Listing listing;
    private final String footer;

    /**
     * Initialise the values
     *
     * @param context Command context
     * @param listing Trademe listing to display
     * @param footer  Footer to display in message
     */
    public PageableTrademeListing(CommandContext context, Listing listing, String footer) {
        super(context, listing.getImages(), 1);
        this.listing = listing;
        this.footer = footer;
    }

    @Override
    public EmbedBuilder getEmbedBuilder(String pageDetails) {
        EmoteHelper emoteHelper = getEmoteHelper();
        DecimalFormat df = new DecimalFormat("#,###");
        boolean closed = listing.isClosed();
        MessageEmbed.AuthorInfo author = getAuthor(listing.getMember());
        String emoteGap = EmoteHelper.formatEmote(emoteHelper.getBlankGap());
        Listing.ListingOverview overview = listing.getOverview();

        EmbedBuilder builder = new EmbedBuilder()
                .setColor(closed ? EmbedHelper.RED : EmbedHelper.ORANGE)
                .setAuthor(author.getName(), author.getUrl(), author.getIconUrl())
                .setDescription(
                        listing.getTruncatedDescription()
                                + "\n\n" + EmoteHelper.formatEmote(emoteHelper.getBiddersWatchers()) + emoteGap
                                + df.format(listing.getBidderWatchers())
                                + "\n" + EmoteHelper.formatEmote(emoteHelper.getPrice()) + emoteGap
                                + overview.getPriceDisplay()
                )
                .setThumbnail(listing.getThumbnail())
                .setFooter(
                        (closed ? "Closed" : "Closing")
                                + ": "
                                + new SimpleDateFormat("dd/MM/yyy").format(listing.getClosingDate())
                                + " | " + pageDetails
                                + " | " + footer,
                        TRADEME_LOGO
                )
                .setTitle(overview.getTitle(), overview.getUrl());

        if(!listing.hasImages()) {
            builder.setImage(Listing.NO_PHOTOS_IMAGE);
        }
        return builder;
    }

    /**
     * Get the author to use for the listing embed.
     * Either the seller (if available) or a default Trademe author.
     *
     * @param member Listing member
     * @return Author to use in listing embed
     */
    private MessageEmbed.AuthorInfo getAuthor(@Nullable Member member) {
        String name = "Trademe";
        if(member == null) {
            return new MessageEmbed.AuthorInfo(
                    name,
                    BASE_URL,
                    TRADEME_LOGO,
                    null
            );
        }
        return new MessageEmbed.AuthorInfo(
                name + " - " + member.getName(),
                member.getUrl(),
                member.getPhoto(),
                null
        );
    }

    @Override
    public void displayItem(EmbedBuilder builder, int currentIndex) {
        builder.setImage((String) getItems().get(currentIndex));
    }

    @Override
    public void pageForward() {
        int index = getIndex() + 1;
        if(index == getItems().size()) {
            index = 0;
        }
        setIndex(index);
    }

    @Override
    public void pageBackward() {
        int index = getIndex() - 1;
        if(index == -1) {
            index = getItems().size() - 1;
        }
        setIndex(index);
    }

    @Override
    public String getPageDetails() {
        return listing.hasImages() ? "Photo: " + getPage() + "/" + getPages() : "No images for listing";
    }

    @Override
    public boolean nonPagingEmoteAdded(Emote e) {
        return false;
    }
}
