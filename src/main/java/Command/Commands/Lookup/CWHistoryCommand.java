package Command.Commands.Lookup;

import Bot.GlobalReference;
import Command.Structure.MatchHistoryCommand;

/**
 * View a Cold War player's match history
 */
public class CWHistoryCommand extends MatchHistoryCommand {
    public CWHistoryCommand() {
        super("cwhistory", GlobalReference.CW_ASSET_MANAGER);
    }
}
