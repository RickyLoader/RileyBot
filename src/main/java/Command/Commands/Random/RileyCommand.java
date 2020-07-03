package Command.Commands.Random;

import Command.Structure.RandomCommand;

public class RileyCommand extends RandomCommand {
    public RileyCommand(String json) {
        super("riley", "Posts a wonderful photo of riley reid!", "riley #", json);
    }
}
