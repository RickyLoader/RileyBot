package Command.Commands.Audio;

import Command.Structure.CommandContext;
import Command.Structure.DiscordCommand;

/**
 * Play a spooky ghost noise in the voice channel of the user
 */
public class GhostCommand extends DiscordCommand {

    public GhostCommand() {
        super("ghost", "Hear a cool ghost!");
    }

    @Override
    public void execute(CommandContext context) {
        context.getAudioPlayer().play(
                "https://www.youtube.com/watch?v=q3O4lCKcuWc",
                context.getMember(),
                context.getMessageChannel(),
                context.getGuild(),
                true
        );
    }
}
