package Command.Commands.Variable;

import Command.Structure.VariableLinkCommand;

import java.util.HashMap;

public class VegetableCommand extends VariableLinkCommand {
    public VegetableCommand() {
        super("vege", "Some tasty vegetables!");
    }

    @Override
    public void insertVersions(HashMap<String, String> versions) {
        versions.put("banana", "https://preview.ibb.co/kUPfJA/Jpb6z1e.jpg");
        versions.put("brogle", "https://i.lensdump.com/i/AThL2Q.jpg");
    }
}
