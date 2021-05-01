package Command.Structure;

import java.util.List;

/**
 * Pageable message embed which loops around back to the first page after the last page
 */
public abstract class CyclicalPageableEmbed extends PageableEmbed {

    /**
     * Initialise the values
     *
     * @param context Command context
     * @param items   List of items to be displayed
     * @param bound   Maximum items to display on one page
     */
    public CyclicalPageableEmbed(CommandContext context, List<?> items, int bound) {
        super(context, items, bound);
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
}
