package Command.Commands.Random;

import Command.Structure.RandomCommand;

public class KimmyCommand extends RandomCommand {
    public KimmyCommand(String json) {
        super("kimmy", "Posts a wonderful photo of kimmy granger!", "kimmy [#]", json);
    }
}
