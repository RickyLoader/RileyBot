package Command.Structure;

import Valheim.ValheimPageSummary;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Pageable Valheim wiki search embed
 */
public class PageableValheimWikiSearchEmbed extends PageableTableEmbed {
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
    public void sortItems(List<?> items, boolean defaultSort) {
        items.sort((Comparator<Object>) (o1, o2) -> {
            String n1 = ((ValheimPageSummary) o1).getTitle();
            String n2 = ((ValheimPageSummary) o2).getTitle();
            if(defaultSort) {
                return n1.compareTo(n2);
            }
            return n2.compareTo(n1);
        });
    }

    @Override
    public String[] getRowValues(int index, List<?> items, boolean defaultSort) {
        ValheimPageSummary pageSummary = (ValheimPageSummary) items.get(index);
        return new String[]{
                EmbedHelper.embedURL(pageSummary.getTitle(), pageSummary.getUrl()),
                pageSummary.getCategory().name()
        };
    }
}
