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
import java.util.Comparator;
import java.util.List;

import static TrademeAPI.Listing.*;

/**
 * Take Trademe listing URLs and replace with an embed detailing the listing
 */
public class TrademeCommand extends DiscordCommand {
    private final Trademe trademe;
    private final String footer, listingIdPrefix = "tm";

    public TrademeCommand() {
        super(
                "trademe",
                "Embed Trademe listings!",
                "trademe [id/query] : [optional category name/id]\ntrademe categories\n[trademe url]"
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

        Category category = trademe.getRootCategory();

        if(query.matches(listingIdPrefix + "\\d+")) {
            channel.sendTyping().queue();
            Listing listing = trademe.getListingById(
                    toLong(
                            query.replaceFirst(listingIdPrefix, "")
                    )
            );
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
        else {
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
                String categoryString = args[1].trim();
                if(Trademe.isCategoryId(categoryString)) {
                    category = trademe.getCategoryByNumber(categoryString);
                    if(category == null) {
                        channel.sendMessage(
                                member.getAsMention() + " You sure that's a category ID bro?"
                        ).queue();
                        return;
                    }
                }
                else {
                    ArrayList<Category> categories = trademe.getCategoriesByName(categoryString);
                    if(categories.isEmpty()) {
                        channel.sendMessage(
                                member.getAsMention()
                                        + " I didn't find any categories named **" + categoryString + "**"
                        ).queue();
                        return;
                    }

                    if(categories.size() > 1) {
                        showCategories(context, categories, categoryString);
                        return;
                    }
                    category = categories.get(0);
                }
            }

            channel.sendTyping().queue();
            ArrayList<ListingOverview> results = trademe.searchListingsByTitle(query, category);
            if(results.size() == 1) {
                Listing listing = trademe.getListingById(results.get(0).getId());
                showListing(context, listing);
                return;
            }
            showListingResults(context, results, query, category);
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
        new PageableTableEmbed(
                context,
                categories,
                Trademe.TRADEME_LOGO,
                searching ? "Trademe Category Search" : "Trademe Categories",
                query == null
                        ? "There are **" + categories.size() + "** categories:"
                        : categories.size()
                        + " categories found for: **" + query + "**"
                        + "\n\n Use the **ID** to search in a specific category",
                footer,
                new String[]{"ID", "Path", "Name"},
                5,
                searching ? EmbedHelper.RED : EmbedHelper.YELLOW
        ) {
            @Override
            public String[] getRowValues(int index, List<?> items, boolean defaultSort) {
                Category category = (Category) items.get(index);
                return new String[]{category.getNumber(), category.getPath(), category.getName()};
            }

            @Override
            public void sortItems(List<?> items, boolean defaultSort) {
                if(searching) {
                    items.sort(new LevenshteinDistance(query, defaultSort) {
                        @Override
                        public String getString(Object o) {
                            return ((Category) o).getName();
                        }
                    });
                    return;
                }
                items.sort((Comparator<Object>) (o1, o2) -> {
                    String n1 = ((Category) o1).getPath();
                    String n2 = ((Category) o2).getPath();
                    return defaultSort ? n1.compareTo(n2) : n2.compareTo(n1);
                });
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
    private void showListingResults(CommandContext context, ArrayList<ListingOverview> results, String query, Category category) {
        boolean noResults = results.isEmpty();
        new PageableTableEmbed(
                context,
                results,
                Trademe.TRADEME_LOGO,
                "Trademe Search",
                (noResults ? "No" : results.size())
                        + " results found for: **" + query + "**"
                        + " in category: **"
                        + (category == trademe.getRootCategory() ? category.getName() : category.getPath()) + "**",
                "Type: " + getTrigger() + " for help",
                new String[]{"ID", "Title", "Price"},
                5
        ) {
            @Override
            public String[] getRowValues(int index, List<?> items, boolean defaultSort) {
                ListingOverview overview = (ListingOverview) items.get(index);
                return new String[]{
                        listingIdPrefix + overview.getId(),
                        EmbedHelper.embedURL(overview.getTitle(), overview.getUrl()),
                        overview.getPriceDisplay()
                };
            }

            @Override
            public void sortItems(List<?> items, boolean defaultSort) {
                items.sort(new LevenshteinDistance(query, defaultSort) {
                    @Override
                    public String getString(Object o) {
                        return ((ListingOverview) o).getTitle();
                    }
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
