package Command.Commands.Audio;

import Audio.DiscordAudioPlayer;
import Command.Structure.CommandContext;
import Command.Structure.DiscordCommand;

public class PlayYoutubeCommand extends DiscordCommand {

    public PlayYoutubeCommand() {
        super("!play [LINK]", "Play a youtube clip in the voice chat!");
    }

    @Override
    public void execute(CommandContext context) {
        String audio = context.getMessageContent().replace("!play ", "");
        new DiscordAudioPlayer(context.getMember(), context.getGuild()).play(audio);
    }
}
