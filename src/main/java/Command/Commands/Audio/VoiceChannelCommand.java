package Command.Commands.Audio;

import Command.Structure.CommandContext;
import Command.Structure.DiscordCommand;
import net.dv8tion.jda.api.entities.Message;

/**
 * Leave, stop functionality for the bot while it is playing in a voice channel
 */
public class VoiceChannelCommand extends DiscordCommand {

    public VoiceChannelCommand() {
        super("stop/leave", "Leave the voice channel!");
    }

    @Override
    public void execute(CommandContext context) {
        context.getAudioPlayer().stop(context.getGuild());
    }

    @Override
    public boolean matches(String query, Message message) {
        return query.equals("stop") || query.equals("leave");
    }
}
