package Command.Commands.Random;

import Command.Structure.RandomCommand;

public class ElsaCommand extends RandomCommand {
    public ElsaCommand(String json) {
        super("elsa", "Posts a wonderful photo of elsa jean!", "elsa [#]", json);
    }
}
