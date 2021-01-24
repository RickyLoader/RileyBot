package Command.Commands.Audio;

import Command.Structure.CommandContext;
import Command.Structure.DiscordCommand;
import net.dv8tion.jda.api.entities.Message;

/**
 * Play a given youtube video in the voice chat of the user
 */
public class PlayYoutubeCommand extends DiscordCommand {

    public PlayYoutubeCommand() {
        super("!play [LINK]", "Play a youtube clip in the voice chat!");
    }

    @Override
    public void execute(CommandContext context) {
        String audio = context.getMessageContent().replace("!play ", "");
        context.playAudio(
                audio
        );
    }

    @Override
    public boolean matches(String query, Message message) {
        return query.split(" ")[0].equalsIgnoreCase("!play");
    }
}
