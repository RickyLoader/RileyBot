package Command.Commands.Audio;

import Command.Structure.CommandContext;
import Command.Structure.DiscordCommand;

public class KnockCommand extends DiscordCommand {
    private final String leftKnock = "https://www.youtube.com/watch?v=KkH3N9w5Ook";
    private final String rightKnock = "https://www.youtube.com/watch?v=cbuExw2EheQ";
    private String lastKnock = leftKnock;

    public KnockCommand() {
        super("knock", "Knock knock");
    }

    @Override
    public void execute(CommandContext context) {
        context.getMessage().delete().queue(
                delete -> {
                    context.playAudio(lastKnock);
                    lastKnock = lastKnock.equals(leftKnock) ? rightKnock : leftKnock;
                }
        );
    }
}
