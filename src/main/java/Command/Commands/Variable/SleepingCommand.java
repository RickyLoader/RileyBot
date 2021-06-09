package Command.Commands.Variable;

import Command.Structure.VariableLinkCommand;

import java.util.HashMap;

public class SleepingCommand extends VariableLinkCommand {
    public SleepingCommand() {
        super("sleep", "Falling asleep is fun!");
    }

    @Override
    public void insertVersions(HashMap<String, String> versions) {
        versions.put("sleeping", "https://i.lensdump.com/i/AE2Ie7.png");
        versions.put("sleeping2", "https://i.lensdump.com/i/AE2Qea.png");
    }
}
