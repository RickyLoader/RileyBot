package Command.Commands.Variable;

import Command.Structure.VariableLinkCommand;

public class TurboCommand extends VariableLinkCommand {
    public TurboCommand(String json) {
        super(new String[]{"turbo","turbogreg","turbobob"},"turbo, turbogreg, turbobob", "The turbo specialists!", json);
    }
}
