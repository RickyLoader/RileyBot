package Command.Commands.Lookup;

import COD.API.CODAPI;
import COD.API.CODStatsManager;
import COD.API.CWManager;
import COD.API.TrackerAPI;
import Command.Structure.MatchHistoryCommand;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;

/**
 * View a Cold War player's match history
 */
public class CWHistoryCommand extends MatchHistoryCommand {
    public CWHistoryCommand() {
        super("cwhistory", CWManager.getInstance(), CWManager.THUMBNAIL);
    }

    @Override
    public String getMatchHistoryJSON(String name, CODStatsManager.PLATFORM platform) {
        return CODAPI.getCWMatchHistoryJson(name, platform);
    }

    @Override
    public @Nullable JSONArray getTrackerMatchHistoryJson(String name, CODStatsManager.PLATFORM platform) {
        return TrackerAPI.getCWMatchHistoryJson(name, platform);
    }
}
