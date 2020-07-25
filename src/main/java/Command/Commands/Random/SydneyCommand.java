package Command.Commands.Random;

import Command.Structure.RandomCommand;

public class SydneyCommand extends RandomCommand {
    public SydneyCommand() {
        super("sydney", "Posts a wonderful photo of sydney cole!", "sydney [#]", "sydney_command.json", "sydney");
    }
}
