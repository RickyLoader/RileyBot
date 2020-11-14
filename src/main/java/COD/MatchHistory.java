package COD;


import COD.MWPlayer.Ratio;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Hold data on a player's match history
 */
public class MatchHistory {
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
        return matchMap.get(id);
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