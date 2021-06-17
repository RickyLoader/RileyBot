package Command.Structure;

import net.dv8tion.jda.api.entities.Emoji;
import net.dv8tion.jda.api.interactions.button.Button;

import java.util.ArrayList;
import java.util.List;

/**
 * Sortable pageable embed
 */
public abstract class PageableSortEmbed<T> extends PageableEmbed<T> {
    private final Button reverse;
    private boolean defaultSort = true;

    /**
     * Initialise the values
     *
     * @param context Command context
     * @param items   List of items to be displayed
     * @param bound   Maximum items to display
     */
    public PageableSortEmbed(CommandContext context, List<T> items, int bound) {
        super(context, items, bound);
        this.reverse = Button.primary("reverse", Emoji.ofEmote(getEmoteHelper().getReverse()));
        sortItems(items, defaultSort);
    }

    @Override
    public ArrayList<Button> getButtonList() {
        ArrayList<Button> buttons = super.getButtonList();
        if(getItems().size() > 1) {
            buttons.add(reverse);
        }
        return buttons;
    }

    @Override
    public boolean nonPagingButtonPressed(String buttonId) {
        if(!buttonId.equals(reverse.getId())) {
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
     * Get the reverse sort button
     *
     * @return Reverse sort button
     */
    public Button getReverseButton() {
        return reverse;
    }

    /**
     * Sort the items based on the value of defaultSort
     *
     * @param items       List of objects to sort
     * @param defaultSort How to sort the items
     */
    public abstract void sortItems(List<T> items, boolean defaultSort);
}
