package Command.Structure;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;

import java.util.List;

/**
 * Embedded message that can be paged through with emotes
 */
public abstract class PageableEmbed {
    private final MessageChannel channel;
    private final List<?> items;
    private long id;
    private int index = 0, page = 1;
    private final int bound, colour;
    private final Emote forward, backward, reverse;
    private boolean defaultSort = true;
    private final String title, desc, thumb;

    /**
     * Initialise the values
     *
     * @param channel Channel to send embed to
     * @param guild   Guild to find emotes
     * @param items   List of items to be displayed
     * @param thumb   Thumbnail to use for embed
     * @param title   Title to use for embed
     * @param desc    Description to use for embed
     * @param colour  Optional colour to use for embed
     */
    public PageableEmbed(MessageChannel channel, Guild guild, List<?> items, String thumb, String title, String desc, int... colour) {
        this.channel = channel;
        this.items = items;
        this.title = title;
        this.desc = desc;
        this.forward = guild.getEmotesByName("forward", true).get(0);
        this.backward = guild.getEmotesByName("backward", true).get(0);
        this.reverse = guild.getEmotesByName("reverse", true).get(0);
        this.thumb = thumb;
        this.bound = 5;
        this.colour = colour.length == 1 ? colour[0] : EmbedHelper.getYellow();
        sortItems(items, defaultSort);
    }

    /**
     * Get the title of the embedded message
     *
     * @return Title of embedded message
     */
    public String getTitle() {
        return title;
    }

    /**
     * Get the description of the embedded message
     *
     * @return Description of embedded message
     */
    public String getDesc() {
        return desc;
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
     * Get the current sort value
     *
     * @return Current sort value
     */
    public boolean isDefaultSort() {
        return defaultSort;
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
     * Get the thumbnail of the embedded message
     *
     * @return Thumbnail of embedded message
     */
    public String getThumb() {
        return thumb;
    }

    /**
     * Get the bound, how many items to display per page
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
            message.addReaction(backward).queue();
            message.addReaction(forward).queue();
            message.addReaction(reverse).queue();
        });
    }

    /**
     * Delete the embed
     */
    public void delete() {
        channel.retrieveMessageById(id).queue(message -> message.delete().queue());
    }

    /**
     * Build the message embed
     *
     * @return Message embed
     */
    public MessageEmbed buildMessage() {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setColor(colour);
        builder.setTitle(title);
        builder.setDescription(desc);
        builder.setThumbnail(thumb);
        builder.setFooter("Page: " + page + "/" + (int) Math.ceil(items.size() / (double) bound));

        int max = Math.min(bound, (items.size() - this.index));

        for(int index = this.index; index < (this.index + max); index++) {
            addFields(builder, index);
        }
        return builder.build();
    }

    /**
     * Add fields to the embed based on the current index
     *
     * @param builder      Embed builder to add fields to
     * @param currentIndex Current index within list of items
     */
    public abstract void addFields(EmbedBuilder builder, int currentIndex);

    /**
     * Sort the items based on the value of defaultSort
     *
     * @param items       List of objects to sort
     * @param defaultSort How to sort the items
     */
    public abstract void sortItems(List<?> items, boolean defaultSort);

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
        channel.retrieveMessageById(id).queue(message -> {
            MessageEmbed update = buildMessage();
            if(update == null) {
                return;
            }
            message.editMessage(update).queue();
        });
    }

    /**
     * React to the paging emotes by changing the value of the index
     *
     * @param reaction Reaction added
     */
    public void reactionAdded(MessageReaction reaction) {
        Emote emote = reaction.getReactionEmote().getEmote();
        if(emote != forward && emote != backward && emote != reverse) {
            return;
        }

        if(emote == forward) {
            if((items.size() - 1) - index < bound) {
                return;
            }
            index += bound;
        }
        else if(emote == backward) {
            if(index == 0) {
                return;
            }
            index -= bound;
        }
        else {
            defaultSort = !defaultSort;
            sortItems(items, defaultSort);
            index = 0;
        }
        this.page = (index / bound) + 1;
        updateMessage();
    }
}
