package Command.Commands.Audio;

import Command.Structure.CommandContext;
import Command.Structure.DiscordCommand;

/**
 * Leave, stop functionality for the bot while it is playing in a voice channel
 */
public class VoiceChannelCommand extends DiscordCommand {

    public VoiceChannelCommand() {
        super("stop/leave", "Leave the voice channel!");
    }

    @Override
    public void execute(CommandContext context) {
        if(!context.getAudioPlayer().stop(context.getGuild())) {
            context.getMessageChannel().sendMessage("You're not allowed to stop this").queue();
        }
    }

    @Override
    public boolean matches(String query) {
        return query.equals("stop") || query.equals("leave");
    }
}
