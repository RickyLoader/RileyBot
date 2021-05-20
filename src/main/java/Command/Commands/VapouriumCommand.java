package Command.Commands;

import Command.Structure.VapeStoreCommand;
import Vape.Vapourium;

/**
 * View Vapourium products
 */
public class VapouriumCommand extends VapeStoreCommand {
    public VapouriumCommand() {
        super("vape", Vapourium.getInstance());
    }
}
