package Command.Structure;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageReaction;
import net.dv8tion.jda.api.entities.User;

public abstract class PageableEmbedCommand extends DiscordCommand {
    private EmoteListener listener;
    private PageableEmbed embed;

    public PageableEmbedCommand(String trigger, String desc, String helpName) {
        super(trigger, desc, helpName);
    }

    public PageableEmbedCommand(String trigger, String desc) {
        super(trigger, desc);
    }

    @Override
    public void execute(CommandContext context) {
        if(embed != null) {
            embed.delete();
        }
        addEmoteListener(context.getJDA());
        embed = getEmbed(context);
    }

    public abstract PageableEmbed getEmbed(CommandContext context);

    private EmoteListener getEmoteListener() {
        return new EmoteListener() {
            @Override
            public void handleReaction(MessageReaction reaction, User user, Guild guild) {
                long reactID = reaction.getMessageIdLong();
                if(embed != null && reactID == embed.getId()) {
                    embed.reactionAdded(reaction);
                }
            }
        };
    }

    private void addEmoteListener(JDA jda) {
        if(this.listener == null) {
            this.listener = getEmoteListener();
            jda.addEventListener(this.listener);
        }
    }
}
