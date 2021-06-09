package Command.Commands.Variable;

import Command.Structure.VariableLinkCommand;

import java.util.HashMap;

public class RiggsCommand extends VariableLinkCommand {
    public RiggsCommand() {
        super("train", "All about Riggs!");
    }

    @Override
    public void insertVersions(HashMap<String, String> versions) {
        versions.put("riggs", "https://steamcommunity.com/profiles/76561198073367937");
        versions.put("rather", "https://i.lensdump.com/i/ATfhfe.png");
    }
}
