package Command.Commands.Passive;

import COD.GunfightEmoteListener;
import COD.Gunfight;
import Command.Structure.CommandContext;
import Command.Structure.DiscordCommand;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageReaction;
import net.dv8tion.jda.api.entities.User;

public class GunfightCommand extends DiscordCommand {
    private Gunfight gunfight;
    private GunfightEmoteListener listener;

    public GunfightCommand() {
        super("gunfight!", "Play a fun game of gunfight!");
    }

    @Override
    public void execute(CommandContext context) {
        if(gunfight != null && gunfight.isActive()) {
            gunfight.relocate();
        }
        else {
            addEmoteListener(context.getJDA());
            gunfight = new Gunfight(
                    context.getMessageChannel(),
                    context.getGuild(),
                    context.getUser()
            );
        }
    }

    private GunfightEmoteListener getEmoteListener() {
        return new GunfightEmoteListener() {
            @Override
            public void handleReaction(MessageReaction reaction, User user, Guild guild) {
                long reactID = reaction.getMessageIdLong();

                if(gunfight != null && reactID == gunfight.getGameId() && gunfight.isActive() && (user == gunfight.getOwner() || (user == guild.getOwner().getUser()))) {
                    gunfight.reactionAdded(reaction);
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
