package Command.Commands.Random;

import Command.Structure.RandomCommand;

public class MemeCommand extends RandomCommand {
    public MemeCommand(String json) {
        super("meme", "Posts a random meme!", "meme [#]", json);
    }
}
