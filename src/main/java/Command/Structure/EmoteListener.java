package Command.Structure;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageReaction;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import javax.annotation.Nonnull;


public abstract class EmoteListener extends ListenerAdapter {

    @Override
    public void onGuildMessageReactionAdd(@Nonnull GuildMessageReactionAddEvent event) {
        reactionAdded(event.getReaction(), event.getUser(), event.getGuild());
    }

    @Override
    public void onGuildMessageReactionRemove(@Nonnull GuildMessageReactionRemoveEvent event) {
        reactionAdded(event.getReaction(), event.getUser(), event.getGuild());
    }

    private void reactionAdded(MessageReaction reaction, User user, Guild guild) {

        if(user == reaction.getJDA().getSelfUser()) {
            return;
        }
        handleReaction(reaction, user, guild);
    }

    public abstract void handleReaction(MessageReaction reaction, User user, Guild guild);
}
