package LOL;

import Bot.ResourceHandler;
import COD.Assets.Ratio;
import Network.Secret;
import Network.NetworkRequest;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;

import java.awt.image.BufferedImage;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;

/**
 * Hold summoner ranked & champion stats
 */
public class SummonerStats {
    private final HashMap<String, RankedQueue> queues = new HashMap<>();
    private final ArrayList<Champion> champions;
    private final String FLEX = "RANKED_FLEX_SR", SOLO = "RANKED_SOLO_5x5", res;
    private final ResourceHandler handler;
    private final SummonerOverview summonerOverview;
    private final BufferedImage profileBanner;

    /**
     * Create a summoner
     *
     * @param summonerOverview Overview of summoner - name, region, etc
     * @param res              Resource path
     */
    public SummonerStats(SummonerOverview summonerOverview, String res) {
        this.summonerOverview = summonerOverview;
        this.res = res;
        this.handler = new ResourceHandler();
        this.champions = fetchChampions();
        fetchRankedQueues();
        this.profileBanner = handler.getImageResource(res + "Summoner/Banners/" + getHighestRank() + ".png");
    }

    /**
     * Fetch a list of all champions with summoner mastery info attached.
     * Sort the list in order of mastery points descending.
     *
     * @return List of champions with summoner mastery info sorted by mastery points descending
     */
    private ArrayList<Champion> fetchChampions() {
        String url = summonerOverview.getApiURL()
                + "champion-mastery/v4/champion-masteries/by-summoner/"
                + summonerOverview.getId() + "?api_key=" + Secret.LEAGUE_KEY;
        String json = new NetworkRequest(url, false).get().body;

        ArrayList<Champion> champions = new ArrayList<>();

        if(json != null) {
            JSONArray championData = new JSONArray(json);
            for(int i = 0; i < championData.length(); i++) {
                JSONObject champion = championData.getJSONObject(i);
                champions.add(new Champion(
                        champion.getInt("championId"),
                        champion.getInt("championLevel"),
                        champion.getInt("championPoints"),
                        res
                ));
            }
            Collections.sort(champions);
        }
        return champions;
    }

    /**
     * Fetch the ranked queue information for the summoner.
     * Map the queue type -> RankedQueue
     * If information is not found for a given queue, initialise the queue
     * to unranked.
     */
    private void fetchRankedQueues() {
        String url = summonerOverview.getApiURL()
                + "league/v4/entries/by-summoner/"
                + summonerOverview.getId() + "?api_key=" + Secret.LEAGUE_KEY;

        String json = new NetworkRequest(url, false).get().body;
        if(json != null) {
            JSONArray queues = new JSONArray(json);
            for(int i = 0; i < queues.length(); i++) {
                JSONObject queue = queues.getJSONObject(i);
                String type = queue.getString("queueType");
                this.queues.put(type, new RankedQueue(
                                queue.getInt("wins"),
                                queue.getInt("losses"),
                                queue.getInt("leaguePoints"),
                                queue.getString("tier"),
                                queue.getString("rank"),
                                res,
                                type.equals(SOLO)
                        )
                );
            }
        }

        // Create default unranked queues
        if(!queues.containsKey(FLEX)) {
            queues.put(FLEX, new RankedQueue(res, false));
        }
        if(!queues.containsKey(SOLO)) {
            queues.put(SOLO, new RankedQueue(res, true));
        }
    }

    /**
     * Get the summoner overview - name, region, etc
     *
     * @return Summoner overview
     */
    public SummonerOverview getSummonerOverview() {
        return summonerOverview;
    }

    /**
     * Get the outline for the summoner banner based on their highest rank
     *
     * @return Profile banner outline
     */
    public BufferedImage getProfileBanner() {
        return profileBanner;
    }

    /**
     * Get the highest rank achieved across the solo and flex ranked queues
     *
     * @return Highest rank achieved between solo and flex queues
     */
    private String getHighestRank() {
        ArrayList<String> tiers = new ArrayList<>(
                Arrays.asList(
                        "default",
                        "iron",
                        "bronze",
                        "silver",
                        "gold",
                        "platinum",
                        "master",
                        "grandmaster",
                        "challenger"
                )
        );
        String solo = getSoloQueue().getTier().toLowerCase();
        String flex = getFlexQueue().getTier().toLowerCase();
        return (tiers.indexOf(solo) > tiers.indexOf(flex)) ? solo : flex;
    }

    /**
     * Get the list of summoner champion stats
     *
     * @return List of champions
     */
    public ArrayList<Champion> getChampions() {
        return champions;
    }

    /**
     * Get the flex ranked queue
     *
     * @return Flex ranked queue
     */
    public RankedQueue getFlexQueue() {
        return queues.get(FLEX);
    }

    /**
     * Get the solo ranked queue
     *
     * @return Solo ranked queue
     */
    public RankedQueue getSoloQueue() {
        return queues.get(SOLO);
    }

    /**
     * Hold information on a ranked queue
     */
    public class RankedQueue {
        private final Ratio winLoss;
        private final int points;
        private final String tier, rank, queue;
        private final BufferedImage helmet, banner;
        private boolean unranked = false;

        /**
         * Create a ranked queue
         *
         * @param wins   Game wins
         * @param losses Game losses
         * @param points Ranked LP
         * @param tier   Ranked tier - bronze, silver...
         * @param rank   Ranked division - IV, III...
         * @param res    Resource path
         * @param solo   Solo queue
         */
        public RankedQueue(int wins, int losses, int points, String tier, String rank, String res, boolean solo) {
            this.winLoss = new Ratio(wins, losses);
            this.points = points;
            this.tier = tier;
            this.rank = rank;
            this.queue = solo ? "Solo/Duo" : "Flex";
            this.helmet = handler.getImageResource(res + "Summoner/Ranked/Helmets/" + tier + "/" + rank + ".png");
            this.banner = handler.getImageResource(res + "Summoner/Ranked/Banners/" + tier + ".png");
        }

        /**
         * Create a default ranked queue
         *
         * @param res  Resource path
         * @param solo Solo queue
         */
        public RankedQueue(String res, boolean solo) {
            this(0, 0, 0, "DEFAULT", "DEFAULT", res, solo);
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
        private String getTier() {
            return tier.charAt(0) + tier.substring(1).toLowerCase();
        }
    }

    /**
     * Hold information on Summoner champion stats
     */
    public class Champion implements Comparable<Champion> {
        private final int id, level, points;
        private String name, imagePath;
        private final String res;
        private final String masteryIconPath;

        /**
         * Create a champion
         *
         * @param id     Champion id
         * @param level  Champion level
         * @param points Champion mastery points
         * @param res    Resource path
         */
        public Champion(int id, int level, int points, String res) {
            this.id = id;
            this.level = level;
            this.points = points;
            this.res = res;
            this.masteryIconPath = res + "Champions/Mastery/" + level + ".png";
            getChampionInfo();
        }

        /**
         * Get the champion id
         *
         * @return Champion id
         */
        public int getId() {
            return id;
        }

        /**
         * Get the champion name
         *
         * @return Champion name
         */
        public String getName() {
            return name;
        }

        /**
         * Get champion mastery points
         *
         * @return Champion mastery points
         */
        public int getPoints() {
            return points;
        }

        /**
         * Format the points 1000 -> 1,000
         *
         * @return Formatted mastery points
         */
        public String getFormattedPoints() {
            return new DecimalFormat("#,###").format(points);
        }

        /**
         * Get the champion loading screen image path
         *
         * @return Champion loading screen image path
         */
        public String getImagePath() {
            return imagePath;
        }

        /**
         * Get the champion mastery icon path
         *
         * @return Mastery icon path
         */
        public String getMasteryIconPath() {
            return masteryIconPath;
        }

        /**
         * Get the champion level
         *
         * @return Champion level
         */
        public int getLevel() {
            return level;
        }

        /**
         * Use the JSON file to find the champion name and image
         */
        private void getChampionInfo() {
            try {
                JSONObject champions = new JSONObject(
                        handler.getResourceFileAsString(res + "Data/champions.json")
                ).getJSONObject("data");
                for(String championName : champions.keySet()) {
                    JSONObject champion = champions.getJSONObject(championName);
                    if(champion.getString("key").equals(String.valueOf(id))) {
                        this.name = championName;
                        this.imagePath = res + "Champions/Images/" + champion.getString("id") + ".png";
                    }
                }
            }
            catch(Exception e) {
                e.printStackTrace();
            }
        }

        /**
         * Sort in descending order of mastery points
         *
         * @param o Champion to compare to current
         * @return Mastery point comparison
         */
        @Override
        public int compareTo(@NotNull SummonerStats.Champion o) {
            return o.getPoints() - getPoints();
        }
    }
}
