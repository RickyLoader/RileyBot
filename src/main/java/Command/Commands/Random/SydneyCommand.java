package Command.Commands.Random;

import Command.Structure.RandomCommand;

public class SydneyCommand extends RandomCommand {
    public SydneyCommand(String json) {
        super("sydney", "Posts a wonderful photo of sydney cole!", "sydney #", json);
    }
}
