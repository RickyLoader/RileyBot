package LOL;

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
import java.util.ArrayList;
import java.util.Collections;

public class Summoner {
    private String name;
    private final String apiKey = Secret.getLeagueKey(), res = "src/main/resources/LOL/Summoner/";
    private final ArrayList<RankedQueue> queues = new ArrayList<>();
    private final ArrayList<Champion> champions = new ArrayList<>();
    private int level;
    boolean exists;
    private File profileIcon, profileBorder;

    public Summoner(String name) {
        this.name = name;
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

    public String getRes() {
        return res;
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
            this.profileIcon = new File(res + "SummonerIcons/" + summoner.getInt("profileIconId") + ".png");
            this.profileBorder = new File(res + "SummonerBorders/" + roundLevel(level) + ".png");

            url = "https://oc1.api.riotgames.com/lol/league/v4/entries/by-summoner/" + id + apiKey;
            json = new NetworkRequest(url, false).get();
            if(json != null) {
                JSONArray queues = new JSONArray(json);
                for(int i = 0; i < queues.length(); i++) {
                    JSONObject queue = queues.getJSONObject(i);
                    this.queues.add(new RankedQueue(
                            queue.getInt("wins"),
                            queue.getInt("losses"),
                            queue.getInt("leaguePoints"),
                            queue.getString("tier"),
                            queue.getString("rank"),
                            queue.getString("queueType")
                    ));
                }
            }

            url = "https://oc1.api.riotgames.com/lol/champion-mastery/v4/champion-masteries/by-summoner/" + id + apiKey;
            json = new NetworkRequest(url, false).get();
            if(json != null) {
                JSONArray champions = new JSONArray(json);
                for(int i = 0; i < champions.length(); i++) {
                    JSONObject champion = champions.getJSONObject(i);
                    this.champions.add(new Champion(
                            champion.getInt("championId"),
                            champion.getInt("championLevel"),
                            champion.getInt("championPoints")
                    ));
                }
                Collections.sort(this.champions);
            }
        }
        catch(Exception e) {
            return false;
        }
        return true;
    }

    public int getLevel() {
        return level;
    }

    public ArrayList<Champion> getChampions() {
        return champions;
    }

    public ArrayList<RankedQueue> getQueues() {
        return queues;
    }

    public File getProfileIcon() {
        return profileIcon;
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
        final int wins, losses, points;
        final String tier, rank, queue;

        public RankedQueue(int wins, int losses, int points, String tier, String rank, String queue) {
            this.wins = wins;
            this.losses = losses;
            this.points = points;
            this.tier = tier;
            this.rank = rank;
            this.queue = (queue.equals("RANKED_SOLO_5x5")) ? "Ranked Solo/Duo" : "Ranked Flex";
        }

        public int getPoints() {
            return points;
        }

        public int getWins() {
            return wins;
        }

        public int getLosses() {
            return losses;
        }

        public String getQueue() {
            return queue;
        }

        public String getRank() {
            return rank;
        }

        public String getTier() {
            return tier;
        }
    }

    public static class Champion implements Comparable<Champion> {
        private final int id, level, points;
        private String name, res = "src/main/resources/LOL/";
        private File image;

        public Champion(int id, int level, int points) {
            this.id = id;
            this.level = level;
            this.points = points;
            getChampionInfo();
        }

        public String getName() {
            return name;
        }

        public int getPoints() {
            return points;
        }

        public File getImage() {
            return image;
        }

        private void getChampionInfo() {
            try {
                JSONObject champions = new JSONObject(new String(Files.readAllBytes(Paths.get(res + "Data/champions.json")), StandardCharsets.UTF_8)).getJSONObject("data");
                for(String championName : champions.keySet()) {
                    JSONObject champion = champions.getJSONObject(championName);
                    if(champion.getString("key").equals(String.valueOf(id))) {
                        this.name = championName;
                        this.image = new File(res + "Champions/" + champion.getString("id") + "_0.jpg");
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
