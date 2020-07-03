package Command.Commands.Variable;

import Command.Structure.LinkCommand;
import Command.Structure.VariableLinkCommand;

public class FishingCommand extends VariableLinkCommand {
    public FishingCommand(String json) {
        super(new String[]{"fishing", "pokies"}, "fishing, pokies", "A man fishing with his best friend!", json);
    }
}
