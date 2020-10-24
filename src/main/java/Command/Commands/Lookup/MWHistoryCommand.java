package Command.Commands.Lookup;

import COD.CODAPI;
import COD.Gunfight;
import COD.MWPlayer;
import COD.MWPlayer.Ratio;
import Command.Structure.*;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.MessageChannel;
import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

/**
 * Get MW player match history
 */
public class MWHistoryCommand extends MWLookupCommand {
    public MWHistoryCommand() {
        super("mwhistory", "Have a gander at a player's match history!");
    }

    @Override
    public void processName(String name, CommandContext context) {
        MessageChannel channel = context.getMessageChannel();
        channel.sendMessage("One moment please").queue();
        MatchHistory matchHistory = getMatches(name, getPlatform(), context.getEmoteHelper());
        if(matchHistory == null) {
            channel.sendMessage("I didn't find any match history for " + name).queue();
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
                new String[]{"Index", "Date", "Result"},
                5
        ) {
            @Override
            public String[] getRowValues(int index, List<?> items, boolean defaultSort) {
                Match match = (Match) items.get(index);
                int position = defaultSort ? (index + 1) : (items.size() - index);
                return new String[]{
                        String.valueOf(position),
                        match.getDateSummary(),
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

        if(matchJSON == null) {
            return null;
        }

        JSONArray matchList = new JSONObject(matchJSON).getJSONArray("matches");

        for(int i = 0; i < matchList.length(); i++) {
            JSONObject match = matchList.getJSONObject(i);
            matches.add(
                    new Match(
                            new Date(match.getLong("utcStartSeconds") * 1000),
                            new Date(match.getLong("utcEndSeconds") * 1000),
                            match.getString("result"),
                            helper
                    )
            );
        }

        return new MatchHistory(
                name,
                matches
        );
    }

    private static class MatchHistory {
        private final ArrayList<Match> matches;
        private final String name;
        private int ties = 0;
        private Ratio winLoss;

        /**
         * Create the match history
         *
         * @param name    Player name
         * @param matches List of matches
         */
        public MatchHistory(String name, ArrayList<Match> matches) {
            this.matches = matches;
            this.name = name;
            calculateSummary();
        }

        /**
         * Calculate the match history wins, losses, and ties
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
                    case TIE:
                        ties++;
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
                    .append("\n\nWINS: ").append("**").append(getWins()).append("**")
                    .append("\nLOSSES: ").append("**").append(getLosses()).append("**");
            if(ties > 0) {
                summary.append("\nTIES: ").append("**").append(ties).append("**");
            }
            return summary
                    .append("\nRATIO: ").append("**").append(winLoss.formatRatio(winLoss.getRatio())).append("**")
                    .toString();
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
        private final String winEmote, lossEmote, tieEmote;

        enum RESULT {
            WIN,
            LOSS,
            TIE
        }

        /**
         * Create a match
         *
         * @param start  Date of match start
         * @param end    Date of match end
         * @param result Match result
         * @param helper Emote Helper
         */
        public Match(Date start, Date end, String result, EmoteHelper helper) {
            this.start = start;
            this.end = end;
            this.duration = end.getTime() - start.getTime();
            this.result = result.equalsIgnoreCase("win") ? RESULT.WIN : (result.equalsIgnoreCase("loss") ? RESULT.LOSS : RESULT.TIE);
            this.winEmote = EmoteHelper.formatEmote(helper.getComplete());
            this.lossEmote = EmoteHelper.formatEmote(helper.getFail());
            this.tieEmote = EmoteHelper.formatEmote(helper.getNeutral());
        }

        /**
         * Get the date and duration of the match
         *
         * @return Date and duration
         */
        public String getDateSummary() {
            return "**DATE**: "
                    + new SimpleDateFormat("dd/MM/yyyy").format(start)
                    + "\n**TIME**: "
                    + new SimpleDateFormat("HH:mm:ss").format(start)
                    + "\n**DURATION**: "
                    + EmbedHelper.formatTime(duration);
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
         * Get the match result - win, loss, draw
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
            return result.toString() + " " + (result == RESULT.WIN ? winEmote : (result == RESULT.LOSS ? lossEmote : tieEmote));
        }
    }
}
