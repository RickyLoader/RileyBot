package COD.API;

import COD.API.CODStatsManager.PLATFORM;
import Network.NetworkRequest;
import Network.NetworkResponse;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * API functions for cod.tracker.gg
 */
public class TrackerAPI {
    public static final String
            TRACKER_NAME = "tracker.gg",
            BASIC_KEY = "basic",
            COMMENDATIONS_KEY = "commendations",
            KILLSTREAKS_KEY = "killstreaks",
            WEAPONS_KEY = "weapons";

    private static final String
            BASE_API_URL = "https://api.tracker.gg/api/v2/",
            STATS_KEY = "stats",
            DATA_KEY = "data",
            MW_PATH = "modern-warfare",
            CW_PATH = "cold-war";

    /**
     * Get a modern warfare player's stats JSON from tracker.gg
     *
     * @param name     Player name
     * @param platform Player platform
     * @return Player stats JSON or null (if an error occurs)
     */
    @Nullable
    public static JSONObject getMWPlayerStatsJson(String name, PLATFORM platform) {
        return getPlayerStatsJson(name, platform, MW_PATH);
    }

    /**
     * Get a modern warfare player's match history JSON from tracker.gg
     * This is the last 20 matches in an array.
     *
     * @param name     Player name
     * @param platform Player platform
     * @return Player match history JSON or null (if an error occurs)
     */
    @Nullable
    public static JSONArray getMWMatchHistoryJson(String name, PLATFORM platform) {
        return getMatchHistoryJson(name, platform, MW_PATH);
    }

    /**
     * Get a cold war player's match history JSON from tracker.gg
     * This is the last 20 matches in an array.
     *
     * @param name     Player name
     * @param platform Player platform
     * @return Player match history JSON or null (if an error occurs)
     */
    @Nullable
    public static JSONArray getCWMatchHistoryJson(String name, PLATFORM platform) {
        return getMatchHistoryJson(name, platform, CW_PATH);
    }

    /**
     * Get a COD player's match history JSON from tracker.gg.
     * This is the last 20 matches in an array.
     *
     * @param name     Player name
     * @param platform Player platform
     * @param gamePath URL path to game API - e.g "modern-warfare"
     * @return Player match history JSON or null (if an error occurs)
     */
    @Nullable
    private static JSONArray getMatchHistoryJson(String name, PLATFORM platform, String gamePath) {
        final String url = BASE_API_URL + gamePath + "/standard/matches/" + getPlayerPath(name, platform) + "?type=mp";
        NetworkResponse response = new NetworkRequest(url, false).get();

        // No response from tracker
        if(response.code != 200) {
            return null;
        }

        return new JSONObject(response.body).getJSONObject(DATA_KEY).getJSONArray("matches");
    }

    /**
     * Get a COD player's stats JSON from tracker.gg
     *
     * @param name     Player name
     * @param platform Player platform
     * @param gamePath URL path to game API - e.g "modern-warfare"
     * @return Player stats JSON or null (if an error occurs)
     */
    @Nullable
    private static JSONObject getPlayerStatsJson(String name, PLATFORM platform, String gamePath) {
        final String baseUrl = BASE_API_URL + gamePath + "/standard/profile/" + getPlayerPath(name, platform);

        // Basic stats & commendations
        NetworkResponse basicResponse = new NetworkRequest(baseUrl, false).get();

        // No response from tracker
        if(basicResponse.code != 200) {
            return null;
        }

        final String segmentUrl = baseUrl + "segments/";

        NetworkResponse killstreakResponse = new NetworkRequest(segmentUrl + "killstreak", false).get();
        NetworkResponse weaponResponse = new NetworkRequest(segmentUrl + "weapon", false).get();

        // Issue fetching weapons/killstreaks
        if(killstreakResponse.code != 200 || weaponResponse.code != 200) {
            return null;
        }

        // Basic stats & commendations
        JSONArray mainData = new JSONObject(basicResponse.body).getJSONObject(DATA_KEY).getJSONArray("segments");

        // Combine the data sources in to one JSON object
        return new JSONObject()
                .put(BASIC_KEY, mainData.getJSONObject(0).getJSONObject(STATS_KEY))
                .put(COMMENDATIONS_KEY, mainData.getJSONObject(1).getJSONObject(STATS_KEY))
                .put(KILLSTREAKS_KEY, new JSONObject(killstreakResponse.body).getJSONArray(DATA_KEY))
                .put(WEAPONS_KEY, new JSONObject(weaponResponse.body).getJSONArray(DATA_KEY));
    }

    /**
     * Get the URL to view a player's stats for Modern Warfare on tracker.gg in a browser.
     *
     * @param name     Player name - e.g "ogrelover#2519"
     * @param platform Player platform - e.g BATTLE
     * @return Player profile URL - e.g "https://cod.tracker.gg/modern-warfare/profile/battlenet/ogrelover%232519/"
     */
    public static String getMWProfileUrl(String name, PLATFORM platform) {
        return getProfileUrl(name, platform, MW_PATH);
    }

    /**
     * Get the URL to view a player's stats for a specific COD game on tracker.gg in a browser.
     *
     * @param name     Player name - e.g "ogrelover#2519"
     * @param platform Player platform - e.g BATTLE
     * @param gamePath URL path to game API - e.g "modern-warfare"
     * @return Player profile URL - e.g "https://cod.tracker.gg/modern-warfare/profile/battlenet/ogrelover%232519/"
     */
    private static String getProfileUrl(String name, PLATFORM platform, String gamePath) {
        return "https://cod.tracker.gg/" + gamePath + "/profile/" + getPlayerPath(name, platform);
    }

    /**
     * Get the relative profile path for the given name and platform.
     * This is appended to player API endpoints.
     *
     * @param name     Player name - e.g "ogrelover#2519"
     * @param platform Player platform - e.g BATTLE
     * @return Relative profile path - e.g "battlenet/ogrelover%232519/"
     */
    private static String getPlayerPath(String name, PLATFORM platform) {
        return platform.getTrackerName() + "/" + CODAPI.encodeName(name) + "/";
    }
}
