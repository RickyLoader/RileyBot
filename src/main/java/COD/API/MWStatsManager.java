package COD.API;

import COD.API.Parsing.MWAPIParser;
import COD.API.Parsing.MWTrackerParser;
import COD.PlayerStats.*;
import Command.Structure.EmbedHelper;
import org.json.JSONObject;

import static COD.API.TrackerAPI.TRACKER_NAME;

/**
 * Get player Modern Warfare stats
 */
public class MWStatsManager extends CODStatsManager<MWManager, MWPlayerAssetStats, MWPlayerStats> {
    private final MWAPIParser apiParser;
    private final MWTrackerParser trackerParser;

    /**
     * Initialise the resource manager
     */
    public MWStatsManager() {
        super(MWManager.getInstance());
        this.apiParser = new MWAPIParser(getManager());
        this.trackerParser = new MWTrackerParser(getManager());
    }

    @Override
    public PlayerStatsResponse<MWPlayerAssetStats, MWPlayerStats> fetchPlayerStats(String name, PLATFORM platform) {
        final String json = CODAPI.getMWStats(name, platform);

        // No response from API
        if(json == null) {
            return new PlayerStatsResponse<>("Failed to communicate with API, try again later.");
        }

        JSONObject data = new JSONObject(json);
        final String status = data.getString("status");

        // Player doesn't exist etc
        if(!status.equals("success")) {
            return new PlayerStatsResponse<>(status);
        }

        return new PlayerStatsResponse<>(apiParser.parseStatsResponse(name, platform, data));
    }

    /**
     * Fetch the COD stats for a player of the given name and platform through the cod.tracker.gg API.
     * The response will contain the player's stats and optionally messages from the API indicating failure etc.
     *
     * @param name     Player name - adhering to platform rules
     * @param platform Player platform
     * @return Stats response - contains player stats and optionally messages from the API
     */
    public PlayerStatsResponse<MWPlayerAssetStats, MWPlayerStats> fetchPlayerStatsFallback(String name, PLATFORM platform) {
        final JSONObject playerStats = TrackerAPI.getMWPlayerStatsJson(name, platform);

        // URL to view stats in browser
        final String profileUrl = TrackerAPI.getMWProfileUrl(name, platform);

        // No response from tracker
        if(playerStats == null) {
            return new PlayerStatsResponse<>(
                    "No response for: "
                            + EmbedHelper.embedURL(name, profileUrl)
                            + " on " + TRACKER_NAME + " (platform = " + platform.name() + ")"
            );
        }

        return new PlayerStatsResponse<>(
                trackerParser.parseStatsResponse(
                        name,
                        platform,
                        playerStats
                ),
                "API down, stats retrieved from "
                        + EmbedHelper.embedURL(TRACKER_NAME, profileUrl)
                        + " instead.\n**Some will be missing** because they're lazy."
        );
    }
}
