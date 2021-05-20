package Command.Commands;

import Command.Structure.VapeStoreCommand;
import Vape.Vapo;

/**
 * View Vapo products
 */
public class VapoCommand extends VapeStoreCommand {
    public VapoCommand() {
        super("vapo", Vapo.getInstance());
    }
}
