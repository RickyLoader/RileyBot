package Command.Commands.Link;

import Command.Structure.LinkCommand;

public class RememberCommand extends LinkCommand {
    public RememberCommand(String json) {
        super("remember", "Remember, no russian!", json);
    }
}
