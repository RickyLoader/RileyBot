package Command.Commands.Link;

import Command.Structure.LinkCommand;

public class BrewsCommand extends LinkCommand {
    public BrewsCommand(String json) {
        super("brews", "Danny having a quick sit down!", json);
    }
}
