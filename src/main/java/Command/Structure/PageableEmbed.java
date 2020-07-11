package Command.Structure;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;

import java.util.ArrayList;

public abstract class PageableEmbed {
    private final MessageChannel channel;
    private final ArrayList<?> items;
    private long id;
    private int index = 0;
    private int page = 1;
    private final Emote forward, backward, reverse;
    private boolean defaultSort = true;
    private final String title, desc, thumb;

    public PageableEmbed(MessageChannel channel, Guild guild, ArrayList<?> items, String thumb, String title, String desc) {
        this.channel = channel;
        this.items = items;
        this.title = title;
        this.desc = desc;
        this.forward = guild.getEmotesByName("forward", true).get(0);
        this.backward = guild.getEmotesByName("backward", true).get(0);
        this.reverse = guild.getEmotesByName("reverse", true).get(0);
        this.thumb = thumb;
        sortItems(items, defaultSort);
        showMessage();
    }

    private void showMessage() {
        channel.sendMessage(buildMessage()).queue(message -> {
            id = message.getIdLong();
            message.addReaction(backward).queue();
            message.addReaction(forward).queue();
            message.addReaction(reverse).queue();
        });
    }

    public MessageEmbed.Field getBlankField(boolean inline) {
        return new MessageEmbed.Field(getBlankChar(), getBlankChar(), inline);
    }

    public MessageEmbed.Field getTitleField(String title, String value, boolean inline) {
        return new MessageEmbed.Field(title, value, inline);
    }

    public String getBlankChar() {
        return "\u200e";
    }

    public void delete() {
        channel.retrieveMessageById(id).queue(message -> message.delete().queue());
    }

    public abstract MessageEmbed.Field[] getField(int index, ArrayList<?> items, boolean header, boolean ascending);

    public abstract void sortItems(ArrayList<?> items, boolean defaultSort);

    private MessageEmbed buildMessage() {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setColor(15655767);
        builder.setTitle(title);
        builder.setDescription(desc);
        builder.setThumbnail(thumb);
        builder.setFooter("Page: " + page + "/" + (int) Math.ceil(items.size() / 5.0));

        int max = Math.min(5, (items.size() - index));

        for(int index = this.index; index < (this.index + max); index++) {
            for(MessageEmbed.Field field : getField(index, items, index == this.index, defaultSort)) {
                builder.addField(field);
            }
        }
        return builder.build();
    }

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
            if((items.size() - 1) - index < 5) {
                return;
            }
            index += 5;
        }
        else if(emote == backward) {
            if(index == 0) {
                return;
            }
            index -= 5;
        }
        else {
            defaultSort = !defaultSort;
            sortItems(items, defaultSort);
            index = 0;
        }
        this.page = (index / 5) + 1;
        updateMessage();
    }
}
