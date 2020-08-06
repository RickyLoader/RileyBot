package Command.Commands.Passive;

import Command.Structure.EmoteListener;
import COD.Gunfight;
import Command.Structure.CommandContext;
import Command.Structure.DiscordCommand;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageReaction;
import net.dv8tion.jda.api.entities.User;

/**
 * Show a message explaining how to use the gunfight command
 */
public class GunfightHelpCommand extends DiscordCommand {
    private Gunfight gunfightHelp;
    private EmoteListener listener;

    public GunfightHelpCommand() {
        super("gunfight help!", "Get some help playing gunfight!");
    }

    /**
     * Show the help message or relocate the current message
     *
     * @param context Command context
     */
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

    /**
     * Get an emote listener for calling the Gunfight instance when emotes are clicked
     *
     * @return Emote listener
     */
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

    /**
     * Add an emote listener to listen for gunfight emotes if there isn't one already
     *
     * @param jda BOT
     */
    private void addEmoteListener(JDA jda) {
        if(this.listener == null) {
            this.listener = getEmoteListener();
            jda.addEventListener(this.listener);
        }
    }
}
