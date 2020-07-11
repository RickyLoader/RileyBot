package Command.Commands.Audio;

import Audio.DiscordAudioPlayer;
import Command.Structure.CommandContext;
import Command.Structure.DiscordCommand;

public class VoiceChannelCommand extends DiscordCommand {

    public VoiceChannelCommand() {
        super("stop/leave", "Leave the voice channel!");
    }

    @Override
    public void execute(CommandContext context) {
        DiscordAudioPlayer discordAudioPlayer = new DiscordAudioPlayer(context.getMember(), context.getGuild());
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
