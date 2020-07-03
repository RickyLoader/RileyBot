package Command.Commands.Variable;

import Command.Structure.VariableLinkCommand;

public class NormanCommand extends VariableLinkCommand {
    public NormanCommand(String json) {
        super(new String[]{"norman", "norman2", "norman3"}, "norman, norman2, norman3", "The green goblin and his glider!", json);
    }
}
