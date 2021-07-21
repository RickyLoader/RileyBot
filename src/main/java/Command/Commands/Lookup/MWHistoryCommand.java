package Command.Commands.Lookup;

import COD.API.CODAPI;
import COD.API.CODStatsManager;
import COD.API.MWManager;
import COD.API.TrackerAPI;
import Command.Structure.MatchHistoryCommand;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;

/**
 * View a Modern Warfare player's match history
 */
public class MWHistoryCommand extends MatchHistoryCommand {
    public MWHistoryCommand() {
        super("mwhistory", MWManager.getInstance(), MWManager.THUMBNAIL);
    }

    @Override
    public String getMatchHistoryJSON(String name, CODStatsManager.PLATFORM platform) {
        return CODAPI.getMWMatchHistoryJson(name, platform);
    }

    @Override
    public @Nullable JSONArray getTrackerMatchHistoryJson(String name, CODStatsManager.PLATFORM platform) {
        return TrackerAPI.getMWMatchHistoryJson(name, platform);
    }
}
