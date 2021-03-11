package Command.Structure;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;

import java.util.List;

/**
 * Embedded message that can be paged through with emotes
 */
public abstract class PageableEmbed {
    private final MessageChannel channel;
    private final int bound, pages;
    private final Emote forward, backward;
    private final EmoteHelper emoteHelper;
    private List<?> items;
    private long id;
    private int index = 0, page = 1;
    private Emote lastAction;

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
        this.forward = emoteHelper.getForward();
        this.backward = emoteHelper.getBackward();
        context.getJDA().addEventListener(new EmoteListener() {
            @Override
            public void handleReaction(MessageReaction reaction, User user, Guild guild) {
                long reactID = reaction.getMessageIdLong();
                if(reactID == id) {
                    reactionAdded(reaction);
                }
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
     * Send the embed and add the paging emotes
     */
    public void showMessage() {
        channel.sendMessage(buildMessage()).queue(message -> {
            id = message.getIdLong();
            addReactions(message);
        });
    }

    /**
     * Add the required reactions to the message
     *
     * @param message Message to add reactions to
     */
    public void addReactions(Message message) {
        if(pages > 1) {
            message.addReaction(backward).queue();
            message.addReaction(forward).queue();
        }
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
     * Edit the embedded message in place
     */
    private void updateMessage() {
        channel.editMessageById(id, buildMessage()).queue();
    }

    /**
     * Build the message embed to send
     *
     * @return Message embed
     */
    public MessageEmbed buildMessage() {
        EmbedBuilder embedBuilder = getEmbedBuilder("Page: " + page + "/" + pages);
        int max = Math.min(bound, (items.size() - this.index));
        for(int index = this.index; index < (this.index + max); index++) {
            displayItem(embedBuilder, index);
        }
        return embedBuilder.build();
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
     * Get the last emote resulting in an action
     *
     * @return Last emote resulting in action
     */
    public Emote getLastAction() {
        return lastAction;
    }

    /**
     * React to the paging emotes by changing the value of the index
     *
     * @param reaction Reaction added
     */
    public void reactionAdded(MessageReaction reaction) {
        Emote emote = reaction.getReactionEmote().getEmote();
        if(emote == forward) {
            pageForward();
        }
        else if(emote == backward) {
            pageBackward();
        }
        else {
            boolean actionPerformed = nonPagingEmoteAdded(emote);
            if(!actionPerformed) {
                return;
            }
        }
        lastAction = emote;
        this.page = (index / bound) + 1;
        updateMessage();
    }

    /**
     * Check for any extra action to perform based on an emote which was
     * added and was not a paging emote.
     *
     * @param e Non paging emote which was added to the message
     * @return Update the message
     */
    public abstract boolean nonPagingEmoteAdded(Emote e);

    /**
     * Page the index forward
     */
    public void pageForward() {
        if((items.size() - 1) - index < bound) {
            return;
        }
        index += bound;
    }

    /**
     * Page the index backward
     */
    public void pageBackward() {
        if(index == 0) {
            return;
        }
        index -= bound;
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
