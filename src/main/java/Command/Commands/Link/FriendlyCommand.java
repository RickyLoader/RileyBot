package Command.Commands.Link;

import Command.Structure.LinkCommand;

public class FriendlyCommand extends LinkCommand {
    public FriendlyCommand(String json) {
        super("friendly", "A nice friendly exchange!", json);
    }
}
