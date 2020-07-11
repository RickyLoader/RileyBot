package Command.Commands.Passive;

import Command.Structure.EmoteListener;
import COD.Gunfight;
import Command.Structure.CommandContext;
import Command.Structure.DiscordCommand;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageReaction;
import net.dv8tion.jda.api.entities.User;

public class GunfightHelpCommand extends DiscordCommand {
    private Gunfight gunfightHelp;
    private EmoteListener listener;

    public GunfightHelpCommand() {
        super("gunfight help!", "Get some help playing gunfight!");
    }

    @Override
    public void execute(CommandContext context) {

        if(gunfightHelp != null && gunfightHelp.isActive()) {
            gunfightHelp.relocate();
        }
        else {
            addEmoteListener(context.getJDA());
            gunfightHelp = new Gunfight(
                    context.getMessageChannel(),
                    context.getGuild()
            );
        }
    }

    @Override
    public String getTrigger() {
        return "gunfight help!";
    }

    private EmoteListener getEmoteListener() {
        return new EmoteListener() {
            @Override
            public void handleReaction(MessageReaction reaction, User user, Guild guild) {
                long reactID = reaction.getMessageIdLong();

                if(reactID == gunfightHelp.getGameId()) {
                    gunfightHelp.updateHelpMessage(reaction);
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
