package Riot.LOL;

import Bot.ResourceHandler;
import COD.Assets.Ratio;

import java.awt.image.BufferedImage;

/**
 * Hold information on a ranked queue
 */
public class RankedQueue {
    private final Ratio winLoss;
    private final int points;
    private final String tier, rank, queue;
    private final BufferedImage helmet, banner;
    private boolean unranked = false;
    public static final String BASE_PATH = SummonerOverview.BASE_PATH + "Ranked/";

    /**
     * Create a ranked queue
     *
     * @param wins   Game wins
     * @param losses Game losses
     * @param points Ranked LP
     * @param tier   Ranked tier - bronze, silver...
     * @param rank   Ranked division - IV, III...
     * @param queue  Queue name
     */
    public RankedQueue(int wins, int losses, int points, String tier, String rank, String queue) {
        this.winLoss = new Ratio(wins, losses);
        this.points = points;
        this.tier = tier;
        this.rank = rank;
        this.queue = queue;
        ResourceHandler handler = new ResourceHandler();
        this.helmet = handler.getImageResource(BASE_PATH + "Helmets/" + tier + "/" + rank + ".png");
        this.banner = handler.getImageResource(BASE_PATH + "Banners/" + tier + ".png");
    }

    /**
     * Create a default ranked queue
     *
     * @param queue Queue name
     */
    public RankedQueue(String queue) {
        this(0, 0, 0, "DEFAULT", "DEFAULT", queue);
        this.unranked = true;
    }

    /**
     * Get the banner to display ranked stats in - based on tier
     *
     * @return Banner to display ranked stats
     */
    public BufferedImage getBanner() {
        return banner;
    }

    /**
     * Get the ranked helmet - based on tier and rank
     *
     * @return Ranked helmet
     */
    public BufferedImage getHelmet() {
        return helmet;
    }

    /**
     * Get the ranked points
     *
     * @return Ranked points/LP
     */
    public String getPoints() {
        return points + " LP";
    }

    /**
     * Get the number of wins
     *
     * @return Number of wins
     */
    public String getWins() {
        int wins = winLoss.getNumerator();
        return wins + ((wins == 1) ? " win" : " wins");
    }

    /**
     * Get the number of losses
     *
     * @return Number of losses
     */
    public String getLosses() {
        int loses = winLoss.getDenominator();
        return loses + ((loses == 1) ? " loss" : " losses");
    }

    /**
     * Get the win loss ratio summary String
     *
     * @return Win loss ratio summary
     */
    public String getRatio() {
        return winLoss.formatRatio(winLoss.getRatio()) + " W/L";
    }

    /**
     * Get the tier and division summary String
     *
     * @return Tier and division summary String
     */
    public String getRankSummary() {
        return unranked ? "Unranked" : getTier() + " " + rank;
    }

    /**
     * Get the name of the queue
     *
     * @return Queue name
     */
    public String getQueue() {
        return queue;
    }

    /**
     * Get the ranked tier name in normal case - GOLD -> Gold
     *
     * @return Ranked tier name
     */
    public String getTier() {
        return tier.charAt(0) + tier.substring(1).toLowerCase();
    }
}