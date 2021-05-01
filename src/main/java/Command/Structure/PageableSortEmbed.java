package Command.Structure;

import net.dv8tion.jda.api.entities.Emote;
import net.dv8tion.jda.api.entities.Message;

import java.util.List;

/**
 * Sortable pageable embed
 */
public abstract class PageableSortEmbed extends PageableEmbed {
    private final Emote reverse;
    private boolean defaultSort = true;

    /**
     * Initialise the values
     *
     * @param context Command context
     * @param items   List of items to be displayed
     * @param bound   Maximum items to display
     */
    public PageableSortEmbed(CommandContext context, List<?> items, int bound) {
        super(context, items, bound);
        this.reverse = context.getEmoteHelper().getReverse();
        sortItems(items, defaultSort);
    }

    @Override
    public void addReactions(Message message) {
        super.addReactions(message);
        if(getItems().size() > 1) {
            message.addReaction(reverse).queue();
        }
    }

    @Override
    public boolean nonPagingEmoteAdded(Emote e) {
        if(e != reverse || getItems().size() == 1) {
            return false;
        }
        flipSortDirection();
        return true;
    }

    /**
     * Get the current sort value
     *
     * @return Current sort value
     */
    public boolean isDefaultSort() {
        return defaultSort;
    }

    /**
     * Reverse the sort of the items and reset the index
     */
    private void flipSortDirection() {
        defaultSort = !defaultSort;
        sortItems(getItems(), defaultSort);
        setIndex(0);
    }

    /**
     * Get the reverse sort emote
     *
     * @return Reverse sort emote
     */
    public Emote getReverse() {
        return reverse;
    }

    /**
     * Sort the items based on the value of defaultSort
     *
     * @param items       List of objects to sort
     * @param defaultSort How to sort the items
     */
    public abstract void sortItems(List<?> items, boolean defaultSort);
}
