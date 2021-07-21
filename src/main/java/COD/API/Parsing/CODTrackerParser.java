package COD.API.Parsing;

import COD.API.CODManager;
import COD.API.CODStatsManager;
import COD.Assets.Ratio;
import COD.Match.MatchHistory;
import COD.Match.MatchPlayer;
import COD.Match.MatchStats;
import COD.Match.Score;
import org.json.JSONArray;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Parsing player data from cod.tracker.gg JSON
 */
public class CODTrackerParser<T extends CODManager> extends CODParser<T> {
    static final String
            ATTRIBUTES_KEY = "attributes",
            STATS_KEY = "stats",
            METADATA_KEY = "metadata",
            SEGMENTS_KEY = "segments",
            RESULT_KEY = "hasWon",
            VALUE_KEY = "value";

    /**
     * Create the stats parser
     *
     * @param manager COD resource manager
     */
    public CODTrackerParser(T manager) {
        super(manager);
    }

    /**
     * Stats are stored in a JSON object containing an object for each stat - e.g kills, deaths, etc.
     * Retrieve the integer value for the given stat key.
     *
     * @param statsData Stats
     * @param statKey   Stat to retrieve - e.g "kills"
     * @return Stat value - e.g 100
     */
    public int getIntValue(JSONObject statsData, String statKey) {
        return statsData.getJSONObject(statKey).getInt(VALUE_KEY);
    }

    /**
     * Parse the match history tracker JSON in to an object.
     *
     * @param name             Name of player which match history belongs to
     * @param platform         Player platform
     * @param matchHistoryData Match history JSON data
     * @return Player match history
     */
    public MatchHistory parseMatchHistory(String name, CODStatsManager.PLATFORM platform, JSONArray matchHistoryData) {
        ArrayList<MatchStats> matchStats = new ArrayList<>();
        int kills = 0;
        int deaths = 0;

        for(int i = 0; i < matchHistoryData.length(); i++) {
            JSONObject matchData = matchHistoryData.getJSONObject(i);
            JSONObject attributes = matchData.getJSONObject(ATTRIBUTES_KEY);
            JSONObject metadata = matchData.getJSONObject(METADATA_KEY);

            Date startDate = parseDate(metadata.getString("timestamp"));
            long duration = metadata.getJSONObject("duration").getLong(VALUE_KEY);

            MatchPlayer player = parseMatchPlayer(matchData, new MatchPlayer.MatchPlayerBuilder(name, platform));
            kills += player.getKills();
            deaths += player.getDeaths();

            matchStats.add(
                    new MatchStats(
                            String.valueOf(attributes.get("id")),
                            getManager().getMapByCodename(attributes.getString("mapId")),
                            getManager().getModeByCodename(attributes.getString("modeId")),
                            startDate,
                            new Date(startDate.getTime() + duration),
                            player,
                            new Score(
                                    metadata.getInt(TEAM_1_SCORE_KEY),
                                    metadata.getInt(TEAM_2_SCORE_KEY),
                                    parseMatchResult(matchData)
                            )
                    )
            );
        }

        return new MatchHistory(
                name,
                matchStats,
                new Ratio(kills, deaths),
                getManager().getFont()
        );
    }

    /**
     * Parse a match player from the given match JSON
     *
     * @param matchData Match JSON
     * @param builder   Player builder initialised with name and platform
     * @return Match player
     */
    private MatchPlayer parseMatchPlayer(JSONObject matchData, MatchPlayer.MatchPlayerBuilder builder) {
        JSONObject playerOverview = matchData
                .getJSONArray(SEGMENTS_KEY)
                .getJSONObject(0);

        JSONObject playerStats = playerOverview.getJSONObject(STATS_KEY);

        builder
                .setTimePlayed(getIntValue(playerStats, TIME_PLAYED_KEY) * 1000L)
                .setKD(
                        new Ratio(
                                getIntValue(playerStats, KILLS_KEY),
                                getIntValue(playerStats, DEATHS_KEY)
                        )
                )
                .setAccuracy(
                        playerStats.has(SHOTS_LANDED_KEY)
                                ? new Ratio(
                                getIntValue(playerStats, SHOTS_LANDED_KEY),
                                getIntValue(playerStats, SHOTS_FIRED_KEY)
                        )
                                : null
                )
                .setTeam(getOptionalString(playerStats, TEAM_KEY))

                // Values not provided
                .setUno(MatchPlayer.UNAVAILABLE)
                .setNemesis(MatchPlayer.UNAVAILABLE)
                .setMostKilled(MatchPlayer.UNAVAILABLE)

                .setLongestStreak(getLongestStreak(playerStats))
                .setDamageDealt(getDamageDealt(playerStats))
                .setDamageReceived(getOptionalInt(playerStats, DAMAGE_TAKEN_KEY))
                .setXP(getOptionalInt(playerStats, MATCH_XP_KEY))
                .setDistanceTravelled(getOptionalInt(playerStats, DISTANCE_TRAVELED_KEY));

        if(playerStats.has(PERCENT_TIME_MOVING_KEY)) {
            builder.setPercentTimeMoving(playerStats.getJSONObject(PERCENT_TIME_MOVING_KEY).getDouble(VALUE_KEY));
        }

        return builder.build();
    }

    /**
     * Get the longest streak value from the match JSON
     * The key varies by game
     *
     * @param playerStats Player stats match JSON
     * @return Longest streak
     */
    private int getLongestStreak(JSONObject playerStats) {
        return getIntValue(playerStats, playerStats.has(LONGEST_STREAK_KEY) ? LONGEST_STREAK_KEY : HIGHEST_STREAK_KEY);
    }

    /**
     * Get the damage dealt value from the match JSON
     * The key varies by game
     *
     * @param playerStats Player stats match JSON
     * @return Damage dealt
     */
    private int getDamageDealt(JSONObject playerStats) {
        return getIntValue(playerStats, playerStats.has(DAMAGE_DEALT_KEY) ? DAMAGE_DEALT_KEY : DAMAGE_DONE_KEY);
    }

    /**
     * Get an optional integer value from the player stats match JSON
     * Return 0 if absent
     *
     * @param playerStats Player stats match JSON
     * @param key         Value key
     * @return Value or 0
     */
    private int getOptionalInt(JSONObject playerStats, String key) {
        return playerStats.has(key) ? getIntValue(playerStats, key) : 0;
    }

    /**
     * Get an optional String value from the player stats match JSON
     * Return '-' if absent
     *
     * @param playerStats Player stats match JSON
     * @param key         Value key
     * @return Value or '-'
     */
    private String getOptionalString(JSONObject playerStats, String key) {
        if(playerStats.has(key) && !playerStats.getJSONObject(key).getString(VALUE_KEY).isEmpty()) {
            return playerStats.getJSONObject(key).getString(VALUE_KEY);
        }
        return MatchPlayer.UNAVAILABLE;
    }

    /**
     * Parse the result of the match from the given match JSON.
     *
     * @param matchData JSON data for a match
     * @return Match result
     */
    private MatchStats.RESULT parseMatchResult(JSONObject matchData) {
        JSONObject playerMetadata = matchData
                .getJSONArray(SEGMENTS_KEY)
                .getJSONObject(0)
                .getJSONObject(METADATA_KEY);

        // If the player left the game or no result is provided, the match result is a forfeit
        if(!playerMetadata.getBoolean(IS_PRESENT_KEY) || playerMetadata.isNull(RESULT_KEY)) {
            return MatchStats.RESULT.FORFEIT;
        }

        // They don't provide a draw result?
        return playerMetadata.getBoolean(RESULT_KEY) ? MatchStats.RESULT.WIN : MatchStats.RESULT.LOSS;
    }

    /**
     * Attempt to parse the given tracker date String to a date.
     *
     * @param dateString Tracker date String - e.g "2021-07-17T12:05:33+00:00"
     * @return Date from date String or current date (if unable to parse)
     */
    private Date parseDate(String dateString) {
        try {
            return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX").parse(dateString);
        }
        catch(ParseException e) {
            return new Date();
        }
    }
}
