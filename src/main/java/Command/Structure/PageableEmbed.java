package Command.Structure;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.interactions.ActionRow;
import net.dv8tion.jda.api.interactions.button.Button;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Embedded message that can be paged through with buttons
 */
public abstract class PageableEmbed {
    private final MessageChannel channel;
    private final int bound, pages;
    private final Button forward, backward;
    private final EmoteHelper emoteHelper;
    private List<?> items;
    private long id;
    private int index = 0, page = 1;
    private String lastAction;

    /**
     * Initialise the values
     *
     * @param context Command context
     * @param items   List of items to be displayed
     * @param bound   Maximum items to display on one page
     */
    public PageableEmbed(CommandContext context, List<?> items, int bound) {
        this.channel = context.getMessageChannel();
        this.items = items;
        this.bound = bound;
        this.pages = items.size() <= bound ? 1 : (int) Math.ceil(items.size() / (double) bound);
        this.emoteHelper = context.getEmoteHelper();
        this.forward = Button.success("forward", Emoji.ofEmote(emoteHelper.getForward()));
        this.backward = Button.success("backward", Emoji.ofEmote(emoteHelper.getBackward()));

        context.getJDA().addEventListener(new ButtonListener() {
            @Override
            public void handleButtonClick(@NotNull ButtonClickEvent event) {
                if(event.getMessageIdLong() != id) {
                    return;
                }
                buttonPressed(event);
            }
        });
    }

    /**
     * Get the emote helper
     *
     * @return Emote helper
     */
    public EmoteHelper getEmoteHelper() {
        return emoteHelper;
    }

    /**
     * Get the list of items to display
     *
     * @return List of objects
     */
    public List<?> getItems() {
        return items;
    }

    /**
     * Get the current page value
     *
     * @return Page value
     */
    public int getPage() {
        return page;
    }

    /**
     * Get the total number of pages
     *
     * @return Total number of pages
     */
    public int getPages() {
        return pages;
    }

    /**
     * Get the current index
     *
     * @return Current index
     */
    public int getIndex() {
        return index;
    }

    /**
     * Get the bound - how many items to display per page
     *
     * @return Bound
     */
    public int getBound() {
        return bound;
    }

    /**
     * Send the embed with the paging buttons
     */
    public void showMessage() {
        channel.sendMessage(buildMessage()).setActionRows(getButtonRow()).queue(message -> id = message.getIdLong());
    }

    /**
     * Get the buttons to add to the message
     */
    public ArrayList<Button> getButtonList() {
        ArrayList<Button> buttons = new ArrayList<>();
        if(pages > 1) {
            buttons.add(isFirstPage() ? this.backward.asDisabled() : this.backward);
            buttons.add(isFinalPage() ? this.forward.asDisabled() : this.forward);
        }
        return buttons;
    }

    /**
     * Delete the embed
     */
    public void delete() {
        channel.retrieveMessageById(id).queue(message -> message.delete().queue());
    }

    /**
     * Get the embed builder to use
     *
     * @param pageDetails Current page details - e.g "Page: 1/5"
     * @return Embed builder
     */
    public abstract EmbedBuilder getEmbedBuilder(String pageDetails);

    /**
     * Display the item of the given index in the embed builder
     *
     * @param builder      Embed builder to display item in
     * @param currentIndex Current index within list of items
     */
    public abstract void displayItem(EmbedBuilder builder, int currentIndex);

    /**
     * Get the id of the embed
     *
     * @return ID of the embed
     */
    public long getId() {
        return id;
    }

    /**
     * Edit the embedded message in place by acknowledging the button click event.
     * This will keep the button displaying the "thinking" animation until the message has been edited.
     *
     * @param event Event to acknowledge
     */
    private void updateMessage(ButtonClickEvent event) {
        event.deferEdit().setEmbeds(buildMessage()).setActionRows(getButtonRow()).queue();
    }

    /**
     * Get the row of buttons to add to the message
     *
     * @return Row of buttons
     */
    private ActionRow getButtonRow() {
        return ActionRow.of(getButtonList());
    }

    /**
     * Build the message embed to send
     *
     * @return Message embed
     */
    public MessageEmbed buildMessage() {
        EmbedBuilder embedBuilder = getEmbedBuilder(getPageDetails());
        int max = Math.min(bound, (items.size() - this.index));
        for(int index = this.index; index < (this.index + max); index++) {
            displayItem(embedBuilder, index);
        }
        return embedBuilder.build();
    }

    /**
     * Get a String detailing the current page - e.g "Page: 1/5"
     *
     * @return String detailing the current page
     */
    public String getPageDetails() {
        return "Page: " + page + "/" + pages;
    }

    /**
     * Set the current index
     *
     * @param index Index to set
     */
    public void setIndex(int index) {
        this.index = index;
    }

    /**
     * Get the last button ID resulting in an action
     *
     * @return Last button ID resulting in action
     */
    public String getLastAction() {
        return lastAction;
    }

    /**
     * Check if there is a last pressed button ID
     *
     * @return Last action exists
     */
    public boolean hasLastAction() {
        return lastAction != null;
    }

    /**
     * React to the paging buttons by changing the value of the index
     *
     * @param event Button click event
     */
    public void buttonPressed(ButtonClickEvent event) {
        String buttonId = event.getComponentId();
        if(buttonId.equals(forward.getId())) {
            pageForward();
        }
        else if(buttonId.equals(backward.getId())) {
            pageBackward();
        }
        else {
            boolean actionPerformed = nonPagingButtonPressed(buttonId);
            if(!actionPerformed) {
                return;
            }
        }
        lastAction = buttonId;
        this.page = (index / bound) + 1;
        updateMessage(event);
    }

    /**
     * Check for any extra action to perform based on a button which was
     * pressed and was not a paging button.
     *
     * @param buttonId ID of the button which was pressed
     * @return Update the message
     */
    public abstract boolean nonPagingButtonPressed(String buttonId);

    /**
     * Page the index forward
     */
    public void pageForward() {
        if(isFinalPage()) {
            return;
        }
        index += bound;
    }

    /**
     * Page the index backward
     */
    public void pageBackward() {
        if(isFirstPage()) {
            return;
        }
        index -= bound;
    }

    /**
     * Check if the final page is being displayed
     *
     * @return Final page is being displayed
     */
    private boolean isFinalPage() {
        return (items.size() - 1) - index < bound;
    }

    /**
     * Check if the first page is being displayed
     *
     * @return First page is being displayed
     */
    private boolean isFirstPage() {
        return index == 0;
    }

    /**
     * Replace the list of items
     *
     * @param items List to replace current
     */
    public void updateItems(List<?> items) {
        this.items = items;
    }
}
