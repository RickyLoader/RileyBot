package Command.Commands.Audio;

import Command.Structure.CommandContext;
import Command.Structure.DiscordCommand;
import net.dv8tion.jda.api.entities.Message;

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

        if(audio.isEmpty()) {
            context.getMessageChannel().sendMessage(getHelpNameCoded()).queue();
            return;
        }

        context.playAudio(audio);
    }

    @Override
    public boolean matches(String query, Message message) {
        return query.startsWith(getTrigger());
    }
}
