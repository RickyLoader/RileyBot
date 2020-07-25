package Command.Commands.Random;

import Command.Structure.RandomCommand;

public class BrockCommand extends RandomCommand {
    public BrockCommand() {
        super("brock", "Posts a wonderful photo of brock cooper!", "brock [#]", "brock_command.json", "brock");
    }
}
