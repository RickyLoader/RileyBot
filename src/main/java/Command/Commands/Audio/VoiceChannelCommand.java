package Command.Commands.Audio;

import Audio.DiscordAudioPlayer;
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
        DiscordAudioPlayer discordAudioPlayer = new DiscordAudioPlayer(context.getMember(), context.getGuild(), context.getMessageChannel());
        if(context.getLowerCaseMessage().equals("stop")) {
            discordAudioPlayer.leave();
        }
        else {
            discordAudioPlayer.join(discordAudioPlayer.getMemberVoiceChannel());
        }
    }

    @Override
    public boolean matches(String query) {
        return query.equalsIgnoreCase("stop") || query.equalsIgnoreCase("join");
    }
}
