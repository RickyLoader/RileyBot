package Command.Commands;

import Command.Structure.*;
import TrademeAPI.Listing;
import TrademeAPI.Trademe;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * Take Trademe listing URLs and replace with an embed detailing the listing
 */
public class TrademeCommand extends DiscordCommand {
    private final Trademe trademe;
    private final String footer;

    public TrademeCommand() {
        super("trademe", "Embed Trademe listings!", "trademe [id/query]\n[trademe url]");
        this.trademe = new Trademe();
        this.footer = "Type: " + getTrigger() + " for help";
    }

    @Override
    public void execute(CommandContext context) {
        Message message = context.getMessage();
        MessageChannel channel = context.getMessageChannel();

        if(Trademe.isTrademeUrl(context.getMessageContent())) {
            Listing listing = trademe.getListingByUrl(context.getMessageContent());
            if(listing == null) {
                return;
            }
            message.delete().queue(deleted -> showListing(context, listing));
            return;
        }

        String query = context.getLowerCaseMessage().replaceFirst(getTrigger(), "").trim();
        if(query.isEmpty()) {
            channel.sendMessage(getHelpNameCoded()).queue();
            return;
        }
        channel.sendTyping().queue();
        long id = toLong(query);
        if(id == 0) {
            Listing.ListingOverview[] results = trademe.getListingOverviewsByTitle(query);
            if(results.length == 1) {
                Listing listing = trademe.getListingById(results[0].getId());
                showListing(context, listing);
                return;
            }
            showListingResults(context, results, query);
        }
        else {
            Listing listing = trademe.getListingById(id);
            if(listing == null) {
                channel.sendMessage(
                        context.getMember().getAsMention()
                                + " Are you sure that's a valid listing id?\n"
                                + getHelpNameCoded()
                ).queue();
                return;
            }
            showListing(context, listing);
        }
    }

    /**
     * Display the given listing in a message embed with reactions used to cycle through images
     *
     * @param context Command context
     * @param listing Listing to display
     */
    private void showListing(CommandContext context, Listing listing) {
        new PageableTrademeListing(context, listing, footer).showMessage();
    }

    /**
     * Display the given listing overview results in a pageable message embed
     *
     * @param context Command context
     * @param results Listing overviews found for the given query
     * @param query   Query used to find listings
     */
    private void showListingResults(CommandContext context, Listing.ListingOverview[] results, String query) {
        boolean noResults = results.length == 0;
        new PageableTableEmbed(
                context,
                Arrays.asList(results),
                Trademe.TRADEME_LOGO,
                "Trademe Search",
                (noResults ? "No" : results.length) + " results found for: **" + query + "**",
                "Type: " + getTrigger() + " for help",
                new String[]{"ID", "Title", "Price"},
                5
        ) {
            @Override
            public String[] getRowValues(int index, List<?> items, boolean defaultSort) {
                Listing.ListingOverview overview = (Listing.ListingOverview) items.get(index);
                return new String[]{
                        String.valueOf(overview.getId()),
                        EmbedHelper.embedURL(overview.getTitle(), overview.getUrl()),
                        overview.getPriceDisplay()
                };
            }

            @Override
            public void sortItems(List<?> items, boolean defaultSort) {
                items.sort((Comparator<Object>) (o1, o2) -> {
                    String t1 = ((Listing.ListingOverview) o1).getTitle();
                    String t2 = ((Listing.ListingOverview) o2).getTitle();
                    if(defaultSort) {
                        return levenshteinDistance(t1, query) - levenshteinDistance(t2, query);
                    }
                    return levenshteinDistance(t1, query) - levenshteinDistance(t2, query);
                });
            }

            @Override
            public EmbedBuilder getEmbedBuilder(String pageDetails) {
                EmbedBuilder builder = super.getEmbedBuilder(pageDetails);
                if(noResults) {
                    builder.setImage(Trademe.NO_SEARCH_RESULTS_IMAGE);
                }
                return builder;
            }
        }.showMessage();
    }

    @Override
    public boolean matches(String query, Message message) {
        return query.startsWith(getTrigger()) || Trademe.isTrademeUrl(message.getContentDisplay());
    }
}
