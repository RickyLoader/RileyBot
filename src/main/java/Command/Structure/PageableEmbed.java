package Command.Structure;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;

import java.util.ArrayList;
import java.util.List;

import static Command.Structure.EmbedHelper.getTitleField;
import static Command.Structure.EmbedHelper.getValueField;

public abstract class PageableEmbed {
    private final MessageChannel channel;
    private final List<?> items;
    private long id;
    private int index = 0, page = 1, bound;
    private final Emote forward, backward, reverse;
    private boolean defaultSort = true;
    private final String title, desc, thumb;
    private final String[] columns;

    public PageableEmbed(MessageChannel channel, Guild guild, List<?> items, String thumb, String title, String desc, String[] columns) {
        this.channel = channel;
        this.items = items;
        this.title = title;
        this.desc = desc;
        this.forward = guild.getEmotesByName("forward", true).get(0);
        this.backward = guild.getEmotesByName("backward", true).get(0);
        this.reverse = guild.getEmotesByName("reverse", true).get(0);
        this.thumb = thumb;
        this.columns = columns;
        this.bound = 5;
        sortItems(items, defaultSort);
    }

    public PageableEmbed(MessageChannel channel, Guild guild, List<?> items, String thumb, String title, String desc, String[] columns, int bound) {
        this(channel, guild, items, thumb, title, desc, columns);
        this.bound = bound;
    }

    public void showMessage() {
        channel.sendMessage(buildMessage()).queue(message -> {
            id = message.getIdLong();
            message.addReaction(backward).queue();
            message.addReaction(forward).queue();
            message.addReaction(reverse).queue();
        });
    }

    public void delete() {
        channel.retrieveMessageById(id).queue(message -> message.delete().queue());
    }

    private MessageEmbed buildMessage() {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setColor(15655767);
        builder.setTitle(title);
        builder.setDescription(desc);
        builder.setThumbnail(thumb);
        builder.setFooter("Page: " + page + "/" + (int) Math.ceil(items.size() / (double) bound));

        int max = Math.min(bound, (items.size() - index));

        for(int index = this.index; index < (this.index + max); index++) {
            String[] values = getValues(index, items, defaultSort);
            for(int i = 0; i < columns.length; i++) {
                if(index == this.index) {
                    builder.addField(getTitleField(columns[i], values[i]));
                }
                else {
                    builder.addField(getValueField(values[i]));
                }
                if(columns.length == 2 && i == 0) {
                    builder.addBlankField(true);
                }
            }
        }
        return builder.build();
    }

    public abstract String[] getValues(int index, List<?> items, boolean defaultSort);

    public abstract void sortItems(List<?> items, boolean defaultSort);

    public long getId() {
        return id;
    }

    private void updateMessage() {
        Message message = channel.retrieveMessageById(id).complete();
        message.editMessage(buildMessage()).queue();
    }

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
