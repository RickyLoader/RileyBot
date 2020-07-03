package Command.Commands.Variable;

import Command.Structure.VariableLinkCommand;

public class VegetableCommand extends VariableLinkCommand {
    public VegetableCommand(String json) {
        super(new String[]{"brogle", "banana"}, "brogle, banana", "Some tasty vegetables!", json);
    }
}
