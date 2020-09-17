package Command.Commands.Variable;

import Command.Structure.VariableLinkCommand;

public class VodkaCommand extends VariableLinkCommand {
    public VodkaCommand() {
        super(new String[]{"vodka", "vodka2", "codka"}, "Nothing like a nice refreshing glug of vodka!");
    }
}
