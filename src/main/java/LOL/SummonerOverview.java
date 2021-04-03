package LOL;

import Bot.FontManager;
import Bot.ResourceHandler;
import Command.Structure.EmbedHelper;
import Network.NetworkRequest;
import Network.Secret;
import org.json.JSONObject;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Hold basic LOL summoner overview - name, id, region, etc
 */
public class SummonerOverview {
    public static final String BASE_PATH = "/LOL/Summoner/";
    private final String apiURL;
    private final Region region;
    private final boolean exists;
    private String name, id;
    private int level;
    private BufferedImage profileIcon, levelIcon;

    /**
     * Create the summoner overview
     *
     * @param nameQuery Summoner name to search (case insensitive, the case appropriate name is retrieved via the API)
     * @param region    Summoner region (for deciding API endpoint to use)
     */
    public SummonerOverview(String nameQuery, Region region, boolean TFT) {
        this.region = region;
        this.apiURL = "https://" + region.getApiName() + ".api.riotgames.com/" + (TFT ? "tft/" : "lol/");
        this.exists = findSummoner(nameQuery, TFT);
    }

    /**
     * Find and initialise the basic summoner information
     * Return whether the summoner exists
     *
     * @param nameQuery Summoner name to search
     * @param TFT       Search for TFT summoner
     * @return Summoner exists
     */
    private boolean findSummoner(String nameQuery, boolean TFT) {
        try {
            String nameEncode = EmbedHelper.urlEncode(nameQuery);
            String endpoint = TFT ? "summoner/v1" : "summoner/v4";
            String url = apiURL + endpoint + "/summoners/by-name/" + nameEncode + "?api_key="
                    + (TFT ? Secret.TFT_KEY : Secret.LEAGUE_KEY);

            String json = new NetworkRequest(url, false).get().body;
            if(json == null) {
                return false;
            }
            JSONObject summoner = new JSONObject(json);
            this.name = summoner.getString("name");
            this.id = summoner.getString("id");
            this.level = summoner.getInt("summonerLevel");

            ResourceHandler handler = new ResourceHandler();
            BufferedImage border = handler.getImageResource(
                    BASE_PATH + "Borders/" + roundLevel(level) + ".png"
            );
            this.profileIcon = buildProfileIconImage(summoner.getInt("profileIconId"), border, handler);
            this.levelIcon = buildLevelIconImage(level, border, handler);
        }
        catch(Exception e) {
            return false;
        }
        return true;
    }

    /**
     * Build the profile icon image.
     * Display the summoner's selected profile icon inside the given border (determined by summoner level)
     *
     * @param profileIconId Unique id of profile icon
     * @param border        Border to display around the profile icon
     * @param handler       Resource handler
     * @return Image displaying profile icon surrounded by the level border
     */
    private BufferedImage buildProfileIconImage(int profileIconId, BufferedImage border, ResourceHandler handler) {
        String path = BASE_PATH + "Icons/";
        BufferedImage profileIcon = handler.getImageResource(path + profileIconId + ".png");

        // May not have the summoner's selected profile icon
        if(profileIcon == null) {
            profileIcon = handler.getImageResource(path + 0 + ".png");
        }

        return addBorderToIcon(border, profileIcon);
    }

    /**
     * Draw the given icon inside the provided border (determined by summoner level)
     *
     * @param border Border to display around the icon
     * @param icon   Icon to be displayed inside the border
     * @return Image displaying icon surrounded by the level border
     */
    private BufferedImage addBorderToIcon(BufferedImage border, BufferedImage icon) {
        Graphics g = icon.getGraphics();
        g.drawImage(
                border,
                (icon.getWidth() / 2) - (border.getWidth() / 2),
                (icon.getHeight() / 2) - (border.getHeight() / 2),
                null
        );
        g.dispose();
        return icon;
    }

    /**
     * Build the summoner level icon image.
     * Display the summoner's level inside the given border (determined by level)
     *
     * @param level   Summoner level
     * @param border  Border to display around the summoner level
     * @param handler Resource handler
     * @return Image displaying summoner level surrounded by the level border
     */
    private BufferedImage buildLevelIconImage(int level, BufferedImage border, ResourceHandler handler) {
        BufferedImage levelCircle = handler.getImageResource(
                BASE_PATH + "Banners/level_circle.png"
        );

        Graphics g = levelCircle.getGraphics();
        g.setFont(FontManager.LEAGUE_FONT.deriveFont(50f));
        FontMetrics fm = g.getFontMetrics();
        String levelString = String.valueOf(level);
        g.drawString(
                levelString,
                (levelCircle.getWidth() - fm.stringWidth(levelString)) / 2,
                (levelCircle.getHeight() / 2) + (fm.getMaxAscent() / 2)
        );
        return addBorderToIcon(border, levelCircle);
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
     * Get an image displaying the summoner's level inside the
     * appropriate level border
     *
     * @return Summoner level icon
     */
    public BufferedImage getLevelIcon() {
        return levelIcon;
    }

    /**
     * Get an image displaying the summoner's profile icon surrounded
     * by the appropriate level border
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
    public Region getRegion() {
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

    /**
     * Summoner region
     */
    public static class Region {
        private final String apiName, displayName;

        /**
         * @param displayName Display name - e.g "oce"
         * @param apiName     API name - e.g "oc1"
         */
        public Region(String displayName, String apiName) {
            this.displayName = displayName;
            this.apiName = apiName;
        }

        /**
         * Get the region display name- e.g "oce"
         *
         * @return Region display name
         */
        public String getDisplayName() {
            return displayName;
        }

        /**
         * Get the region API name - e.g "oc1"
         *
         * @return Region API name
         */
        public String getApiName() {
            return apiName;
        }
    }
}
