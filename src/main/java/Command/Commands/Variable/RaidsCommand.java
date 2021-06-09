package Command.Commands.Variable;

import Command.Structure.VariableLinkCommand;

import java.util.HashMap;

public class RaidsCommand extends VariableLinkCommand {
    public RaidsCommand() {
        super("rs", "Some fun and helpful raids pictures!");
    }

    @Override
    public void insertVersions(HashMap<String, String> versions) {
        versions.put("raids", "https://imgur.com/a/67zKiQL");
        versions.put("chopping", "https://cdn.discordapp.com/attachments/359859612918480897/586748077461733377/elite_raid.png");
    }
}
