package Runescape;

import Command.Structure.EmbedHelper;
import Command.Structure.EmoteHelper;
import Command.Structure.ImageBuilder;
import Command.Structure.ImageLoadingMessage;
import Network.NetworkRequest;
import Network.NetworkResponse;
import net.dv8tion.jda.api.entities.MessageChannel;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

public abstract class Hiscores extends ImageBuilder {
    public ImageLoadingMessage loading;
    private boolean timeout = false;
    private final MessageChannel channel;

    /**
     * Create a Runescape Hiscores instance
     *
     * @param channel      Channel to send message to
     * @param emoteHelper  Emote helper
     * @param resourcePath Path to resources
     * @param font         Font to use in image
     */
    public Hiscores(MessageChannel channel, EmoteHelper emoteHelper, String resourcePath, Font font) {
        super(emoteHelper, resourcePath, font);
        this.channel = channel;
    }

    /**
     * Make a request to the hiscores API
     *
     * @param url Hiscores URL to query
     * @return Response from API
     */
    public String[] hiscoresRequest(String url) {
        NetworkResponse response = new NetworkRequest(url, false).get();
        if(response.code == 504 || response.code == 408 || response.code == -1) {
            timeout = true;
            return null;
        }
        if(response.code == 404) {
            return null;
        }
        return (response.body + "," + url).replace("\n", ",").split(",");
    }

    /**
     * Get the URL to request a normal account's stats from the hiscores.
     * Every account appears on the normal hiscores regardless of type
     *
     * @param name Player name
     * @return URL to normal account hiscores CSV
     */
    public String getNormalAccount(String name) {
        return getURL("", name);
    }

    /**
     * Get the URL to request an ironman account's stats from the hiscores.
     * Every ironman account appears on the ironman hiscores regardless of type
     *
     * @param name Player name
     * @return URL to ironman account hiscores CSV
     */
    public String getIronmanAccount(String name) {
        return getURL("_ironman", name);
    }

    /**
     * Get the URL to request a hardcore ironman's stats from the hiscores.
     * Only hardcore ironman accounts appear on this page
     *
     * @param name Player name
     * @return URL to hardcore ironman account hiscores CSV
     */
    public String getHardcoreAccount(String name) {
        return getURL("_hardcore_ironman", name);
    }

    /**
     * Get the URL to request an ultimate ironman account's stats from the hiscores.
     * Only ultimate ironman accounts appear on this page
     *
     * @param name Player name
     * @return URL to ultimate ironman account hiscores CSV
     */
    public String getUltimateAccount(String name) {
        return getURL("_ultimate", name);
    }

    /**
     * Get a hiscores request URL using the given hiscores type and player name
     *
     * @param type Hiscores type
     * @param name Player name
     * @return URL to hiscores CSV
     */
    public abstract String getURL(String type, String name);

    /**
     * Get the default URL to display in the loading message
     *
     * @param name Player name
     * @return default URL to display in loading
     */
    public abstract String getDefaultURL(String name);

    /**
     * Fetch the player data from the hiscores
     *
     * @param name Player name
     * @return Player data
     */
    public abstract PlayerStats fetchPlayerData(String name);

    /**
     * Get the message to display if a player was not found - Appended to "That player "
     *
     * @param name Player name
     * @return Player not found error message
     */
    public abstract String getNotFoundMessage(String name);

    /**
     * Look a player up on the hiscores and return an image displaying their skills
     *
     * @param nameQuery   Player name
     * @param helpMessage Help message to display in loading message
     */
    public void buildImage(String nameQuery, String helpMessage) {
        String defaultURL = getDefaultURL(nameQuery);
        ArrayList<String> loadingCriteria = getLoadingCriteria();
        loadingCriteria.add("Building image...");

        this.loading = new ImageLoadingMessage(
                channel,
                getEmoteHelper(),
                getLoadingTitle(nameQuery),
                "Give me a second, their website can be slow as fuck.",
                getLoadingThumbnail(),
                helpMessage,
                loadingCriteria.toArray(new String[0])
        );

        loading.showLoading();

        PlayerStats stats = fetchPlayerData(nameQuery);

        if(stats == null) {
            if(timeout) {
                loading.failLoading("I wasn't able to connect to the " + EmbedHelper.embedURL("hiscores", defaultURL));
                return;
            }
            loading.failLoading("That player " + EmbedHelper.embedURL(getNotFoundMessage(nameQuery), defaultURL));
            return;
        }

        BufferedImage playerImage = buildHiscoresImage(stats);
        loading.completeStage();
        loading.completeLoading(playerImage, EmbedHelper.embedURL("View raw data", stats.getUrl()));
    }

    /**
     * Get the title to be used in the loading message
     *
     * @param name Player name
     * @return Loading message title
     */
    public abstract String getLoadingTitle(String name);

    /**
     * Get the thumbnail to be used in the loading message
     *
     * @return Loading message thumbnail
     */
    public abstract String getLoadingThumbnail();

    /**
     * Get the loading criteria to use
     *
     * @return Loading criteria
     */
    public abstract ArrayList<String> getLoadingCriteria();

    /**
     * Build the hiscores image
     *
     * @param playerStats PlayerStats instance
     * @return Hiscores image
     */
    public abstract BufferedImage buildHiscoresImage(PlayerStats playerStats);
}
