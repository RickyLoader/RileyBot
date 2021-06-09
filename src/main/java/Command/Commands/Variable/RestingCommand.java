package Command.Commands.Variable;

import Command.Structure.VariableLinkCommand;

import java.util.HashMap;

public class RestingCommand extends VariableLinkCommand {
    public RestingCommand() {
        super("rest", "Taking a rest on multiple surfaces!");
    }

    @Override
    public void insertVersions(HashMap<String, String> versions) {
        versions.put("resting", "https://i.lensdump.com/i/AThVKv.jpg");
        versions.put("resting2", "https://preview.ibb.co/gyxd5q/lolMWQy.jpg");
    }
}
