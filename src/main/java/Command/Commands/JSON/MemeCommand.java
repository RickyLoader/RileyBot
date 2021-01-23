package Command.Commands.JSON;

import Command.Structure.RandomLinkCommand;

public class MemeCommand extends RandomLinkCommand {
    public MemeCommand() {
        super("meme", "Posts a random meme!", "meme_command.json", "meme");
    }
}
