package Command.Commands.Link;

import Command.Structure.LinkCommand;

public class MandrillCommand extends LinkCommand {
    public MandrillCommand(String json) {
        super("mandrill", "The most powerful PC ever built!", json);
    }
}
