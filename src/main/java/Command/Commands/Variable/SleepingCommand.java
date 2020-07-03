package Command.Commands.Variable;

import Command.Structure.VariableLinkCommand;

public class SleepingCommand extends VariableLinkCommand {
    public SleepingCommand(String json) {
        super(new String[]{"sleeping", "sleeping2"}, "sleeping, sleeping2", "Falling asleep is fun!", json);
    }
}
