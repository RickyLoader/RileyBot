package TrackerGG;

import COD.API.CODAPI;
import COD.API.CODStatsManager.PLATFORM;
import Network.NetworkRequest;
import Network.NetworkResponse;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * API functions for tracker.gg
 */
public class TrackerAPI {
    public static final String
            DEFAULT_DOMAIN = "tracker.gg",
            BASIC_KEY = "basic",
            STATS_KEY = "stats",
            METADATA_KEY = "metadata",
            WEAPONS_KEY = "weapon";

    protected static final String
            BASE_API_URL = "https://api.tracker.gg/api/v2/",
            DATA_KEY = "data",
            SEGMENTS_KEY = "segments",
            MW_PATH = "modern-warfare",
            CW_PATH = "cold-war",
            VALORANT = "valorant";

    public enum TRACKER_PLATFORM {
        BATTLE,
        XBOX,
        PSN,
        RIOT,
        NONE;

        /**
         * Get a tracker platform by COD platform
         *
         * @param codPlatform COD platform
         * @return Tracker platform
         */
        public static TRACKER_PLATFORM fromCodPlatform(PLATFORM codPlatform) {
            switch(codPlatform) {
                case BATTLE:
                    return BATTLE;
                case XBOX:
                    return XBOX;
                case PSN:
                    return PSN;
                default:
                    return NONE;
            }
        }

        /**
         * Get the name of the platform as used by cod.tracker.gg
         *
         * @return Platform name
         */
        public String getTrackerName() {
            switch(this) {
                case XBOX:
                    return "xbl";
                case BATTLE:
                    return "battlenet";
                default:
                    return this.name().toLowerCase();
            }
        }
    }

    /**
     * Get a player's match history JSON for a specific game from tracker.gg.
     * This is the last 20 matches in an array.
     *
     * @param name            Player name
     * @param platform        Player platform
     * @param gamePath        URL path to game API - e.g "modern-warfare"
     * @param parameterString Optional parameter String - e.g "?type=mp"
     * @return Player match history JSON or null (if an error occurs)
     */
    @Nullable
    protected static JSONArray getMatchHistoryJson(String name, TRACKER_PLATFORM platform, String gamePath, String... parameterString) {
        String url = BASE_API_URL + gamePath + "/standard/matches/" + getPlayerPath(name, platform);

        // Append parameterString
        if(parameterString.length != 0) {
            url = url + parameterString[0];
        }

        NetworkResponse response = new NetworkRequest(url, false).get();

        // No response from tracker
        if(response.code != 200) {
            return null;
        }

        return new JSONObject(response.body).getJSONObject(DATA_KEY).getJSONArray("matches");
    }

    /**
     * Get the URL used for fetching a player's stats from the API.
     *
     * @param name     Player name - e.g "ogrelover#2519"
     * @param platform Player platform - e.g BATTLE
     * @param gamePath URL path to game API - e.g "modern-warfare"
     * @return Player stats API URL - e.g "https://api.tracker.gg/api/v2/modern-warfare/standard/profile/battlenet/ogrelover%232519/"
     */
    protected static String getStatsUrl(String name, TRACKER_PLATFORM platform, String gamePath) {
        return BASE_API_URL + gamePath + "/standard/profile/" + getPlayerPath(name, platform);
    }

    /**
     * Get the URL to view a player's stats for a specific game on tracker.gg in a browser.
     *
     * @param domain   Tracker domain e.g "tracker.gg" or "cod.tracker.gg"
     * @param name     Player name - e.g "ogrelover#2519"
     * @param platform Player platform - e.g BATTLE
     * @param gamePath URL path to game API - e.g "modern-warfare"
     * @return Player profile URL - e.g "https://tracker.gg/modern-warfare/profile/battlenet/ogrelover%232519/"
     */
    protected static String getProfileUrl(String domain, String name, TRACKER_PLATFORM platform, String gamePath) {
        return "https://" + domain + "/" + gamePath + "/profile/" + getPlayerPath(name, platform);
    }

    /**
     * Get the relative profile path for the given name and platform.
     * This is appended to player API endpoints.
     *
     * @param name     Player name - e.g "ogrelover#2519"
     * @param platform Player platform - e.g BATTLE
     * @return Relative profile path - e.g "battlenet/ogrelover%232519/"
     */
    private static String getPlayerPath(String name, TRACKER_PLATFORM platform) {
        return platform.getTrackerName() + "/" + CODAPI.encodeName(name) + "/";
    }

    /**
     * Get a player's stats JSON for a segment of a specific game from tracker.gg.
     * A segment is typically an aspect of the game e.g killstreaks or weapons.
     *
     * @param name            Player name
     * @param platform        Player platform
     * @param gamePath        URL path to game API - e.g "modern-warfare"
     * @param segmentName     Segment name e.g "killstreaks"
     * @param parameterString Optional parameter String - e.g "?playlist=unranked"
     * @return Player stats JSON for segment or null (if an error occurs)
     */
    @Nullable
    protected static JSONObject getPlayerSegmentJson(String name, TRACKER_PLATFORM platform, String gamePath, String segmentName, String... parameterString) {

        // https://api.tracker.gg/api/v2/valorant/standard/profile/riot/The%20Bread%20Guy%23OCE/segments/weapon
        String segmentUrl = getStatsUrl(name, platform, gamePath) + "segments/" + segmentName;

        // Append parameterString
        if(parameterString.length != 0) {
            segmentUrl = segmentUrl + parameterString[0];
        }

        NetworkResponse segmentResponse = new NetworkRequest(segmentUrl, false).get();

        // No response from tracker
        if(segmentResponse.code != 200) {
            return null;
        }

        return new JSONObject(segmentResponse.body);
    }

    /**
     * Get a player's profile response for a specific game from tracker.gg.
     * This typically contains player details & an overview of stats.
     * Further stats are found in segments which can be
     * retrieved via {@link #getPlayerSegmentJson(String, TRACKER_PLATFORM, String, String, String...)}
     * The response will also indicate whether the player exists.
     *
     * @param name            Player name
     * @param platform        Player platform
     * @param gamePath        URL path to game API - e.g "modern-warfare"
     * @param parameterString Optional parameter String - e.g "?playlist=unranked"
     * @return Player profile response
     */
    protected static NetworkResponse getPlayerProfileResponse(String name, TRACKER_PLATFORM platform, String gamePath, String... parameterString) {

        // https://api.tracker.gg/api/v2/valorant/standard/profile/riot/The%20Bread%20Guy%23OCE
        String profileUrl = getStatsUrl(name, platform, gamePath);

        // Append parameterString
        if(parameterString.length != 0) {
            profileUrl = profileUrl + parameterString[0];
        }

        return new NetworkRequest(profileUrl, false).get();
    }
}
