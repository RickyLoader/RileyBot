package Runescape.OSRS.Polling.PageableMessage;

import Command.Structure.CommandContext;
import Command.Structure.EmbedHelper;
import Command.Structure.PageableTableEmbed;
import Runescape.OSRS.Polling.Poll;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Pageable OSRS poll message showing search results
 */
public class PollSearchResultsMessage extends PageableTableEmbed {

    /**
     * Initialise the message values
     *
     * @param results Polls containing search query
     * @param query   Search query used to find results
     * @param context Command context
     */
    public PollSearchResultsMessage(ArrayList<Poll> results, String query, CommandContext context) {
        super(
                context,
                results,
                EmbedHelper.OSRS_LOGO,
                "OSRS Poll Search",
                results.size() + " "
                        + (results.size() == 1 ? "result" : "results")
                        + " found for: **" + query + "**",
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
    public void sortItems(List<?> items, boolean defaultSort) {
        items.sort((Comparator<Object>) (o1, o2) -> {
            Poll p1 = (Poll) o1;
            Poll p2 = (Poll) o2;
            if(defaultSort) {
                return p1.getNumber() - p2.getNumber();
            }
            return p2.getNumber() - p1.getNumber();
        });
    }

    @Override
    public String[] getRowValues(int index, List<?> items, boolean defaultSort) {
        Poll poll = (Poll) items.get(index);
        return new String[]{
                String.valueOf(poll.getNumber()),
                EmbedHelper.embedURL(poll.getTitle(), poll.getUrl()),
                poll.getStartDateFormatted()
        };
    }
}
