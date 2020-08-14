package LOL;

import COD.Player;
import Network.Secret;
import Network.NetworkRequest;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class Summoner {
    private String name;
    private final String apiKey = Secret.getLeagueKey(), res;
    private final HashMap<String, RankedQueue> queues = new HashMap<>();
    private final ArrayList<Champion> champions = new ArrayList<>();
    private int level;
    boolean exists;
    private final String FLEX = "RANKED_FLEX_SR", SOLO = "RANKED_SOLO_5x5";
    private File profileIcon, profileBorder, profileBanner;

    public Summoner(String name, String res) {
        this.name = name;
        this.res = res;
        queues.put(FLEX, new RankedQueue(res, false));
        queues.put(SOLO, new RankedQueue(res, true));
        this.exists = fetchSummonerData();
    }

    public String getName() {
        return name;
    }

    /**
     * Check if a summoner was found on the API
     *
     * @return Boolean account exists
     */
    public boolean exists() {
        return exists;
    }

    public File getProfileBorder() {
        return profileBorder;
    }

    /**
     * Fetch summoner data from riot
     *
     * @return JSON from API
     */
    private boolean fetchSummonerData() {
        String json;
        try {
            String name = URLEncoder.encode(this.name, "UTF-8");
            String url = "https://oc1.api.riotgames.com/lol/summoner/v4/summoners/by-name/" + name + apiKey;
            json = new NetworkRequest(url, false).get();
            if(json == null) {
                return false;
            }
            JSONObject summoner = new JSONObject(json);
            String id = summoner.getString("id");
            this.name = summoner.getString("name");
            this.level = summoner.getInt("summonerLevel");
            this.profileIcon = new File(res + "Summoner/Icons/" + summoner.getInt("profileIconId") + ".png");
            this.profileBorder = new File(res + "Summoner/Borders/" + roundLevel(level) + ".png");
            url = "https://oc1.api.riotgames.com/lol/league/v4/entries/by-summoner/" + id + apiKey;
            json = new NetworkRequest(url, false).get();

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
            this.profileBanner = new File(res + "Summoner/Banners/" + getHighestRank() + ".png");
            url = "https://oc1.api.riotgames.com/lol/champion-mastery/v4/champion-masteries/by-summoner/" + id + apiKey;
            json = new NetworkRequest(url, false).get();
            if(json != null) {
                JSONArray champions = new JSONArray(json);
                for(int i = 0; i < champions.length(); i++) {
                    JSONObject champion = champions.getJSONObject(i);
                    this.champions.add(new Champion(
                            champion.getInt("championId"),
                            champion.getInt("championLevel"),
                            champion.getInt("championPoints"),
                            res
                    ));
                }
                Collections.sort(this.champions);
            }
        }
        catch(Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public File getProfileBanner() {
        return profileBanner;
    }

    private String getHighestRank() {
        ArrayList<String> tiers = new ArrayList<>();
        tiers.add("default");
        tiers.add("iron");
        tiers.add("bronze");
        tiers.add("silver");
        tiers.add("gold");
        tiers.add("platinum");
        tiers.add("master");
        tiers.add("grandmaster");
        tiers.add("challenger");
        String solo = getSoloQueue().getTier().toLowerCase();
        String flex = getFlexQueue().getTier().toLowerCase();
        if(tiers.indexOf(solo) > tiers.indexOf(flex)) {
            return solo;
        }
        return flex;
    }

    public int getLevel() {
        return level;
    }

    public ArrayList<Champion> getChampions() {
        return champions;
    }

    public RankedQueue getFlexQueue() {
        return queues.get(FLEX);
    }

    public RankedQueue getSoloQueue() {
        return queues.get(SOLO);
    }

    public File getProfileIcon() {
        return profileIcon.exists() ? profileIcon : new File(res + "Summoner/Icons/0.png");
    }

    private int roundLevel(int level) {
        if(level < 30) {
            return 1;
        }
        if(level < 50) {
            return 30;
        }
        return (int) (25 * Math.floor((double) level / 25));
    }

    public static class RankedQueue {
        private final Player.Ratio winLoss;
        private final int points;
        private final String tier, rank, queue;
        private final File helmet, banner;
        private boolean unranked = false;

        public RankedQueue(int wins, int losses, int points, String tier, String rank, String res, boolean solo) {
            this.winLoss = new Player.Ratio(wins, losses);
            this.points = points;
            this.tier = tier;
            this.rank = rank;
            this.queue = solo ? "Solo/Duo" : "Flex";
            this.helmet = new File(res + "Summoner/Ranked/Helmets/" + tier + "/" + rank + ".png");
            this.banner = new File(res + "Summoner/Ranked/Banners/" + tier + ".png");
        }

        public RankedQueue(String res, boolean solo) {
            this(0, 0, 0, "DEFAULT", "DEFAULT", res, solo);
            this.unranked = true;
        }

        public boolean isUnranked() {
            return unranked;
        }

        public File getBanner() {
            return banner;
        }

        public File getHelmet() {
            return helmet;
        }

        public String getPoints() {
            return points + " LP";
        }

        public String getWins() {
            int wins = winLoss.getNumerator();
            return wins + ((wins == 1) ? " win" : " wins");
        }

        public String getLosses() {
            int loses = winLoss.getDenominator();
            return loses + ((loses == 1) ? " loss" : " losses");
        }

        public String getRatio() {
            return winLoss.formatRatio(winLoss.getRatio()) + " W/L";
        }

        public String getRankSummary() {
            return unranked ? "Unranked" : getTier() + " " + rank;
        }

        public String getQueue() {
            return queue;
        }

        public String getRank() {
            return rank;
        }

        private String getTier() {
            return tier.charAt(0) + tier.substring(1).toLowerCase();
        }
    }

    public static class Champion implements Comparable<Champion> {
        private final int id, level, points;
        private String name;
        private final String res;
        private File image, masteryIcon;

        public Champion(int id, int level, int points, String res) {
            this.id = id;
            this.level = level;
            this.points = points;
            this.res = res;
            this.masteryIcon = new File(res + "Champions/Mastery/" + level + ".png");
            getChampionInfo();
        }

        public String getName() {
            return name;
        }

        public int getPoints() {
            return points;
        }

        public String getFormattedPoints() {
            return new DecimalFormat("#,###").format(points);
        }

        public File getImage() {
            return image;
        }

        public File getMasteryIcon() {
            return masteryIcon;
        }

        public int getLevel() {
            return level;
        }

        private void getChampionInfo() {
            try {
                JSONObject champions = new JSONObject(new String(Files.readAllBytes(Paths.get(res + "Data/champions.json")), StandardCharsets.UTF_8)).getJSONObject("data");
                for(String championName : champions.keySet()) {
                    JSONObject champion = champions.getJSONObject(championName);
                    if(champion.getString("key").equals(String.valueOf(id))) {
                        this.name = championName;
                        this.image = new File(res + "Champions/Images/" + champion.getString("id") + ".png");
                    }
                }
            }
            catch(Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public int compareTo(@NotNull Summoner.Champion o) {
            return o.getPoints() - getPoints();
        }
    }
}
