package Command.Commands.Random;

import Command.Structure.RandomCommand;

public class BrockCommand extends RandomCommand {
    public BrockCommand(String json) {
        super("brock", "Posts a wonderful photo of brock cooper!", "brock [#]", json);
    }
}
