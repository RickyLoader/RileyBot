package Runescape.Hiscores;

import Network.NetworkRequest;
import Network.NetworkResponse;
import Runescape.OSRS.League.LeagueTier;
import Runescape.Stats.PlayerStats;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

import static Network.NetworkResponse.TIMEOUT_CODE;
import static Runescape.Stats.PlayerStats.ACCOUNT.*;

/**
 * Wise Old Man OSRS tracker API functions
 */
public class WiseOldMan {
    private final HashMap<String, Long> lastUpdated;
    private final TrackerResponse apiDownResponse;
    private static final HashSet<PlayerStats.ACCOUNT> SUPPORTED_ACCOUNT_TYPES = new HashSet<>(
            Arrays.asList(
                    NORMAL,
                    IRON,
                    ULTIMATE,
                    HARDCORE,
                    LOCATE
            )
    );

    private static final String
            API_ENTRY = "api/",
            PLAYER_DATA_ENTRY = API_ENTRY + "players/";
    private static WiseOldMan instance;

    /**
     * Initialise a map of player name -> last updated
     */
    private WiseOldMan() {
        this.lastUpdated = new HashMap<>();
        this.apiDownResponse = new TrackerResponse("Tracker didn't respond!");
    }

    /**
     * Get an instance of the WiseOldMan class
     *
     * @return Instance
     */
    public static WiseOldMan getInstance() {
        if(instance == null) {
            instance = new WiseOldMan();
        }
        return instance;
    }

    /**
     * Check if the given account type is supported by Wise Old Man.
     * Certain temporary/seasonal account types are unsupported.
     * Unsupported account types will still return data from Wise Old Man,
     * but it will pertain to the player's normal account type.
     * <p>
     * e.g If a Hardcore Ironman is playing DMM, you cannot view the player's DMM XP gains, what will be returned
     * is their normal (Hardcore Ironman) XP gains.
     *
     * @param accountType Account type to check
     * @return Account type is supported
     */
    public static boolean isSupportedAccountType(PlayerStats.ACCOUNT accountType) {
        return SUPPORTED_ACCOUNT_TYPES.contains(accountType);
    }

    /**
     * Get the achievements data for the given player.
     * If the player is not currently tracked begin tracking them.
     *
     * @param name   Player name to fetch XP tracker data for
     * @param league Get the XP tracker for the seasonal league mode
     * @return XP tracker response data or null (if tracker is down)
     */
    public TrackerResponse getPlayerAchievementsData(String name, boolean league) {
        try {
            updateTracker(name, league);

            NetworkResponse achievementsResponse = new NetworkRequest(
                    getBaseStatsUrl(name, league) + "achievements/progress",
                    false
            ).get();

            // API down
            if(achievementsResponse.code == TIMEOUT_CODE) {
                return apiDownResponse;
            }

            return new TrackerResponse(
                    new JSONObject().put(TrackerResponse.ACHIEVEMENTS_KEY, new JSONArray(achievementsResponse.body))
            );
        }
        catch(Exception e) {
            return new TrackerResponse("Failed to fetch achievements!");
        }
    }

    /**
     * Calculate a player's league tier manually from the given rank
     *
     * @param rank Player league point rank
     * @return League tier
     */
    public LeagueTier.LEAGUE_TIER calculateLeagueTier(long rank) {
        LeagueTier.LEAGUE_TIER tier = LeagueTier.LEAGUE_TIER.UNQUALIFIED;
        try {
            final String json = new NetworkRequest(
                    getTrackerDomain(true) + API_ENTRY + "/league/tiers",
                    false
            ).get().body;

            if(json == null) {
                throw new Exception();
            }

            JSONArray tiers = new JSONArray(json);
            for(int i = 0; i < tiers.length(); i++) {
                JSONObject tierInfo = tiers.getJSONObject(i);
                if(rank > tierInfo.getLong("threshold")) {
                    break;
                }
                tier = LeagueTier.LEAGUE_TIER.valueOf(tierInfo.getString("name").toUpperCase());
            }

            return tier;
        }
        catch(Exception e) {
            return tier;
        }
    }

    /**
     * Get the weekly XP tracker data for the given player.
     * If the player is not currently tracked begin tracking them.
     *
     * @param name   Player name to fetch XP tracker data for
     * @param league Get the XP tracker for the seasonal league mode
     * @return XP tracker response data or null (if tracker is down)
     */
    public TrackerResponse getXpTrackerData(String name, boolean league) {
        try {
            updateTracker(name, league);
            final String baseUrl = getBaseStatsUrl(name, league);
            NetworkResponse xpTrackerResponse = new NetworkRequest(baseUrl + "gained", false).get();

            // API down
            if(xpTrackerResponse.code == TIMEOUT_CODE) {
                return apiDownResponse;
            }

            NetworkResponse xpRecordsResponse = new NetworkRequest(baseUrl + "records", false).get();

            // API down
            if(xpRecordsResponse.code == TIMEOUT_CODE) {
                return apiDownResponse;
            }

            JSONObject result = new JSONObject();
            result.put(TrackerResponse.XP_KEY, new JSONObject(xpTrackerResponse.body).getJSONObject("week"));
            result.put(TrackerResponse.RECORDS_KEY, new JSONArray(xpRecordsResponse.body));

            return new TrackerResponse(result);
        }
        catch(Exception e) {
            return new TrackerResponse("Failed to fetch Weekly XP!");
        }
    }

    /**
     * Send a request to update the tracker for the given player.
     * This also begins tracking a player if they are not currently tracked.
     *
     * @param name   Player name
     * @param league Player is a league account
     */
    private void updateTracker(String name, boolean league) {
        long now = System.currentTimeMillis();
        final String nameKey = name.toLowerCase();
        Long lastUpdated = this.lastUpdated.get(nameKey);

        // Don't refresh if less than 2 minutes have passed since the last update
        if(lastUpdated != null && now - lastUpdated < 120000) {
            return;
        }

        this.lastUpdated.put(nameKey, now);

        new NetworkRequest(
                getTrackerDomain(league) + PLAYER_DATA_ENTRY + "track",
                false
        ).post(new JSONObject().put("username", name).toString());
    }

    /**
     * Get the domain to use for the tracker data
     *
     * @param league League XP tracker
     * @return Domain to use for tracker data e.g "https://wiseoldman.net/"
     */
    private String getTrackerDomain(boolean league) {
        String domain = "wiseoldman.net";
        return "https://" + (league ? "trailblazer." + domain : domain) + "/";
    }

    /**
     * Get the weekly XP tracker browser URL (the URL to view the tracker directly in a browser)
     *
     * @param name   Player name
     * @param league League XP tracker
     * @return XP tracker browser URL e.g "https://wiseoldman.net/players/player123/gained/skilling?period=week"
     */
    public String getXpTrackerBrowserUrl(String name, boolean league) {
        return getTrackerDomain(league)
                + "players/" + name.replaceAll(" ", "%20") + "/gained/skilling?period=week";
    }

    /**
     * Get the base player stats URL. Append an endpoint to this URL to retrieve different player
     * stats from the tracker.
     *
     * @param name   Player name
     * @param league Get league URL
     * @return Base Player tracker stats URL - e.g "https://wiseoldman.net/api/players/username/player123/"
     */
    private String getBaseStatsUrl(String name, boolean league) {
        return getTrackerDomain(league) + PLAYER_DATA_ENTRY + "username/" + name + "/";
    }

    /**
     * Data response from tracker
     */
    public static class TrackerResponse {
        private final JSONObject data;
        private final String error;

        public static final String
                XP_KEY = "xp",
                ACHIEVEMENTS_KEY = "achievements",
                RECORDS_KEY = "records";

        /**
         * Create a successful response
         *
         * @param data JSON response
         */
        public TrackerResponse(@NotNull JSONObject data) {
            this.data = data;
            this.error = null;
        }

        /**
         * Create a failed response
         *
         * @param error Error that occurred
         */
        public TrackerResponse(@NotNull String error) {
            this.data = null;
            this.error = error;
        }

        /**
         * Get the response data.
         * This may be null if no response was received
         *
         * @return Response data
         */
        @Nullable
        public JSONObject getData() {
            return data;
        }

        /**
         * Get the error message (if available)
         *
         * @return Error message
         */
        public String getError() {
            return error;
        }
    }
}
