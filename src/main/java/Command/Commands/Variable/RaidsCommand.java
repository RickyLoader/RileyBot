package Command.Commands.Variable;

import Command.Structure.VariableLinkCommand;

public class RaidsCommand extends VariableLinkCommand {
    public RaidsCommand(String json) {
        super(new String[]{"raids", "chopping"}, "raids, chopping", "Some fun and helpful raids pictures!", json);
    }
}
