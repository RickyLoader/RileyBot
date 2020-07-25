package Command.Commands.Variable;

import Command.Structure.VariableLinkCommand;

public class PhotoshopCommand extends VariableLinkCommand {
    public PhotoshopCommand() {
        super(new String[]{"cube", "friends"}, "cube, friends", "The fun stuff we get up to!");
    }
}
