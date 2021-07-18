package Command.Commands.Lookup;

import COD.API.CWManager;
import Command.Structure.MatchHistoryCommand;

/**
 * View a Cold War player's match history
 */
public class CWHistoryCommand extends MatchHistoryCommand {
    public CWHistoryCommand() {
        super("cwhistory", CWManager.getInstance());
    }
}
