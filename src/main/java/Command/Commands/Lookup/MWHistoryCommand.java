package Command.Commands.Lookup;

import Command.Structure.CommandContext;
import Command.Structure.MWLookupCommand;

/**
 * Get MW player match history
 */
public class MWHistoryCommand extends MWLookupCommand {
    public MWHistoryCommand(String trigger, String desc) {
        super(trigger, desc);
    }

    @Override
    public void processName(String name, CommandContext context) {

    }
}
