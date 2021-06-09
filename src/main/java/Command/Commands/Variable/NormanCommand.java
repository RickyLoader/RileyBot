package Command.Commands.Variable;

import Command.Structure.VariableLinkCommand;

import java.util.HashMap;

public class NormanCommand extends VariableLinkCommand {
    public NormanCommand() {
        super("green goblin", "The green goblin and his glider!");
    }

    @Override
    public void insertVersions(HashMap<String, String> versions) {
        versions.put("norman", "https://preview.ibb.co/j8JwrV/IFXdToe.png");
        versions.put("norman2", "https://i.lensdump.com/i/AE2dTk.png");
        versions.put("norman3", "https://i.lensdump.com/i/AE27QP.png");
    }
}
