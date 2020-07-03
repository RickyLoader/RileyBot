package Command.Commands.Variable;

import Command.Structure.LinkCommand;
import Command.Structure.VariableLinkCommand;

public class VodkaCommand extends VariableLinkCommand {
    public VodkaCommand(String json) {
        super(new String[]{"vodka","codka"},"vodka, codka", "Nothing like a nice refreshing glug of vodka!", json);
    }
}
