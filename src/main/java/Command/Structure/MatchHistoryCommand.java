package Command.Structure;

import Bot.ResourceHandler;
import COD.*;
import COD.MWPlayer.Ratio;
import COD.Map;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;

/**
 * View a COD player's match history
 */
public abstract class MatchHistoryCommand extends CODLookupCommand {
    private final HashMap<String, String> modes, maps;
    private String matchID;
    private String win, loss, draw;

    /**
     * Create the command
     *
     * @param trigger Command trigger
     * @param res     Resource location
     */
    public MatchHistoryCommand(String trigger, String res) {
        super(
                trigger,
                "Have a gander at a player's match history!",
                getDefaultLookupArgs(trigger) + " [match id/latest]\n"
                        + getHelpText(trigger) + " [match id/latest]"
        );
        res = "/COD/" + res;
        this.modes = getItemMap(res + "modes.json", "modes");
        this.maps = getItemMap(res + "maps.json", "maps");
    }

    @Override
    public String stripArguments(String query) {
        query = setPlatform(query);
        String[] args = query.split(" ");

        if(args.length == 1) {
            return query;
        }

        String lastArg = args[args.length - 1];
        if(lastArg.matches("\\d+") || lastArg.equals("latest")) {
            matchID = lastArg;
            return fixName(query.replace(lastArg, "").trim());
        }
        matchID = null;
        return fixName(query);
    }

    /**
     * Parse map/mode names from local JSON
     *
     * @param location Location of file
     * @param key      JSON parent key
     * @return Map of code names -> real names
     */
    private HashMap<String, String> getItemMap(String location, String key) {
        HashMap<String, String> items = new HashMap<>();
        JSONObject itemData = new JSONObject(
                new ResourceHandler().getResourceFileAsString(location)
        ).getJSONObject(key);

        for(String iwName : itemData.keySet()) {
            items.put(
                    iwName,
                    itemData.getJSONObject(iwName).getString("real_name")
            );
        }
        return items;
    }

    @Override
    public void processName(String name, CommandContext context) {
        if(win == null) {
            EmoteHelper helper = context.getEmoteHelper();
            win = EmoteHelper.formatEmote(helper.getComplete());
            loss = EmoteHelper.formatEmote(helper.getFail());
            draw = EmoteHelper.formatEmote(helper.getDraw());
        }

        MessageChannel channel = context.getMessageChannel();
        MatchHistory matchHistory = getMatchHistory(name, getPlatform(), context.getEmoteHelper());
        if(matchHistory == null) {
            channel.sendMessage(
                    "I didn't find any match history for **"
                            + name
                            + "** on platform: **"
                            + getPlatform()
                            + "**, try another platform or learn how to spell.\n"
                            + getHelpNameCoded()
            ).queue();
            return;
        }
        if(matchID != null) {
            channel.sendMessage(getMatchEmbed(matchHistory)).queue();
            return;
        }
        getMatchHistoryEmbed(context, matchHistory).showMessage();
    }

    /**
     * Create a message embed detailing the provided match id
     *
     * @param matchHistory Match history of player
     * @return Message embed detailing match or error
     */
    private MessageEmbed getMatchEmbed(MatchHistory matchHistory) {
        Match match = matchID.equals("latest") ? matchHistory.getMatches().get(0) : matchHistory.getMatch(matchID);
        if(match == null) {
            return getErrorEmbed(matchHistory);
        }
        return getDefaultEmbedBuilder(matchHistory.getName().toUpperCase())
                .setColor(getResultColour(match.getResult()))
                .setImage(match.getMap().getImageURL())
                .addField("**Date**", match.getDateString(), true)
                .addField("**Time**", match.getTimeString(), true)
                .addField("**Duration**", match.getDurationString(), true)
                .addField("**Mode**", match.getMode(), true)
                .addField("**Map**", match.getMap().getName(), true)
                .addBlankField(true)
                .addField("**K/D**", match.getKillDeathSummary(), true)
                .addField("**Shots Fired/Hit**", match.getShotSummary(), true)
                .addField("**Accuracy**", match.getAccuracySummary(), true)
                .addField("**Damage Dealt**", match.getDamageDealt(), true)
                .addField("**Damage Taken**", match.getDamageReceived(), true)
                .addField("**Highest Streak**", String.valueOf(match.getLongestStreak()), true)
                .addField(
                        "**Distance Travelled**",
                        match.getWobblies() + "\n" + match.getDistanceTravelled(),
                        false
                )
                .addField("**Nemesis**", match.getNemesis(), true)
                .addField("**Most Killed**", match.getMostKilled(), true)
                .addBlankField(true)
                .addField("**Match XP**", match.getExperience(), true)
                .addField(
                        "**Result**",
                        match.getResult().toString() + " (" + match.getScore() + ") " + getResultEmote(match.getResult()),
                        true
                )
                .build();
    }

    /**
     * Get the colour to use in a Match embed based on the match result
     *
     * @param result Match result
     * @return Colour to use
     */
    public int getResultColour(Match.RESULT result) {
        switch(result) {
            case WIN:
                return EmbedHelper.GREEN;
            case LOSS:
                return EmbedHelper.RED;
            case DRAW:
                return EmbedHelper.YELLOW;
            default:
                return EmbedHelper.PURPLE;
        }
    }

    /**
     * Get the result formatted for use in a message embed with an emote and score
     *
     * @param match Match
     * @return Formatted result
     */
    public String getFormattedResult(Match match) {
        Match.RESULT result = match.getResult();
        return result.toString() + " " + getResultEmote(result) + "\n(" + match.getScore() + ")";
    }

    /**
     * Get the emote to use for the match result
     *
     * @param result Match result
     * @return Emote indicating the result of the match
     */
    public String getResultEmote(Match.RESULT result) {
        switch(result) {
            case WIN:
                return win;
            case LOSS:
                return loss;
            default:
                return draw;
        }
    }

    /**
     * Get an embed detailing an error for a non existent match
     *
     * @param matchHistory Player match history
     * @return Error embed
     */
    private MessageEmbed getErrorEmbed(MatchHistory matchHistory) {
        return getDefaultEmbedBuilder(matchHistory.getName().toUpperCase())
                .setColor(EmbedHelper.RED)
                .setDescription(
                        "No match found with id: **" + matchID + "**" +
                                " for player: **" + matchHistory.getName().toUpperCase() + "**"
                )
                .build();
    }

    /**
     * Create the default embed builder
     *
     * @param name Player name
     * @return Default embed builder
     */
    private EmbedBuilder getDefaultEmbedBuilder(String name) {
        return new EmbedBuilder()
                .setTitle(getSummaryEmbedTitle(name))
                .setFooter(
                        "Type " + getTrigger() + " for help"
                )
                .setThumbnail(getEmbedThumbnail());
    }

    /**
     * Get the title to use for the match summary embed
     *
     * @param name Player name
     * @return Embed title
     */
    public abstract String getSummaryEmbedTitle(String name);

    /**
     * Get the title to use for the match history embed
     *
     * @param name Player name
     * @return Embed title
     */
    public abstract String getHistoryEmbedTitle(String name);

    /**
     * Get the thumbnail to use in the embed
     *
     * @return Embed thumbnail
     */
    public abstract String getEmbedThumbnail();

    /**
     * Create the match history pageable embed
     *
     * @param context      Command context
     * @param matchHistory Player match history
     * @return Match history pageable embed
     */
    private PageableTableEmbed getMatchHistoryEmbed(CommandContext context, MatchHistory matchHistory) {
        return new PageableTableEmbed(
                context.getJDA(),
                context.getMessageChannel(),
                context.getEmoteHelper(),
                matchHistory.getMatches(),
                getEmbedThumbnail(),
                getHistoryEmbedTitle(matchHistory.getName().toUpperCase()),
                matchHistory.getSummary(),
                new String[]{"Match", "Details", "Result"},
                3
        ) {
            @Override
            public String[] getRowValues(int index, List<?> items, boolean defaultSort) {
                Match match = (Match) items.get(index);
                int position = defaultSort ? (index + 1) : (items.size() - index);
                return new String[]{
                        String.valueOf(position),
                        match.getMatchSummary(),
                        getFormattedResult(match)
                };
            }

            @Override
            public void sortItems(List<?> items, boolean defaultSort) {
                items.sort((Comparator<Object>) (o1, o2) -> {
                    Date d1 = ((Match) o1).getStart();
                    Date d2 = ((Match) o2).getStart();
                    return defaultSort ? d2.compareTo(d1) : d1.compareTo(d2);
                });
            }
        };
    }

    /**
     * Get the player's match history
     *
     * @param name     Player name
     * @param platform Player platform
     * @param helper   Emote helper
     * @return Match history
     */
    private MatchHistory getMatchHistory(String name, String platform, EmoteHelper helper) {
        ArrayList<Match> matches = new ArrayList<>();
        String matchJSON = getMatchHistoryJSON(name, platform);

        if(matchJSON == null || !new JSONObject(matchJSON).has("matches")) {
            return null;
        }

        JSONObject overview = new JSONObject(matchJSON);
        JSONArray matchList = overview.getJSONArray("matches");
        JSONObject summary = overview.getJSONObject("summary").getJSONObject("all");

        for(int i = 0; i < matchList.length(); i++) {
            JSONObject match = matchList.getJSONObject(i);
            JSONObject playerStats = match.getJSONObject("playerStats");
            JSONObject playerSummary = match.getJSONObject("player");
            String mapName = match.getString("map");
            Match.RESULT result = parseResult(
                    (!match.getBoolean("isPresentAtEnd") || match.isNull("result")) ? "FORFEIT" : match.getString("result")
            );

            matches.add(
                    new Match.MatchBuilder(
                            match.getString("matchID"),
                            new Map(
                                    maps.getOrDefault(
                                            mapName,
                                            "MISSING: " + mapName
                                    ),
                                    getMapImageURL(mapName)
                            ),
                            modes.getOrDefault(
                                    match.getString("mode"),
                                    "MISSING: " + match.getString("mode")
                            ),
                            new Date(match.getLong("utcStartSeconds") * 1000),
                            new Date(match.getLong("utcEndSeconds") * 1000),
                            result
                    )
                            .setKD(new Ratio(
                                    playerStats.getInt("kills"),
                                    playerStats.getInt("deaths")
                            ))
                            .setAccuracy(playerStats.has("shotsLanded") ? new Ratio(
                                    playerStats.getInt("shotsLanded"),
                                    playerStats.getInt("shotsFired")
                            ) : null)
                            .setMatchScore(new Score(
                                    match.getInt("team1Score"),
                                    match.getInt("team2Score"),
                                    result
                            ))
                            .setNemesis(getOptionalString(playerSummary, "nemesis"))
                            .setMostKilled(getOptionalString(playerSummary, "mostKilled"))
                            .setLongestStreak(getLongestStreak(playerStats))
                            .setDamageDealt(getDamageDealt(playerStats))
                            .setDamageReceived(getOptionalInt(playerStats, "damageTaken"))
                            .setXP(getOptionalInt(playerStats, "matchXp"))
                            .setDistanceTravelled(getOptionalInt(playerStats, "distanceTraveled"))
                            .build()
            );
        }

        return new MatchHistory(
                name,
                matches,
                new Ratio(
                        summary.getInt("kills"),
                        summary.getInt("deaths")
                )
        );
    }

    /**
     * Get the map image URL
     *
     * @param mapName Map code name e.g mp_aniyah
     * @return URL to map image
     */
    public abstract String getMapImageURL(String mapName);

    /**
     * Get the match history JSON
     *
     * @param name     Player name
     * @param platform Player platform
     * @return Match history JSON
     */
    public abstract String getMatchHistoryJSON(String name, String platform);

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
        return playerStats.has(key) && !playerStats.getString(key).isEmpty() ? playerStats.getString(key) : "-";
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
                getOptionalInt(playerStats, "damageDone"),
                getOptionalInt(playerStats, "damageDealt")
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
                getOptionalInt(playerStats, "longestStreak"),
                getOptionalInt(playerStats, "highestStreak")
        );
    }

    /**
     * Parse the result of the match from the given String
     *
     * @param result Result of match - win, loss, draw, forfeit
     * @return Match result
     */
    private Match.RESULT parseResult(String result) {
        switch(result.toLowerCase()) {
            case "win":
                return Match.RESULT.WIN;
            case "loss":
            case "lose":
                return Match.RESULT.LOSS;
            case "forfeit":
                return Match.RESULT.FORFEIT;
            default:
                return Match.RESULT.DRAW;
        }
    }
}
