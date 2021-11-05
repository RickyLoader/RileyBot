package TrackerGG;

import Network.NetworkResponse;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * API functions for valorant on tracker.gg
 */
public class ValorantTrackerAPI extends TrackerAPI {
    public static String
            PROFILE_KEY = "profile",
            AGENTS_KEY = "agent";

    /**
     * Valorant playlist
     */
    public enum PLAYLIST {
        COMPETITIVE("competitive", "Competitive"),
        DEATHMATCH("deathmatch", "Deathmatch"),
        ESCALATION("escalation", "Escalation"),
        REPLICATION("replication", "Replication"),
        SNOWBALL_FIGHT("snowball", "Snowball Fight"),
        SPIKE_RUSH("spikerush", "Spike Rush"),
        UNRATED("unrated", "Unrated");

        private final String trackerName, actualName;

        /**
         * Create a Valorant playlist - e.g SNOWBALL_FIGHT
         *
         * @param trackerName Name to use with the API e.g "snowball"
         * @param actualName  Actual in-game name - e.g "Snowball Fight"
         */
        PLAYLIST(String trackerName, String actualName) {
            this.trackerName = trackerName;
            this.actualName = actualName;
        }

        /**
         * Get a String list of the available playlists, new line separated and tab indented.
         *
         * @return List of available playlists
         */
        public static String getHelpText() {
            StringBuilder builder = new StringBuilder();
            PLAYLIST[] playlists = PLAYLIST.values();

            for(int i = 0; i < playlists.length; i++) {
                builder.append("\t").append(playlists[i].getActualName());

                // Don't add new line to final playlist
                if(i != playlists.length - 1) {
                    builder.append("\n");
                }
            }

            return builder.toString();
        }

        /**
         * Get the In-game name - e.g "Snowball Fight"
         *
         * @return In-game name
         */
        public String getActualName() {
            return actualName;
        }

        /**
         * Check if the given name is the name of a playlist
         *
         * @param name Playlist name
         * @return Name is of a playlist
         */
        public static boolean isPlaylist(String name) {
            return byActualName(name) != null;
        }

        /**
         * Get a playlist by in-game name ({@code actualName})
         *
         * @param name Name of playlist e.g "Spike Rush"
         * @return Playlist - e.g SPIKE or null
         */
        @Nullable
        public static PLAYLIST byActualName(String name) {
            for(PLAYLIST playlist : PLAYLIST.values()) {
                if(playlist.actualName.equalsIgnoreCase(name)) {
                    return playlist;
                }
            }
            return null;
        }

        /**
         * Get a playlist by the tracker name ({@code trackerName})
         *
         * @param name Tracker name e.g "spikerush"
         * @return Playlist - e.g SPIKE_RUSH
         */
        public static PLAYLIST byTrackerName(String name) {
            for(PLAYLIST playlist : PLAYLIST.values()) {
                if(playlist.trackerName.equalsIgnoreCase(name)) {
                    return playlist;
                }
            }
            return null;
        }
    }

    /**
     * Get a Valorant player's stats JSON from tracker.gg, this aggregates multiple requests for agents, weapons, etc.
     *
     * @param name     Player name e.g "dave#123"
     * @param playlist Playlist to retrieve stats for (there are no overall stats, only per playlist)
     * @return Player stats JSON
     * @throws ValorantTrackerException When the stats are unable to be retrieved
     */
    public static JSONObject getPlayerStatsJson(String name, PLAYLIST playlist) throws ValorantTrackerException {
        NetworkResponse profileResponse = getPlayerProfileResponse(
                name,
                TRACKER_PLATFORM.RIOT,
                TrackerAPI.VALORANT,
                "?forceCollect=true"
        );

        // No response from tracker
        if(profileResponse.code != 200) {
            throw new ValorantTrackerException(getErrorFromResponse(name, profileResponse));
        }

        // Basic stats overview & profile info
        JSONObject profile = new JSONObject(profileResponse.body).getJSONObject(DATA_KEY);
        JSONArray segments = profile.getJSONArray(SEGMENTS_KEY);

        /*
         * Find the requested playlist overview - the segments array contains playlist overviews,
         * recent match results, and agent stats for the player's "default playlist" (most played playlist?).
         * Playlist overviews are absent if the player hasn't played it.
         */
        JSONObject requestedPlaylistOverview = null;
        for(int i = 0; i < segments.length(); i++) {
            JSONObject segment = segments.getJSONObject(i);
            if(!segment.getString("type").equals("playlist")) {
                continue;
            }

            // "spikerush"
            final String playlistName = segment.getJSONObject("attributes").getString("key");

            // "spikerush" -> SPIKE_RUSH
            if(PLAYLIST.byTrackerName(playlistName) == playlist) {
                requestedPlaylistOverview = segment;
            }
        }

        // Player doesn't have stats for the requested playlist
        if(requestedPlaylistOverview == null) {
            throw new ValorantTrackerException("This player has not played any " + playlist.getActualName() + "!");
        }

        // No endpoint for retrieving all weapon/agent stats, only by playlist
        final String playlistParam = "?playlist=" + playlist.trackerName;

        // Remove segments and replace with requested playlist stats
        profile.remove(SEGMENTS_KEY);
        profile.put(STATS_KEY, requestedPlaylistOverview);

        JSONObject weapons = getPlayerSegmentJson(name, WEAPONS_KEY, playlistParam);
        JSONObject agents = getPlayerSegmentJson(name, AGENTS_KEY, playlistParam);

        return new JSONObject()
                .put(PROFILE_KEY, profile)
                .put(
                        WEAPONS_KEY,
                        weapons == null ? new JSONObject() : refactorSegmentArray(weapons.getJSONArray(DATA_KEY))
                )
                .put(
                        AGENTS_KEY,
                        agents == null ? new JSONObject() : refactorSegmentArray(agents.getJSONArray(DATA_KEY))
                );
    }

    /**
     * Take a segment array and refactor it to be a JSON object with each item's ID as the key to its stats.
     *
     * @param segmentArray JSON segment array
     * @return JSON segment object
     */
    private static JSONObject refactorSegmentArray(JSONArray segmentArray) {
        JSONObject result = new JSONObject();

        for(int i = 0; i < segmentArray.length(); i++) {
            JSONObject segment = segmentArray.getJSONObject(i);
            final String id = segment.getJSONObject(METADATA_KEY).getString("name").toLowerCase();
            result.put(id, segment.getJSONObject(STATS_KEY));
        }

        return result;
    }

    /**
     * Parse the error message from the given failed API request.
     *
     * @param name          Name used to trigger error response
     * @param errorResponse Unsuccessful API response
     * @return Error message
     */
    private static String getErrorFromResponse(String name, NetworkResponse errorResponse) {
        try {
            JSONObject error = new JSONObject(errorResponse.body)
                    .getJSONArray("errors")
                    .getJSONObject(0);

            // e.g CollectorResultStatus::Private -> PRIVATE
            final String code = error.getString("code")
                    .replaceFirst("CollectorResultStatus::", "")
                    .toUpperCase();

            // Rewrite error message
            String errorMessage;
            switch(code) {
                case "PRIVATE":
                    errorMessage = "This profile is private. The player will have to visit this URL"
                            + " and give permission to slobber up their stats.\n\n" + getProfileUrl(name);
                    break;
                case "NOTFOUND":
                    errorMessage = "This profile does not exist!";
                    break;
                default:
                    errorMessage = error.getString("message");
            }

            return errorMessage;
        }

        // Response may not contain JSON (tracker down)
        catch(JSONException e) {
            return "The tracker didn't respond, maybe give it another poke in a minute.";
        }
    }

    /**
     * Get a player's stats JSON for a segment of Valorant from tracker.gg.
     *
     * @param name            Player name
     * @param segmentName     Segment name e.g "killstreaks"
     * @param parameterString Optional parameter String - e.g "?playlist=unranked"
     * @return Player stats JSON for segment or null (if an error occurs)
     */
    @Nullable
    protected static JSONObject getPlayerSegmentJson(String name, String segmentName, String... parameterString) {
        return getPlayerSegmentJson(name, TRACKER_PLATFORM.RIOT, VALORANT, segmentName, parameterString);
    }

    /**
     * Get the URL to view a player's Valorant stats on tracker.gg in a browser.
     *
     * @param name Player name - e.g "the bread guy#oce"
     * @return Player profile URL - e.g "https://tracker.gg/valorant/profile/riot/the+bread+guy%23oce/"
     */
    protected static String getProfileUrl(String name) {
        return TrackerAPI.getProfileUrl(DEFAULT_DOMAIN, name, TRACKER_PLATFORM.RIOT, VALORANT);
    }

    /**
     * Check if the given name is in the valid format for retrieving Valorant stats.
     * Valorant uses a "name#tagline" format where the name & tagline are not individually
     * unique however the combination is.
     * They will transition all Riot games to use this format at some point, however by default if the player has not
     * set a tagline, their tagline is their region name - e.g "OCE".
     *
     * @param name Player Valorant name - e.g "the bread guy#oce"
     * @return Name is valid
     */
    public static boolean isValidName(String name) {
        return name.split("#").length == 2;
    }

    /**
     * Exception to throw when Valorant stats are unable to be retrieved
     */
    public static class ValorantTrackerException extends Exception {
        public ValorantTrackerException(String message) {
            super(message);
        }
    }
}
