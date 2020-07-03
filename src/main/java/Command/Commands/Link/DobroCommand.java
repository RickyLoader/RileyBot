package Command.Commands.Link;

import Command.Structure.LinkCommand;

public class DobroCommand extends LinkCommand {
    public DobroCommand(String json) {
        super("dobro", "A nice nap on the dobros!", json);
    }
}
