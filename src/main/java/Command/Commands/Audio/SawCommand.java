package Command.Commands.Audio;

import Command.Structure.CommandContext;
import Command.Structure.DiscordCommand;

/**
 * Play the Saw theme in the voice chat of the user
 */
public class SawCommand extends DiscordCommand {

    public SawCommand() {
        super("saw", "The choice is yours!");
    }

    @Override
    public void execute(CommandContext context) {
        context.getMessage().delete().complete();
        context.getAudioPlayer().play(
                "https://www.youtube.com/watch?v=4VB2vjfNp_o",
                context.getMember(),
                context.getMessageChannel(),
                context.getGuild(),
                true
        );
    }
}
