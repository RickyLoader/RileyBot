package COD.API.Parsing;

import COD.API.CODManager;
import COD.API.CODStatsManager.PLATFORM;
import COD.Assets.*;
import COD.Match.*;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;

/**
 * Parsing player data from COD API JSON
 */
public class CODAPIParser<T extends CODManager> extends CODParser<T> {
    private static final String RESULT_KEY = "result";

    /**
     * Create the stats parser
     *
     * @param manager COD resource manager
     */
    public CODAPIParser(T manager) {
        super(manager);
    }

    /**
     * Parse the match history API JSON in to an object.
     *
     * @param name             Name of player which match history belongs to
     * @param platform         Player platform
     * @param matchHistoryData Match history JSON data
     * @return Player match history
     */
    public MatchHistory parseMatchHistory(String name, PLATFORM platform, JSONObject matchHistoryData) {
        ArrayList<MatchStats> matchStats = new ArrayList<>();

        JSONArray matchList = matchHistoryData.getJSONArray("matches");
        JSONObject summary = matchHistoryData.getJSONObject("summary").getJSONObject("all");

        for(int i = 0; i < matchList.length(); i++) {
            matchStats.add(parseMatchStats(name, platform, matchList.getJSONObject(i)));
        }
        return new MatchHistory(
                name,
                matchStats,
                new Ratio(
                        summary.getInt("kills"),
                        summary.getInt("deaths")
                ),
                getManager().getFont()
        );
    }

    /**
     * Parse the match stats API JSON in to an object
     *
     * @param name           Name of player which match history belongs to
     * @param platform       Player platform
     * @param matchStatsData Match stats JSON data
     * @return Match stats
     */
    public MatchStats parseMatchStats(String name, PLATFORM platform, JSONObject matchStatsData) {
        MatchPlayer player = parseMatchPlayer(matchStatsData, new MatchPlayer.MatchPlayerBuilder(name, platform));

        return new MatchStats(
                matchStatsData.getString("matchID"),
                getManager().getMapByCodename(matchStatsData.getString("map")),
                getManager().getModeByCodename(matchStatsData.getString("mode")),
                new Date(matchStatsData.getLong("utcStartSeconds") * 1000),
                new Date(matchStatsData.getLong("utcEndSeconds") * 1000),
                player,
                new Score(
                        matchStatsData.getInt(TEAM_1_SCORE_KEY),
                        matchStatsData.getInt(TEAM_2_SCORE_KEY),
                        parseMatchResult(matchStatsData)
                )
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
        JSONObject player = matchData.getJSONObject("player");
        JSONObject playerStats = matchData.getJSONObject("playerStats");

        builder
                .setTimePlayed(playerStats.getLong(TIME_PLAYED_KEY) * 1000)
                .setKD(
                        new Ratio(
                                playerStats.getInt(KILLS_KEY),
                                playerStats.getInt(DEATHS_KEY)
                        )
                )
                .setAccuracy(
                        playerStats.has(SHOTS_LANDED_KEY)
                                ? new Ratio(
                                playerStats.getInt(SHOTS_LANDED_KEY),
                                playerStats.getInt(SHOTS_FIRED_KEY)
                        )
                                : null
                )
                .setTeam(getOptionalString(player, TEAM_KEY))
                .setUno(getOptionalString(player, "uno"))
                .setNemesis(getOptionalString(player, "nemesis"))
                .setMostKilled(getOptionalString(player, "mostKilled"))
                .setLongestStreak(getLongestStreak(playerStats))
                .setDamageDealt(getDamageDealt(playerStats))
                .setDamageReceived(getOptionalInt(playerStats, DAMAGE_TAKEN_KEY))
                .setXP(getOptionalInt(playerStats, MATCH_XP_KEY))
                .setDistanceTravelled(getOptionalInt(playerStats, DISTANCE_TRAVELED_KEY))
                .setLoadouts(parseLoadouts(player.getJSONArray("loadout")));

        if(playerStats.has(PERCENT_TIME_MOVING_KEY)) {
            builder.setPercentTimeMoving(playerStats.getDouble(PERCENT_TIME_MOVING_KEY));
        }

        return builder.build();
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
        return playerStats.has(key) ? playerStats.getInt(key) : 0;
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
        return playerStats.has(key) && !playerStats.getString(key).isEmpty()
                ? playerStats.getString(key)
                : MatchPlayer.UNAVAILABLE;
    }

    /**
     * Get the damage dealt value from the match JSON
     * The key varies by game
     *
     * @param playerStats Player stats match JSON
     * @return Damage dealt
     */
    private int getDamageDealt(JSONObject playerStats) {
        return Math.max(
                getOptionalInt(playerStats, DAMAGE_DONE_KEY),
                getOptionalInt(playerStats, DAMAGE_DEALT_KEY)
        );
    }

    /**
     * Get the longest streak value from the match JSON
     * The key varies by game
     *
     * @param playerStats Player stats match JSON
     * @return Longest streak
     */
    private int getLongestStreak(JSONObject playerStats) {
        return Math.max(
                getOptionalInt(playerStats, LONGEST_STREAK_KEY),
                getOptionalInt(playerStats, HIGHEST_STREAK_KEY)
        );
    }

    /**
     * Parse the result of the match from the given match JSON.
     *
     * @param matchData JSON data for a match
     * @return Match result
     */
    private MatchStats.RESULT parseMatchResult(JSONObject matchData) {

        // If the player left the game or no result is provided, the match result is a forfeit
        if(!matchData.getBoolean(IS_PRESENT_KEY) || matchData.isNull(RESULT_KEY)) {
            return MatchStats.RESULT.FORFEIT;
        }

        final String result = matchData.getString(RESULT_KEY);

        switch(result.toLowerCase()) {
            case "win":
                return MatchStats.RESULT.WIN;
            case "loss":
            case "lose":
                return MatchStats.RESULT.LOSS;
            default:
                return MatchStats.RESULT.DRAW;
        }
    }

    /**
     * Parse the provided loadout JSON into an array of loadouts
     *
     * @param loadoutList JSON loadout array
     * @return Array of player loadouts
     */
    private Loadout[] parseLoadouts(JSONArray loadoutList) {
        ArrayList<Loadout> loadouts = new ArrayList<>();
        for(int i = 0; i < loadoutList.length(); i++) {
            JSONObject loadoutData = loadoutList.getJSONObject(i);
            loadouts.add(
                    new Loadout.LoadoutBuilder()
                            .setPrimaryWeapon(parseLoadoutWeapon(loadoutData.getJSONObject("primaryWeapon")))
                            .setSecondaryWeapon(parseLoadoutWeapon(loadoutData.getJSONObject("secondaryWeapon")))
                            .setPerks(parsePerks(loadoutData.getJSONArray("perks")))
                            .setLethalEquipment(
                                    parseWeapon(
                                            loadoutData.getJSONObject("lethal"),
                                            Weapon.CATEGORY.LETHALS
                                    )
                            )
                            .setTacticalEquipment(
                                    (TacticalWeapon) parseWeapon(
                                            loadoutData.getJSONObject("tactical"),
                                            Weapon.CATEGORY.TACTICALS
                                    )
                            )
                            .build()
            );
        }
        return loadouts.stream().distinct().toArray(Loadout[]::new);
    }

    /**
     * Parse an array of perks from the match loadout JSON
     *
     * @param perkJSONArray JSON array of perks
     * @return Array of perks
     */
    private Perk[] parsePerks(JSONArray perkJSONArray) {
        ArrayList<Perk> perks = new ArrayList<>();
        for(int i = 0; i < perkJSONArray.length(); i++) {
            String codename = perkJSONArray.getJSONObject(i).getString("name");
            if(codename.equals("specialty_null")) {
                continue;
            }
            perks.add(getManager().getPerkByCodename(codename));
        }
        return perks.toArray(new Perk[0]);
    }

    /**
     * Parse a weapon from the match loadout weapon JSON
     *
     * @param loadoutWeapon Match loadout weapon JSON
     * @param category      Weapon category
     * @return Weapon
     */
    private Weapon parseWeapon(JSONObject loadoutWeapon, Weapon.CATEGORY category) {
        String codename = loadoutWeapon.getString("name");
        if(codename.equals("none")) {
            return null;
        }
        if(category == Weapon.CATEGORY.UNKNOWN) {
            category = Weapon.getCategoryFromWeaponCodename(codename);
        }
        return getManager().getWeaponByCodename(codename, category);
    }

    /**
     * Parse a loadout weapon from the match loadout weapon JSON
     *
     * @param loadoutWeapon Match loadout weapon JSON
     * @return Loadout weapon containing weapon & attachments
     */
    private LoadoutWeapon parseLoadoutWeapon(JSONObject loadoutWeapon) {
        Weapon weapon = parseWeapon(loadoutWeapon, Weapon.CATEGORY.UNKNOWN);
        if(weapon == null) {
            return null;
        }
        ArrayList<Attachment> attachments = new ArrayList<>();
        if(loadoutWeapon.has("attachments")) {
            JSONArray attachmentData = loadoutWeapon.getJSONArray("attachments");
            for(int i = 0; i < attachmentData.length(); i++) {
                String attachmentName = attachmentData.getJSONObject(i).getString("name");
                if(attachmentName.equals("none")) {
                    continue;
                }
                Attachment attachment = weapon.getAttachmentByCodename(attachmentName);
                if(attachment == null) {
                    attachment = new Attachment(
                            attachmentName,
                            "MISSING: " + attachmentName,
                            Attachment.CATEGORY.UNKNOWN,
                            Attachment.CATEGORY.UNKNOWN,
                            null,
                            null
                    );
                }
                attachments.add(attachment);
            }
        }
        return new LoadoutWeapon(weapon, attachments, loadoutWeapon.getInt("variant"));
    }
}
