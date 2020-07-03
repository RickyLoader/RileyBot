package Command.Commands.Link;

import Command.Structure.LinkCommand;

public class PureCommand extends LinkCommand {
    public PureCommand(String json) {
        super("pure", "A great deception!", json);
    }
}
