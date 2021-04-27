package Command.Structure;

import Command.Commands.TrademeCommand;
import Trademe.Listing;
import Trademe.Member;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Emote;
import net.dv8tion.jda.api.entities.MessageEmbed;

import javax.annotation.Nullable;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;

/**
 * Page through images in a Trademe listing
 */
public class PageableTrademeListing extends PageableEmbed {
    private final Listing listing;

    /**
     * Initialise the values
     *
     * @param context Command context
     * @param listing Trademe listing to display
     */
    public PageableTrademeListing(CommandContext context, Listing listing) {
        super(context, listing.getImages(), 1);
        this.listing = listing;
    }

    @Override
    public EmbedBuilder getEmbedBuilder(String pageDetails) {
        EmoteHelper emoteHelper = getEmoteHelper();
        DecimalFormat df = new DecimalFormat("#,###");
        boolean closed = listing.isClosed();
        MessageEmbed.AuthorInfo author = getAuthor(listing.getMember());
        String emoteGap = EmoteHelper.formatEmote(emoteHelper.getBlankGap());

        EmbedBuilder builder = new EmbedBuilder()
                .setColor(closed ? EmbedHelper.RED : EmbedHelper.ORANGE)
                .setAuthor(author.getName(), author.getUrl(), author.getIconUrl())
                .setDescription(
                        listing.getTruncatedDescription()
                                + "\n\n" + EmoteHelper.formatEmote(emoteHelper.getBiddersWatchers()) + emoteGap
                                + df.format(listing.getBidderWatchers())
                                + "\n" + EmoteHelper.formatEmote(emoteHelper.getPrice()) + emoteGap
                                + listing.getPriceDisplay()
                )
                .setThumbnail(listing.getThumbnail())
                .setFooter(
                        (closed ? "Closed" : "Closing")
                                + ": "
                                + new SimpleDateFormat("dd/MM/yyy HH:mm:ss").format(listing.getClosingDate())
                                + " | " + pageDetails,
                        TrademeCommand.TRADEME_LOGO
                )
                .setTitle(listing.getTitle(), listing.getUrl());

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
                    TrademeCommand.BASE_URL,
                    TrademeCommand.TRADEME_LOGO,
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
