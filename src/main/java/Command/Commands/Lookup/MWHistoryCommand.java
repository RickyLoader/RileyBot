package Command.Commands.Lookup;

import Bot.ResourceHandler;
import COD.CODAPI;
import COD.Gunfight;
import COD.MWPlayer.Ratio;
import Command.Structure.*;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.json.JSONArray;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Get MW player match history
 */
public class MWHistoryCommand extends MWLookupCommand {
    private final HashMap<String, String> modes, maps;
    private String matchID;

    public MWHistoryCommand() {
        super(
                "mwhistory",
                "Have a gander at a player's match history!",
                getDefaultLookupArgs("mwhistory") + " [match id]\n"
                        + getHelpText("mwhistory") + " [match id]"
        );
        String res = "/COD/MW/Data/";
        this.modes = getItemMap(res + "modes.json", "modes");
        this.maps = getItemMap(res + "maps.json", "maps");
    }

    @Override
    public String stripArguments(String query) {
        query = setPlatform(query); // mwhistory args[]
        String[] args = query.split(" ");
        if(args.length == 1) {
            return query;
        }
        if(args[1].equals("save") || args.length < 3) {
            matchID = null;
            return fixName(query); // mwhistory save [name] || mwhistory [name]
        }
        matchID = args[2];
        return fixName(query.replace(matchID, "").trim());
    }

    /**
     * Parse map/mode names from local JSON
     *
     * @param location Location of file
     * @param key      JSON parent key
     * @return Map of IW names -> real names
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
                .setColor(EmbedHelper.getPurple())
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
                .addField("**Damage Dealt**", String.valueOf(match.getDamageDealt()), true)
                .addField("**Damage Taken**", String.valueOf(match.getDamageReceived()), true)
                .addField("**Longest Streak**", String.valueOf(match.getLongestStreak()), true)
                .addField("**Distance Traveled**", match.getDistanceTravelled(), false)
                .addField("**Nemesis**", match.getNemesis(), true)
                .addField("**Most Killed**", match.getMostKilled(), true)
                .addBlankField(true)
                .addField("**Match XP**", String.valueOf(match.getExperience()), true)
                .addField(
                        "**Result**",
                        match.getResult().toString() + " (" + match.getScore() + ") " + match.getResultEmote(),
                        true
                )
                .build();
    }

    /**
     * Get an embed detailing an error for a non existent match
     *
     * @param matchHistory Player match history
     * @return Error embed
     */
    private MessageEmbed getErrorEmbed(MatchHistory matchHistory) {
        return getDefaultEmbedBuilder(matchHistory.getName().toUpperCase())
                .setColor(EmbedHelper.getRed())
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
                .setTitle("MW Match Summary: " + name)
                .setFooter(
                        getDefaultLookupArgs("mwhistory") + " [match id]\n"
                                + getHelpText("mwhistory") + " [match id]"
                )
                .setThumbnail(Gunfight.getThumb());
    }

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
                Gunfight.getThumb(),
                "MW Match History: " + matchHistory.getName().toUpperCase(),
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
                        match.getFormattedResult()
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
     * @param name     Player MW name
     * @param platform Player platform
     * @param helper   Emote helper
     * @return Match history
     */
    private MatchHistory getMatchHistory(String name, String platform, EmoteHelper helper) {
        ArrayList<Match> matches = new ArrayList<>();
        String matchJSON = CODAPI.getMWMatchHistory(name, platform);

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
                    match.getBoolean("isPresentAtEnd") ? match.getString("result") : "FORFEIT"
            );

            matches.add(
                    new Match(
                            match.getString("matchID"),
                            new Date(match.getLong("utcStartSeconds") * 1000),
                            new Date(match.getLong("utcEndSeconds") * 1000),
                            result,
                            new Map(
                                    mapName,
                                    maps.getOrDefault(
                                            mapName,
                                            "MISSING: " + mapName
                                    )
                            ),
                            modes.getOrDefault(
                                    match.getString("mode"),
                                    "MISSING: " + match.getString("mode")
                            ),
                            new Ratio(
                                    playerStats.getInt("kills"),
                                    playerStats.getInt("deaths")
                            ),
                            playerStats.has("shotsLanded") ? new Ratio(
                                    playerStats.getInt("shotsLanded"),
                                    playerStats.getInt("shotsFired")
                            ) : null,
                            new Score(
                                    match.getInt("team1Score"),
                                    match.getInt("team2Score"),
                                    result
                            ),
                            playerSummary.has("nemesis") ? playerSummary.getString("nemesis") : "-",
                            playerSummary.has("mostKilled") ? playerSummary.getString("mostKilled") : "-",
                            playerStats.getInt("longestStreak"),
                            playerStats.getInt("damageDone"),
                            playerStats.has("damageTaken") ? playerStats.getInt("damageTaken") : 0,
                            playerStats.getInt("matchXp"),
                            playerStats.has("distanceTraveled") ? playerStats.getInt("distanceTraveled") : 0,
                            helper
                    )
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
                return Match.RESULT.LOSS;
            case "forfeit":
                return Match.RESULT.FORFEIT;
            default:
                return Match.RESULT.DRAW;
        }
    }

    /**
     * Hold data on a player's match history
     */
    private static class MatchHistory {
        private final ArrayList<Match> matches;
        private final HashMap<String, Match> matchMap;
        private final String name;
        private int draws = 0, forfeits = 0;
        private Ratio winLoss;
        private final Ratio killDeath;

        /**
         * Create the match history
         *
         * @param name      Player name
         * @param matches   List of matches
         * @param killDeath Kill/Death ratio
         */
        public MatchHistory(String name, ArrayList<Match> matches, Ratio killDeath) {
            this.matches = matches;
            this.name = name;
            this.killDeath = killDeath;
            this.matchMap = new HashMap<>();
            for(Match m : matches) {
                matchMap.put(m.getId(), m);
            }
            calculateSummary();
        }

        /**
         * Calculate the match history wins, losses, and draws
         */
        private void calculateSummary() {
            int wins = 0, losses = 0;
            for(Match match : matches) {
                switch(match.getResult()) {
                    case WIN:
                        wins++;
                        break;
                    case LOSS:
                        losses++;
                        break;
                    case DRAW:
                        draws++;
                        break;
                    case FORFEIT:
                        forfeits++;
                        break;
                }
            }
            this.winLoss = new Ratio(wins, losses);
        }

        /**
         * Get a match given the match ID
         *
         * @param id ID of match
         * @return Match or null
         */
        public Match getMatch(String id) {
            return matchMap.getOrDefault(id, null);
        }

        /**
         * Get match wins
         *
         * @return Wins
         */
        private int getWins() {
            return winLoss.getNumerator();
        }

        /**
         * Get match losses
         *
         * @return Losses
         */
        private int getLosses() {
            return winLoss.getDenominator();
        }

        /**
         * Get the match history summary of wins/losses
         *
         * @return Match history summary
         */
        public String getSummary() {
            StringBuilder summary = new StringBuilder("Here are the last " + matches.size() + " matches:");
            summary
                    .append("\n\nWins: ").append("**").append(getWins()).append("**")
                    .append(" | ")
                    .append("Losses: ").append("**").append(getLosses()).append("**");

            if(draws > 0) {
                summary
                        .append(" | ")
                        .append("Draws: ").append("**").append(draws).append("**");
            }

            if(forfeits > 0) {
                summary
                        .append(" | ")
                        .append("Forfeits: ").append("**").append(forfeits).append("**");
            }

            summary
                    .append(" | ")
                    .append("Ratio: ").append("**").append(winLoss.formatRatio(winLoss.getRatio())).append("**")
                    .append("\nKills: ").append("**").append(killDeath.getNumerator()).append("**")
                    .append(" | ")
                    .append("Deaths: ").append("**").append(killDeath.getDenominator()).append("**")
                    .append(" | ")
                    .append("Ratio: ").append("**").append(killDeath.formatRatio(killDeath.getRatio())).append("**");

            return summary.toString();
        }

        /**
         * Get the player name
         *
         * @return Player name
         */
        public String getName() {
            return name;
        }

        /**
         * Get the list of matches
         *
         * @return List of matches
         */
        public ArrayList<Match> getMatches() {
            return matches;
        }
    }

    /**
     * Hold data on a match played
     */
    private static class Match {
        private final Date start, end;
        private final long duration;
        private final RESULT result;
        private final String winEmote, lossEmote, drawEmote, mode, id, nemesis, mostKilled;
        private final Map map;
        private final Ratio killDeath, accuracy;
        private final Score score;
        private final int longestStreak, damageDealt, damageReceived, xp, distanceTravelled;

        enum RESULT {
            WIN,
            LOSS,
            DRAW,
            FORFEIT
        }

        /**
         * Create a match
         *
         * @param id                Match id
         * @param start             Date of match start
         * @param end               Date of match end
         * @param result            Match result
         * @param map               Map the match was played on
         * @param mode              Name of mode
         * @param killDeath         Kill/Death ratio
         * @param accuracy          Shots hit/Shots fired ratio
         * @param score             Match score
         * @param nemesis           Most killed by player
         * @param mostKilled        Most killed player
         * @param longestStreak     Longest killstreak
         * @param damageDealt       Total damage dealt by the player
         * @param damageReceived    Total damage received by the player
         * @param xp                Total match xp
         * @param distanceTravelled Distance travelled
         * @param helper            Emote Helper
         */
        public Match(String id, Date start, Date end, RESULT result, Map map, String mode, Ratio killDeath, Ratio accuracy, Score score, String nemesis, String mostKilled, int longestStreak, int damageDealt, int damageReceived, int xp, int distanceTravelled, EmoteHelper helper) {
            this.id = id;
            this.start = start;
            this.end = end;
            this.duration = end.getTime() - start.getTime();
            this.result = result;
            this.winEmote = EmoteHelper.formatEmote(helper.getComplete());
            this.lossEmote = EmoteHelper.formatEmote(helper.getFail());
            this.drawEmote = EmoteHelper.formatEmote(helper.getDraw());
            this.map = map;
            this.mode = mode;
            this.killDeath = killDeath;
            this.accuracy = accuracy;
            this.score = score;
            this.mostKilled = mostKilled;
            this.nemesis = nemesis;
            this.longestStreak = longestStreak;
            this.damageDealt = damageDealt;
            this.damageReceived = damageReceived;
            this.xp = xp;
            this.distanceTravelled = distanceTravelled;
        }

        /**
         * Get the distance travelled TODO WHAT IS THE UNIT OF MEASUREMENT
         *
         * @return Distance travelled
         */
        public String getDistanceTravelled() {
            return new DecimalFormat("#,### wobblies").format(distanceTravelled);
        }

        /**
         * Get the total match experience
         *
         * @return Match XP
         */
        public int getExperience() {
            return xp;
        }

        /**
         * Get the total damage dealt during the match by the player
         *
         * @return Damage dealt
         */
        public int getDamageDealt() {
            return damageDealt;
        }

        /**
         * Get the total damage the player received during the match
         *
         * @return Damage received
         */
        public int getDamageReceived() {
            return damageReceived;
        }

        /**
         * Get the longest killstreak the player obtained during the match
         *
         * @return Longest killstreak
         */
        public int getLongestStreak() {
            return longestStreak;
        }

        /**
         * Get the name of the enemy who killed the player the most
         *
         * @return Nemesis
         */
        public String getNemesis() {
            return nemesis;
        }

        /**
         * Get the name of the enemy who was killed by the player the most
         *
         * @return Most killed enemy
         */
        public String getMostKilled() {
            return mostKilled;
        }

        /**
         * Get a String displaying shots hit/shots fired
         *
         * @return Accuracy summary
         */
        public String getShotSummary() {
            if(accuracy == null) {
                return "-";
            }
            return getShotsFired() + "/" + getShotsHit();
        }

        /**
         * Get a String displaying the player accuracy during the match
         *
         * @return Player accuracy
         */
        public String getAccuracySummary() {
            if(accuracy == null) {
                return "-";
            }
            return accuracy.getRatioPercentage();
        }

        /**
         * Get the total number of shots fired
         *
         * @return Shots fired
         */
        public int getShotsFired() {
            return accuracy.getDenominator();
        }

        /**
         * Get the total number of shots hit
         *
         * @return Shots hit
         */
        public int getShotsHit() {
            return accuracy.getNumerator();
        }

        /**
         * Return presence of accuracy data
         *
         * @return Accuracy exists
         */
        public boolean hasAccuracy() {
            return accuracy != null;
        }

        /**
         * Get the match score
         *
         * @return Match score
         */
        public String getScore() {
            return score.getScore();
        }

        /**
         * Get the match gamemode
         *
         * @return Mode
         */
        public String getMode() {
            return mode;
        }

        /**
         * Get the map that the match was played on
         *
         * @return Map
         */
        public Map getMap() {
            return map;
        }

        /**
         * Get the match ID
         *
         * @return Match ID
         */
        public String getId() {
            return id;
        }

        /**
         * Get the match kills
         *
         * @return Match kills
         */
        public int getKills() {
            return killDeath.getNumerator();
        }

        /**
         * Get the match deaths
         *
         * @return Match deaths
         */
        public int getDeaths() {
            return killDeath.getDenominator();
        }

        /**
         * Get the date, duration, map, mode, and result of the match
         *
         * @return Date and duration
         */
        public String getMatchSummary() {
            return "**ID**: " + id +
                    "\n**Date**: " + getDateString() +
                    "\n**Time**: " + getTimeString() +
                    "\n**Duration**: " + getDurationString() +
                    "\n\n**Mode**: " + mode +
                    "\n**Map**: " + map.getName() +
                    "\n**K/D**: " + getKillDeathSummary();
        }

        /**
         * Get the kill/death ratio summary
         *
         * @return K/D Ratio
         */
        public String getKillDeathSummary() {
            return killDeath.getNumerator()
                    + "/" + killDeath.getDenominator()
                    + " (" + killDeath.formatRatio(killDeath.getRatio()) + ")";
        }

        /**
         * Get the date of the match formatted to a String
         *
         * @return Date String
         */
        public String getDateString() {
            return new SimpleDateFormat("dd/MM/yyyy").format(start);
        }

        /**
         * Get the time of the match formatted to a String
         *
         * @return Time String
         */
        public String getTimeString() {
            return new SimpleDateFormat("HH:mm:ss").format(start);
        }

        /**
         * Get the match duration formatted to a String
         *
         * @return Match String
         */
        public String getDurationString() {
            return EmbedHelper.formatTime(duration);
        }

        /**
         * Get date of match start
         *
         * @return Match start
         */
        public Date getEnd() {
            return end;
        }

        /**
         * Get date of match end
         *
         * @return Match end
         */
        public Date getStart() {
            return start;
        }

        /**
         * Get the match result - win, loss, draw, forfeit
         *
         * @return Match result
         */
        public RESULT getResult() {
            return result;
        }

        /**
         * Get the result formatted for use in a message embed with an emote and score
         *
         * @return Formatted result
         */
        public String getFormattedResult() {
            return result.toString() + " " + getResultEmote() + "\n(" + score.getScore() + ")";
        }

        /**
         * Get the embed formatted emote associated with the result
         *
         * @return Result emote
         */
        public String getResultEmote() {
            switch(result) {
                case WIN:
                    return winEmote;
                case LOSS:
                    return lossEmote;
                default:
                    return drawEmote;
            }
        }
    }

    /**
     * Hold map name and image info
     */
    private static class Map {
        private final String name, imageURL;

        /**
         * Create a map
         *
         * @param iwName   Infinity Ward map name e.g mp_m_overunder
         * @param realName Real map name e.g Docks
         */
        public Map(String iwName, String realName) {
            this.name = realName;
            this.imageURL = "https://www.callofduty.com/cdn/app/base-maps/mw/" + iwName + ".jpg";
        }

        /**
         * Get the map name
         *
         * @return Map name
         */
        public String getName() {
            return name;
        }

        /**
         * Get the URL to an image displaying the map loading screen
         *
         * @return Image URL
         */
        public String getImageURL() {
            return imageURL;
        }
    }

    /**
     * Hold score details
     */
    private static class Score {
        private final int scoreA, scoreB;
        private final Match.RESULT result;

        /**
         * Create a score
         *
         * @param scoreA Team a score
         * @param scoreB Team b score
         */
        public Score(int scoreA, int scoreB, Match.RESULT result) {
            this.scoreA = scoreA;
            this.scoreB = scoreB;
            this.result = result;
        }

        /**
         * Get the score and format to display relevant team first
         * e.g WIN = 6/2 LOSS = 2/6
         *
         * @return Formatted score
         */
        public String getScore() {
            int min = Math.min(scoreA, scoreB);
            int max = Math.max(scoreA, scoreB);
            return result == Match.RESULT.WIN ? max + "/" + min : min + "/" + max;
        }
    }
}
