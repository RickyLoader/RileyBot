package Command.Commands.Lookup;

import Bot.ResourceHandler;
import COD.CODAPI;
import COD.Gunfight;
import COD.MWPlayer.Ratio;
import Command.Structure.*;
import net.dv8tion.jda.api.entities.MessageChannel;
import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Get MW player match history
 */
public class MWHistoryCommand extends MWLookupCommand {
    private final HashMap<String, String> modes, maps;

    public MWHistoryCommand() {
        super("mwhistory", "Have a gander at a player's match history!");
        String res = "/COD/MW/Data/";
        this.modes = getItemMap(res + "modes.json", "modes");
        this.maps = getItemMap(res + "maps.json", "maps");
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
        MatchHistory matchHistory = getMatches(name, getPlatform(), context.getEmoteHelper());

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
        getMatchHistoryEmbed(context, matchHistory).showMessage();
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
    private MatchHistory getMatches(String name, String platform, EmoteHelper helper) {
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
            matches.add(
                    new Match(
                            new Date(match.getLong("utcStartSeconds") * 1000),
                            new Date(match.getLong("utcEndSeconds") * 1000),
                            match.getBoolean("isPresentAtEnd") ? match.getString("result") : "FORFEIT",
                            maps.getOrDefault(
                                    match.getString("map"),
                                    "MISSING: " + match.getString("map")
                            ),
                            modes.getOrDefault(
                                    match.getString("mode"),
                                    "MISSING: " + match.getString("mode")
                            ),
                            new Ratio(
                                    playerStats.getInt("kills"),
                                    playerStats.getInt("deaths")
                            ),
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

    private static class MatchHistory {
        private final ArrayList<Match> matches;
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

    private static class Match {
        private final Date start, end;
        private final long duration;
        private final RESULT result;
        private final String winEmote, lossEmote, drawEmote, map, mode;
        private final Ratio killDeath;

        enum RESULT {
            WIN,
            LOSS,
            DRAW,
            FORFEIT
        }

        /**
         * Create a match
         *
         * @param start     Date of match start
         * @param end       Date of match end
         * @param result    Match result
         * @param killDeath Kill/Death ratio
         * @param helper    Emote Helper
         */
        public Match(Date start, Date end, String result, String map, String mode, Ratio killDeath, EmoteHelper helper) {
            this.start = start;
            this.end = end;
            this.duration = end.getTime() - start.getTime();
            this.result = parseResult(result);
            this.winEmote = EmoteHelper.formatEmote(helper.getComplete());
            this.lossEmote = EmoteHelper.formatEmote(helper.getFail());
            this.drawEmote = EmoteHelper.formatEmote(helper.getDraw());
            this.map = map;
            this.mode = mode;
            this.killDeath = killDeath;
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
         * Parse the result of the match from the given String
         *
         * @param result Result of match - win, loss, draw, forfeit
         * @return Match result
         */
        private RESULT parseResult(String result) {
            switch(result.toLowerCase()) {
                case "win":
                    return RESULT.WIN;
                case "loss":
                    return RESULT.LOSS;
                case "forfeit":
                    return RESULT.FORFEIT;
                default:
                    return RESULT.DRAW;
            }
        }

        /**
         * Get the date, duration, map, mode, and result of the match
         *
         * @return Date and duration
         */
        public String getMatchSummary() {
            return "**Date**: " + new SimpleDateFormat("dd/MM/yyyy").format(start) +
                    "\n**Time**: " + new SimpleDateFormat("HH:mm:ss").format(start) +
                    "\n**Duration**: " + EmbedHelper.formatTime(duration) +
                    "\n\n**Mode**: " + mode +
                    "\n**Map**: " + map +
                    "\n**K/D**: " + killDeath.getNumerator() + "/" + killDeath.getDenominator() +
                    " (" + killDeath.formatRatio(killDeath.getRatio()) + ")";
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
         * Get match victory
         *
         * @return Match victory
         */
        public boolean matchWon() {
            return result == RESULT.WIN;
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
         * Get the result formatted for use in a message embed with an emote
         *
         * @return Formatted result
         */
        public String getFormattedResult() {
            String title = result.toString();
            String emote;
            switch(result) {
                case WIN:
                    emote = winEmote;
                    break;
                case LOSS:
                    emote = lossEmote;
                    break;
                default:
                    emote = drawEmote;
                    break;
            }
            return title + " " + emote;
        }
    }
}
