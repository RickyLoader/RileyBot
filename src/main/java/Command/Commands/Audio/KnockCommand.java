package Command.Commands.Audio;

import Command.Structure.CommandContext;
import Command.Structure.DiscordCommand;

public class KnockCommand extends DiscordCommand {
    public KnockCommand() {
        super("knock", "Knock knock");
    }

    @Override
    public void execute(CommandContext context) {
        context.getMessage().delete().queue(
                aVoid -> context.playAudio("https://www.youtube.com/watch?v=nDAfnDnT2gA")
        );
    }
}
