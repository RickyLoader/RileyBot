package Command.Commands.Variable;

import Command.Structure.LinkCommand;
import Command.Structure.VariableLinkCommand;

public class RiggsCommand extends VariableLinkCommand {
    public RiggsCommand(String json) {
        super(new String[]{"riggs", "rather"}, "riggs, rather", "All about Riggs!", json);
    }
}
