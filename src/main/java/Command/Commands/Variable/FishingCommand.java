package Command.Commands.Variable;

import Command.Structure.VariableLinkCommand;

import java.util.HashMap;

public class FishingCommand extends VariableLinkCommand {
    public FishingCommand() {
        super("bear", "A man fishing with his best friend!");
    }

    @Override
    public void insertVersions(HashMap<String, String> versions) {
        versions.put("fishing", "https://i.lensdump.com/i/AThnYM.jpg");
        versions.put("pokies", "https://i.lensdump.com/i/ABBy57.jpg");
    }
}
