package COD.Match;

import COD.Assets.Breakdown;
import COD.Assets.Map;
import COD.Assets.Mode;
import COD.Assets.Ratio;
import Command.Structure.EmbedHelper;
import Command.Structure.PieChart;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.stream.Collectors;

/**
 * Hold data on a player's match history
 */
public class MatchHistory {
    private final ArrayList<MatchStats> matchStats;
    private final HashMap<String, MatchStats> matchMap;
    private final String name;
    private int draws = 0, forfeits = 0;
    private Ratio winLoss;
    private final Ratio killDeath;
    private final Breakdown mapBreakdown, modeBreakdown;
    private final Font font;

    /**
     * Create the match history
     *
     * @param name       Player name
     * @param matchStats List of matches
     * @param killDeath  Kill/Death ratio
     * @param font       Game font to use for chart breakdowns
     */
    public MatchHistory(String name, ArrayList<MatchStats> matchStats, Ratio killDeath, Font font) {
        this.matchStats = matchStats;
        this.name = name;
        this.killDeath = killDeath;
        this.font = font;
        this.mapBreakdown = createMapBreakdown();
        this.modeBreakdown = createModeBreakdown();
        this.matchMap = new HashMap<>();
        for(MatchStats m : matchStats) {
            matchMap.put(m.getId(), m);
        }
        calculateSummary();
    }

    /**
     * Create a pie chart for the frequency of maps played in the match history
     */
    private Breakdown createMapBreakdown() {
        ArrayList<Map> mapsPlayed = matchStats
                .stream()
                .map(MatchStats::getMap)
                .collect(Collectors.toCollection(ArrayList::new));

        HashMap<Map, Integer> mapFrequency = new HashMap<>();
        for(Map map : mapsPlayed) {
            if(!mapFrequency.containsKey(map)) {
                mapFrequency.put(map, 0);
            }
            mapFrequency.put(map, mapFrequency.get(map) + 1);
        }
        PieChart.Section[] sections = mapFrequency
                .keySet()
                .stream()
                .map(m -> new PieChart.Section(m.getName(), mapFrequency.get(m), EmbedHelper.getRandomColour()))
                .toArray(PieChart.Section[]::new);
        return new Breakdown("Map", new PieChart(sections, font, false));
    }

    /**
     * Create a pie chart for the frequency of modes played in the match history
     */
    private Breakdown createModeBreakdown() {
        ArrayList<Mode> modesPlayed = matchStats
                .stream()
                .map(MatchStats::getMode)
                .collect(Collectors.toCollection(ArrayList::new));
        HashMap<Mode, Integer> modeFrequency = new HashMap<>();
        for(Mode mode : modesPlayed) {
            if(!modeFrequency.containsKey(mode)) {
                modeFrequency.put(mode, 0);
            }
            modeFrequency.put(mode, modeFrequency.get(mode) + 1);
        }
        PieChart.Section[] sections = modeFrequency
                .keySet()
                .stream()
                .map(m -> new PieChart.Section(m.getName(), modeFrequency.get(m), EmbedHelper.getRandomColour()))
                .toArray(PieChart.Section[]::new);
        return new Breakdown("Mode", new PieChart(sections, font, false));
    }

    /**
     * Get the breakdown of the maps played in the match history
     *
     * @return Breakdown of maps played
     */
    public Breakdown getMapBreakdown() {
        return mapBreakdown;
    }

    /**
     * Get the breakdown of modes played in the match history
     *
     * @return Breakdown of modes played
     */
    public Breakdown getModeBreakdown() {
        return modeBreakdown;
    }

    /**
     * Calculate the match history wins, losses, and draws
     */
    private void calculateSummary() {
        int wins = 0, losses = 0;
        for(MatchStats matchStats : this.matchStats) {
            switch(matchStats.getResult()) {
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
    public MatchStats getMatch(String id) {
        return matchMap.get(id);
    }

    /**
     * Get match wins
     *
     * @return Wins
     */
    public int getWins() {
        return winLoss.getNumerator();
    }

    /**
     * Get match losses
     *
     * @return Losses
     */
    public int getLosses() {
        return winLoss.getDenominator();
    }

    /**
     * Get the match history summary of wins/losses
     *
     * @return Match history summary
     */
    public String getSummary() {
        StringBuilder summary = new StringBuilder("Here are the last " + matchStats.size() + " matches:");
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
    public ArrayList<MatchStats> getMatches() {
        return matchStats;
    }
}