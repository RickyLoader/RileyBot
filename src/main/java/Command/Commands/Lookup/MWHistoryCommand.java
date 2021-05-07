package Command.Commands.Lookup;

import COD.MWManager;
import Command.Structure.MatchHistoryCommand;

/**
 * View a Modern Warfare player's match history
 */
public class MWHistoryCommand extends MatchHistoryCommand {
    public MWHistoryCommand() {
        super("mwhistory", MWManager.getInstance());
    }
}
