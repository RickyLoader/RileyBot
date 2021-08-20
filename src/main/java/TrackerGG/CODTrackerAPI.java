package TrackerGG;

import COD.API.CODStatsManager.PLATFORM;
import Network.NetworkResponse;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * API functions for cod.tracker.gg
 */
public class CODTrackerAPI extends TrackerAPI {
    public static final String
            DOMAIN = "cod." + DEFAULT_DOMAIN,
            COMMENDATIONS_KEY = "commendations",
            KILLSTREAKS_KEY = "killstreak",
            MATCH_HISTORY_ARGS = "?type=mp";

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
        return getMatchHistoryJson(name, TRACKER_PLATFORM.fromCodPlatform(platform), MW_PATH, MATCH_HISTORY_ARGS);
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
        return getMatchHistoryJson(name, TRACKER_PLATFORM.fromCodPlatform(platform), CW_PATH, MATCH_HISTORY_ARGS);
    }

    /**
     * Get the URL to view a player's stats for Modern Warfare on tracker.gg in a browser.
     *
     * @param name     Player name - e.g "ogrelover#2519"
     * @param platform Player platform - e.g BATTLE
     * @return Player profile URL - e.g "https://cod.tracker.gg/modern-warfare/profile/battlenet/ogrelover%232519/"
     */
    public static String getMWProfileUrl(String name, PLATFORM platform) {
        return getProfileUrl(DOMAIN, name, TRACKER_PLATFORM.fromCodPlatform(platform), MW_PATH);
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
        TRACKER_PLATFORM trackerPlatform = TRACKER_PLATFORM.fromCodPlatform(platform);

        NetworkResponse profileResponse = getPlayerProfileResponse(name, trackerPlatform, gamePath);

        // No response from tracker
        if(profileResponse.code != 200) {
            return null;
        }

        JSONObject killstreaks = getPlayerSegmentJson(name, trackerPlatform, gamePath, KILLSTREAKS_KEY);
        JSONObject weapons = getPlayerSegmentJson(name, trackerPlatform, gamePath, WEAPONS_KEY);

        // Issue fetching weapons/killstreaks
        if(weapons == null || killstreaks == null) {
            return null;
        }

        // Basic stats & commendations
        JSONArray mainData = new JSONObject(profileResponse.body).getJSONObject(DATA_KEY).getJSONArray(SEGMENTS_KEY);

        // Combine the data sources in to one JSON object
        return new JSONObject()
                .put(BASIC_KEY, mainData.getJSONObject(0).getJSONObject(STATS_KEY))
                .put(COMMENDATIONS_KEY, mainData.getJSONObject(1).getJSONObject(STATS_KEY))
                .put(KILLSTREAKS_KEY, killstreaks.getJSONArray(DATA_KEY))
                .put(WEAPONS_KEY, weapons.getJSONArray(DATA_KEY));
    }
}
