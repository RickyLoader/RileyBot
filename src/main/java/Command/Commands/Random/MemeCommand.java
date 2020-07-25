package Command.Commands.Random;

import Command.Structure.RandomCommand;

public class MemeCommand extends RandomCommand {
    public MemeCommand() {
        super("meme", "Posts a random meme!", "meme [#]", "meme_command.json", "meme");
    }
}
