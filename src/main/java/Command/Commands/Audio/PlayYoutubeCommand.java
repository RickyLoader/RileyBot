package Command.Commands.Audio;

import Audio.DiscordAudioPlayer;
import Command.Structure.CommandContext;
import Command.Structure.DiscordCommand;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;

/**
 * Play a given youtube video in the voice chat of the user
 */
public class PlayYoutubeCommand extends DiscordCommand {

    public PlayYoutubeCommand() {
        super("!play", "Play a youtube clip in the voice chat!", "!play [LINK]");
    }

    @Override
    public void execute(CommandContext context) {
        String audio = context.getMessageContent().substring(getTrigger().length()).trim();
        MessageChannel channel = context.getMessageChannel();

        if(audio.isEmpty()) {
            channel.sendMessage(getHelpNameCoded()).queue();
            return;
        }

        DiscordAudioPlayer.getInstance(context.getGuild()).play(audio, context.getMember(), context.getMessageChannel());
    }

    @Override
    public boolean matches(String query, Message message) {
        return query.startsWith(getTrigger());
    }
}
