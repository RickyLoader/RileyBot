package Command.Commands.Variable;

import Command.Structure.VariableLinkCommand;

import java.util.HashMap;

public class PhotoshopCommand extends VariableLinkCommand {
    public PhotoshopCommand() {
        super("photoshop", "The fun stuff we get up to!");
    }

    @Override
    public void insertVersions(HashMap<String, String> versions) {
        versions.put("cube", "https://i.lensdump.com/i/AThF0c.jpg");
        versions.put("friends", "https://i.lensdump.com/i/AThxei.png");
    }
}
