package Command.Structure;

import Valheim.Wiki.ValheimPageSummary;

import java.util.ArrayList;
import java.util.List;

/**
 * Pageable Valheim wiki search embed
 */
public class PageableValheimWikiSearchEmbed extends PageableTableEmbed<ValheimPageSummary> {
    public static final String THUMBNAIL = "https://goldencheckpoint.com/wp-content/uploads/2021/02/Valheim_logo-800x400.png";

    /**
     * Display Valheim wiki search results as a pageable embed
     *
     * @param context       Command context
     * @param query         Search query used to find page summaries
     * @param pageSummaries List of Valheim page summaries found for the given query
     * @param footer        Footer to use in the embed
     */
    public PageableValheimWikiSearchEmbed(CommandContext context, ArrayList<ValheimPageSummary> pageSummaries, String query, String footer) {
        super(
                context,
                pageSummaries,
                THUMBNAIL,
                "Valheim Wiki Search",
                getDescription(query, pageSummaries),
                footer,
                new String[]{"Title", "Category"},
                5,
                pageSummaries.isEmpty() ? EmbedHelper.RED : EmbedHelper.FIRE_ORANGE
        );
    }

    /**
     * Get the description to use in the search results embed based on how many results were found
     *
     * @param query         Search query used to find page summaries
     * @param pageSummaries List of Valheim page summaries found for the given query
     * @return Embed description
     */
    private static String getDescription(String query, ArrayList<ValheimPageSummary> pageSummaries) {
        if(pageSummaries.isEmpty()) {
            return "No results found for: **" + query + "**" +
                    "\n\nI am looking for either an exact match or a partial match in the **page name**, try being less specific or learn how to spell.";
        }
        return pageSummaries.size() + " results found for: **" + query + "**";
    }

    @Override
    public void sortItems(List<ValheimPageSummary> items, boolean defaultSort) {
        items.sort((o1, o2) -> defaultSort
                ? o1.getTitle().compareTo(o2.getTitle())
                : o2.getTitle().compareTo(o1.getTitle()));
    }

    @Override
    public String[] getRowValues(int index, ValheimPageSummary pageSummary, boolean defaultSort) {
        return new String[]{
                EmbedHelper.embedURL(pageSummary.getTitle(), pageSummary.getUrl()),
                pageSummary.getCategory().name()
        };
    }

    @Override
    public String getNoItemsDescription() {
        return "Nothing to see here!";
    }
}
