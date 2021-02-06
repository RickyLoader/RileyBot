package LOL;

import Bot.ResourceHandler;
import Network.NetworkRequest;
import Network.Secret;
import org.json.JSONObject;

import java.awt.image.BufferedImage;
import java.net.URLEncoder;

/**
 * Hold basic LOL summoner overview - name, id, region, etc
 */
public class SummonerOverview {
    public static final String BASE_PATH = "/LOL/Summoner/";
    private final String region, apiURL;
    private final boolean exists;
    private String name, id;
    private int level;
    private BufferedImage profileIcon, levelBorder;

    /**
     * Create the summoner overview
     *
     * @param nameQuery Summoner name to search (case insensitive, the case appropriate name is retrieved via the API)
     * @param region    Summoner region (for deciding API endpoint to use)
     */
    public SummonerOverview(String nameQuery, String region) {
        this.region = region;
        this.apiURL = "https://" + region + ".api.riotgames.com/lol/";
        this.exists = findSummoner(nameQuery);
    }

    /**
     * Find and initialise the basic summoner information
     * Return whether the summoner exists
     *
     * @param nameQuery Summoner name to search
     * @return Summoner exists
     */
    private boolean findSummoner(String nameQuery) {
        try {
            String nameEncode = URLEncoder.encode(nameQuery, "UTF-8");
            String url = apiURL + "summoner/v4/summoners/by-name/" + nameEncode + "?api_key=" + Secret.LEAGUE_KEY;
            String json = new NetworkRequest(url, false).get().body;
            if(json == null) {
                return false;
            }
            JSONObject summoner = new JSONObject(json);
            this.name = summoner.getString("name");
            this.id = summoner.getString("id");
            this.level = summoner.getInt("summonerLevel");

            ResourceHandler handler = new ResourceHandler();
            this.profileIcon = getProfileIconImage(summoner.getInt("profileIconId"), handler);
            this.levelBorder = handler.getImageResource(
                    BASE_PATH + "Borders/" + roundLevel(level) + ".png"
            );
        }
        catch(Exception e) {
            return false;
        }
        return true;
    }

    /**
     * Get the summoner profile icon image. If the icon of the given id is not found,
     * return a default icon.
     *
     * @param id      Unique id of profile icon
     * @param handler Resource handler
     * @return Summoner profile icon image
     */
    private BufferedImage getProfileIconImage(int id, ResourceHandler handler) {
        String path = BASE_PATH + "Icons/";
        BufferedImage desired = handler.getImageResource(path + id + ".png");
        return desired == null ? handler.getImageResource(path + 0 + ".png") : desired;
    }

    /**
     * Get the summoner name as displayed in game
     *
     * @return Summoner name
     */
    public String getName() {
        return name;
    }

    /**
     * Check if the summoner exists
     *
     * @return Summoner exists
     */
    public boolean exists() {
        return exists;
    }

    /**
     * Get the summoner id - unique to each summoner and used in place
     * of summoner name when querying endpoints
     *
     * @return Unique summoner id
     */
    public String getId() {
        return id;
    }

    /**
     * Get the border shown around the summoner level
     *
     * @return Summoner level border
     */
    public BufferedImage getLevelBorder() {
        return levelBorder;
    }

    /**
     * Get the profile icon of the summoner
     *
     * @return Summoner profile icon
     */
    public BufferedImage getProfileIcon() {
        return profileIcon;
    }

    /**
     * Get the API URL for retrieving data about the summoner.
     * Varies by region.
     *
     * @return API URL
     */
    public String getApiURL() {
        return apiURL;
    }

    /**
     * Get the region of the summoner
     *
     * @return Region
     */
    public String getRegion() {
        return region;
    }

    /**
     * Get the summoner level
     *
     * @return Summoner level
     */
    public int getLevel() {
        return level;
    }

    /**
     * Round the summoner level to the nearest floor multiple of 25.
     * Summoner icon border is based on level and is rewarded every 25 levels from 50 onward
     *
     * @param level Summoner level
     * @return Closest floor multiple of 25
     */
    private int roundLevel(int level) {
        if(level < 30) {
            return 1;
        }
        if(level < 50) {
            return 30;
        }
        return (int) (25 * Math.floor((double) level / 25));
    }
}
