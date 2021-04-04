package Command.Commands.Lookup;

import Bot.GlobalReference;
import Command.Structure.MatchHistoryCommand;

/**
 * View a Modern Warfare player's match history
 */
public class MWHistoryCommand extends MatchHistoryCommand {
    public MWHistoryCommand() {
        super("mwhistory", GlobalReference.MW_ASSET_MANAGER);
    }
}
