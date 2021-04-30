package Command.Commands;

import Command.Structure.*;
import TrademeAPI.Category;
import TrademeAPI.Listing;
import TrademeAPI.Trademe;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;

import java.util.ArrayList;
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
        super(
                "trademe",
                "Embed Trademe listings!",
                "trademe [id/query] : [optional category name]\ntrademe categories\n[trademe url]"
        );
        this.trademe = new Trademe();
        this.footer = "Type: " + getTrigger() + " for help";
    }

    @Override
    public void execute(CommandContext context) {
        Message message = context.getMessage();
        MessageChannel channel = context.getMessageChannel();
        Member member = context.getMember();

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
        if(query.equals("categories")) {
            showCategories(context, trademe.getCategories(), null);
            return;
        }

        long id = toLong(query);

        // Search by listing name
        if(id == 0) {
            Category category = null;
            if(query.contains(":")) {
                String[] args = query.split(":");
                if(args.length == 0 || args[0].isEmpty()) {
                    channel.sendMessage(
                            member.getAsMention() + " You didn't provide a search query bro\n" + getHelpNameCoded()
                    ).queue();
                    return;
                }
                if(args.length == 1 || args[1].isEmpty()) {
                    channel.sendMessage(
                            member.getAsMention() + " You didn't provide a category bro\n" + getHelpNameCoded()
                    ).queue();
                    return;
                }
                query = args[0].trim();
                String categoryName = args[1].trim();
                category = trademe.getCategoryByName(categoryName);
                if(category == null && !categoryName.equalsIgnoreCase("all")) {
                    ArrayList<Category> similarCategories = trademe.getCategoriesByName(categoryName);
                    if(similarCategories.isEmpty()) {
                        channel.sendMessage(
                                member.getAsMention()
                                        + " I didn't find any categories named **" + categoryName + "**"
                        ).queue();
                        return;
                    }
                    if(similarCategories.size() > 1) {
                        showCategories(context, trademe.getCategoriesByName(categoryName), categoryName);
                        return;
                    }
                    category = similarCategories.get(0);
                }
            }
            channel.sendTyping().queue();
            Listing.ListingOverview[] results = trademe.getListingOverviewsByTitle(query, category);
            if(results.length == 1) {
                Listing listing = trademe.getListingById(results[0].getId());
                showListing(context, listing);
                return;
            }
            showListingResults(context, results, query, category);
        }

        // Search by listing ID
        else {
            channel.sendTyping().queue();
            Listing listing = trademe.getListingById(id);
            if(listing == null) {
                channel.sendMessage(
                        member.getAsMention()
                                + " Are you sure that's a valid listing id?\n"
                                + getHelpNameCoded()
                ).queue();
                return;
            }
            showListing(context, listing);
        }
    }

    /**
     * Display the given Trademe listing categories in a pageable message embed
     *
     * @param context    Command context
     * @param categories Categories to display
     * @param query      Search query used to find the categories (May be null)
     */
    private void showCategories(CommandContext context, ArrayList<Category> categories, String query) {
        boolean searching = query != null;
        new PageableListEmbed(
                context,
                categories,
                Trademe.TRADEME_LOGO,
                searching ? "Trademe Category Search" : "Trademe Categories",
                query == null
                        ? "There are **" + categories.size() + "** categories:"
                        : "No categories named: **" + query + "**\n\n"
                        + "Here are " + categories.size() + " similar results:",
                footer,
                5,
                searching ? EmbedHelper.RED : EmbedHelper.YELLOW
        ) {
            @Override
            public void sortItems(List<?> items, boolean defaultSort) {
                items.sort((Comparator<Object>) (o1, o2) -> {
                    String n1 = ((Category) o1).getName();
                    String n2 = ((Category) o2).getName();
                    if(searching) {
                        if(defaultSort) {
                            return levenshteinDistance(n1, query) - levenshteinDistance(n2, query);
                        }
                        return levenshteinDistance(n2, query) - levenshteinDistance(n1, query);
                    }
                    else {
                        return defaultSort ? n1.compareTo(n2) : n2.compareTo(n1);
                    }
                });
            }

            @Override
            public String getName(int currentIndex) {
                return EmbedHelper.BLANK_CHAR;
            }

            @Override
            public String getValue(int currentIndex) {
                return ((Category) getItems().get(currentIndex)).getName();
            }
        }.showMessage();
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
     * @param context  Command context
     * @param results  Listing overviews found for the given query
     * @param query    Query used to find listings
     * @param category Category which was searched for listings
     */
    private void showListingResults(CommandContext context, Listing.ListingOverview[] results, String query, Category category) {
        boolean noResults = results.length == 0;
        new PageableTableEmbed(
                context,
                Arrays.asList(results),
                Trademe.TRADEME_LOGO,
                "Trademe Search",
                (noResults ? "No" : results.length)
                        + " results found for: **" + query
                        + "** in category: **" + (category == null ? "All" : category.getName()) + "**",
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
                    return levenshteinDistance(t2, query) - levenshteinDistance(t1, query);
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
