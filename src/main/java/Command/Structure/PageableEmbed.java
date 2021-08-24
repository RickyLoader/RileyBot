package Command.Structure;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.Button;
import net.dv8tion.jda.api.requests.restaction.MessageAction;
import net.dv8tion.jda.api.requests.restaction.interactions.UpdateInteractionAction;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Embedded message that can be paged through with buttons
 */
public abstract class PageableEmbed<T> {
    private final MessageChannel channel;
    private final int bound, pages;
    private final Button forward, backward;
    private final EmoteHelper emoteHelper;
    private final List<T> items;
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
    public PageableEmbed(CommandContext context, List<T> items, int bound) {
        this.channel = context.getMessageChannel();
        this.items = items;
        this.bound = bound;
        this.pages = items.size() <= bound ? 1 : (int) Math.ceil(items.size() / (double) bound);
        this.emoteHelper = context.getEmoteHelper();
        this.forward = Button.success("forward", Emoji.fromEmote(emoteHelper.getForward()));
        this.backward = Button.success("backward", Emoji.fromEmote(emoteHelper.getBackward()));

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
    public List<T> getItems() {
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
        getMessageAction().queue(getMessageCallback());
    }

    /**
     * Get the message callback to use after sending the pageable message.
     * Remember the ID of the message for paging.
     *
     * @return Message callback
     */
    private Consumer<Message> getMessageCallback() {
        return message -> id = message.getIdLong();
    }

    /**
     * Get the message action to use for sending the pageable message.
     * This action involves building the message and (optionally) adding the paging buttons.
     *
     * @return Message action
     */
    protected MessageAction getMessageAction() {
        MessageAction sendMessage = channel.sendMessage(buildMessage());
        ActionRow buttonRow = getButtonRow();
        if(buttonRow != null) {
            sendMessage = sendMessage.setActionRows(buttonRow);
        }
        return sendMessage;
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
        channel.deleteMessageById(id).queue();
    }

    /**
     * Get the button used to page backward
     *
     * @return Page backward button
     */
    public Button getBackwardButton() {
        return backward;
    }

    /**
     * Get the button used to page forward
     *
     * @return Page forward button
     */
    public Button getForwardButton() {
        return forward;
    }

    /**
     * Get the embed builder to use
     *
     * @param pageDetails Current page details - e.g "Page: 1/5"
     * @return Embed builder
     */
    public abstract EmbedBuilder getEmbedBuilder(String pageDetails);

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
     *
     * @param event Event to acknowledge
     */
    private void updateMessage(ButtonClickEvent event) {
        UpdateInteractionAction updateMessage = event.deferEdit().setEmbeds(buildMessage());
        ActionRow buttonRow = getButtonRow();
        if(buttonRow != null) {
            updateMessage = updateMessage.setActionRows(buttonRow);
        }
        updateMessage.queue();
    }

    /**
     * Get the row of buttons to add to the message
     *
     * @return Row of buttons
     */
    @Nullable
    private ActionRow getButtonRow() {
        ArrayList<Button> buttons = getButtonList();
        return buttons.isEmpty() ? null : ActionRow.of(buttons);
    }

    /**
     * Build the message embed to send
     *
     * @return Message embed
     */
    public MessageEmbed buildMessage() {
        if(items.isEmpty()) {
            return getNoItemsEmbed();
        }
        else {
            EmbedBuilder embedBuilder = getEmbedBuilder(getPageDetails());
            int pageStart = index;

            // Page is either bound length or remainder of items in list
            int pageEnd = pageStart + Math.min(bound, (items.size() - pageStart));

            displayPageItems(embedBuilder, items.subList(pageStart, pageEnd), pageStart);
            return embedBuilder.build();
        }
    }

    /**
     * Display the given item (item at currentIndex) in the embed builder
     *
     * @param builder      Embed builder to display item in
     * @param currentIndex Current index within list of items
     * @param item         Item at current index
     */
    public abstract void displayItem(EmbedBuilder builder, int currentIndex, T item);

    /**
     * Display the given page of items in the message embed.
     * TODO abstract
     *
     * @param builder    Embed builder to display page items in
     * @param pageItems  List of items to display on the page (sublist of items)
     * @param startingAt Index of page start within full list of items
     */
    public void displayPageItems(EmbedBuilder builder, List<T> pageItems, int startingAt) {
        for(int i = 0; i < pageItems.size(); i++) {
            displayItem(builder, startingAt + i, pageItems.get(i));
        }
    }

    /**
     * Get the message embed to use when there are no items to display
     *
     * @return No items message embed
     */
    protected abstract MessageEmbed getNoItemsEmbed();

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
}
