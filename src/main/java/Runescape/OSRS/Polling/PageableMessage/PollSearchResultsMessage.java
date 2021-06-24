package Runescape.OSRS.Polling.PageableMessage;

import Command.Structure.CommandContext;
import Command.Structure.EmbedHelper;
import Command.Structure.PageableTableEmbed;
import Runescape.OSRS.Polling.Poll;

import java.util.ArrayList;
import java.util.List;

/**
 * Pageable OSRS poll message showing search results
 */
public class PollSearchResultsMessage extends PageableTableEmbed<Poll> {

    /**
     * Initialise the message values
     *
     * @param results Polls containing search query
     * @param query   Search query used to find results
     * @param footer  Footer to use in the embed
     * @param context Command context
     */
    public PollSearchResultsMessage(ArrayList<Poll> results, String query, String footer, CommandContext context) {
        super(
                context,
                results,
                EmbedHelper.OSRS_LOGO,
                "OSRS Poll Search",
                results.size() + " "
                        + (results.size() == 1 ? "result" : "results")
                        + " found for: **" + query + "**",
                footer,
                new String[]{
                        "#",
                        "Title",
                        "Date"
                },
                5,
                EmbedHelper.GREEN
        );
    }

    @Override
    public void sortItems(List<Poll> items, boolean defaultSort) {
        items.sort((p1, p2) -> defaultSort ? p1.getNumber() - p2.getNumber() : p2.getNumber() - p1.getNumber());
    }

    @Override
    public String[] getRowValues(int index, Poll poll, boolean defaultSort) {
        return new String[]{
                String.valueOf(poll.getNumber()),
                EmbedHelper.embedURL(poll.getTitle(), poll.getUrl()),
                poll.getStartDateFormatted()
        };
    }

    @Override
    public String getNoItemsDescription() {
        return "No results!";
    }
}
