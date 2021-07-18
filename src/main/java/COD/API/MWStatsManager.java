package COD.API;

import COD.API.Parsing.MWAPIStatsParser;
import COD.API.Parsing.MWTrackerStatsParser;
import COD.PlayerStats.*;
import Command.Structure.EmbedHelper;
import Network.NetworkRequest;
import Network.NetworkResponse;
import org.json.JSONObject;

/**
 * Get player Modern Warfare stats
 */
public class MWStatsManager extends CODStatsManager<MWManager, MWPlayerAssetStats, MWPlayerStats> {
    private final MWAPIStatsParser apiParser;
    private final MWTrackerStatsParser trackerParser;
    public static final String TRACKER_NAME = "tracker.gg";

    /**
     * Initialise the resource manager
     */
    public MWStatsManager() {
        super(MWManager.getInstance());
        this.apiParser = new MWAPIStatsParser(getManager());
        this.trackerParser = new MWTrackerStatsParser(getManager());
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

        MWPlayerStats playerStats = new MWPlayerStats(
                name,
                platform,
                apiParser.parseAssetStats(data.getJSONObject("data").getJSONObject("lifetime")),
                apiParser.parseBasicStats(data.getJSONObject("basic"))
        );

        return new PlayerStatsResponse<>(playerStats);
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
        final String urlPath = platform.getTrackerName()
                + "/" + CODAPI.encodeName(name)
                + "/";

        final String viewUrl = "https://cod.tracker.gg/modern-warfare/profile/" + urlPath;
        final String baseUrl = "https://api.tracker.gg/api/v2/modern-warfare/standard/profile/" + urlPath;

        final String enquiry = "**" + name + "** on " + TRACKER_NAME + " (platform = " + platform.name() + ")";

        // Basic stats & commendations
        NetworkResponse basicResponse = new NetworkRequest(baseUrl, false).get();

        // No response from tracker
        if(basicResponse.code != 200) {
            return new PlayerStatsResponse<>(
                    "No response for: " + enquiry
            );
        }

        final String segmentUrl = baseUrl + "segments/";

        NetworkResponse killstreakResponse = new NetworkRequest(segmentUrl + "killstreak", false).get();
        NetworkResponse weaponResponse = new NetworkRequest(segmentUrl + "weapon", false).get();

        // Issue fetching weapons/killstreaks
        if(killstreakResponse.code != 200 || weaponResponse.code != 200) {
            return new PlayerStatsResponse<>("Not enough data available for: " + enquiry);
        }

        return new PlayerStatsResponse<>(
                trackerParser.parseTrackerResponse(
                        name,
                        platform,
                        new JSONObject(basicResponse.body),
                        new JSONObject(weaponResponse.body),
                        new JSONObject(killstreakResponse.body)
                ),
                "API down, stats retrieved from "
                        + EmbedHelper.embedURL(TRACKER_NAME, viewUrl)
                        + " instead.\n**Some will be missing** because they're lazy."
        );
    }
}
