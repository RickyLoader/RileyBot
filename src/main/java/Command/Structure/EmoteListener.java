package Command.Structure;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageReaction;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import javax.annotation.Nonnull;


public abstract class EmoteListener extends ListenerAdapter {
    private final User self;

    /**
     * Create the emote listener
     * Use JDA to get the bot user to allow removing reactions from anyone but the bot
     *
     * @param jda JDA to determine bot user
     */
    public EmoteListener(JDA jda) {
        this.self = jda.getSelfUser();
    }

    @Override
    public void onGuildMessageReactionAdd(@Nonnull GuildMessageReactionAddEvent event) {
        User eventUser = event.getUser();
        if(eventUser == self) {
            return;
        }
        MessageReaction reaction = event.getReaction();
        reaction.removeReaction(eventUser).queue();
        handleReaction(reaction, eventUser, event.getGuild());
    }

    public abstract void handleReaction(MessageReaction reaction, User user, Guild guild);
}
