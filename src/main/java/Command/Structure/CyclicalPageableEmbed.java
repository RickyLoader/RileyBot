package Command.Structure;

import net.dv8tion.jda.api.interactions.button.Button;

import java.util.ArrayList;
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

    @Override
    public ArrayList<Button> getButtonList() {
        ArrayList<Button> buttons = new ArrayList<>();
        if(getPages() > 1) {
            buttons.add(getBackwardButton());
            buttons.add(getForwardButton());
        }
        return buttons;
    }
}
