package Runescape;

import Command.Structure.*;
import Network.NetworkRequest;
import Network.NetworkResponse;
import net.dv8tion.jda.api.entities.MessageChannel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

public abstract class Hiscores<T extends HiscoresArgs, S extends PlayerStats> extends ImageBuilder {
    private final String helpMessage;
    private boolean timeout = false;

    public enum LOADING_UPDATE_TYPE {
        COMPLETE,
        FAIL,
        UPDATE
    }

    /**
     * Create a Runescape Hiscores instance
     *
     * @param emoteHelper  Emote helper
     * @param resourcePath Path to resources
     * @param font         Font to use in image
     * @param helpMessage  Help message to display in loading message
     */
    public Hiscores(EmoteHelper emoteHelper, String resourcePath, Font font, String helpMessage) {
        super(emoteHelper, resourcePath, font);
        this.helpMessage = helpMessage;
    }

    /**
     * Make a request to the hiscores API
     *
     * @param url Hiscores URL to query
     * @return Response from API
     */
    @Nullable
    public String[] hiscoresRequest(String url) {
        NetworkResponse response = new NetworkRequest(url, false).get();
        if(response.code == 504 || response.code == 408 || response.code == NetworkResponse.TIMEOUT_CODE) {
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
     * @param args Hiscores arguments
     * @return default URL to display in loading
     */
    public abstract String getDefaultURL(String name, T args);

    /**
     * Fetch the player data from the hiscores
     *
     * @param name           Player name
     * @param args           Hiscores arguments
     * @param loadingMessage Optional loading message
     * @return Player data
     */
    @Nullable
    public abstract S fetchPlayerData(String name, T args, ImageLoadingMessage... loadingMessage);

    /**
     * Complete the current stage of the given loading message.
     *
     * @param loadingMessages Optional loading message
     */
    protected void completeLoadingMessageStage(@NotNull ImageLoadingMessage[] loadingMessages) {
        updateLoadingMessage(LOADING_UPDATE_TYPE.COMPLETE, null, loadingMessages);
    }

    /**
     * Pass/fail/update the current stage of the given loading message (if provided).
     *
     * @param type            COMPLETE/FAIL/UPDATE to perform
     * @param message         Optional message to display
     * @param loadingMessages Optional loading message
     */
    protected void updateLoadingMessage(LOADING_UPDATE_TYPE type, @Nullable String message, @NotNull ImageLoadingMessage[] loadingMessages) {
        if(loadingMessages.length == 0) {
            return;
        }

        ImageLoadingMessage loadingMessage = loadingMessages[0];
        boolean displayMessage = message != null;

        switch(type) {
            case COMPLETE:
                if(displayMessage) {
                    loadingMessage.completeStage(message);
                }
                else {
                    loadingMessage.completeStage();
                }
                break;
            case FAIL:
                loadingMessage.failStage(displayMessage ? message : EmbedLoadingMessage.LoadingStage.EMPTY_MESSAGE);
                break;
            case UPDATE:
                loadingMessage.updateStage(displayMessage ? message : EmbedLoadingMessage.LoadingStage.EMPTY_MESSAGE);
                break;
        }
    }

    /**
     * Get the message to display if a player was not found - Appended to "That player "
     *
     * @param name Player name
     * @param args Hiscores arguments
     * @return Player not found error message
     */
    public abstract String getNotFoundMessage(String name, T args);

    /**
     * Look a player up on the hiscores and build an image displaying their skills.
     * Send this message to the provided channel.
     * During this process, update a loading message within the channel to indicate the current progress.
     *
     * @param nameQuery Player name
     * @param channel   Channel to send the image to
     * @param args      Arguments for building image/looking up stats
     */
    public void buildImage(String nameQuery, MessageChannel channel, T args) {
        String defaultURL = getDefaultURL(nameQuery, args);
        ArrayList<String> loadingCriteria = getLoadingCriteria(args);
        loadingCriteria.add("Building image...");

        ImageLoadingMessage loadingMessage = new ImageLoadingMessage(
                channel,
                getEmoteHelper(),
                getLoadingTitle(nameQuery, args),
                "Give me a second, their website can be slow as fuck.",
                getLoadingThumbnail(args),
                helpMessage,
                loadingCriteria.toArray(new String[0])
        );

        loadingMessage.showLoading();

        S stats = fetchPlayerData(nameQuery, args, loadingMessage);

        if(stats == null) {
            if(timeout) {
                loadingMessage.failLoading(
                        "I wasn't able to connect to the "
                                + EmbedHelper.embedURL("hiscores", defaultURL)
                );
                return;
            }
            loadingMessage.failLoading(
                    "That player "
                            + EmbedHelper.embedURL(getNotFoundMessage(nameQuery, args), defaultURL)
            );
            return;
        }

        BufferedImage playerImage = buildHiscoresImage(stats, args);
        loadingMessage.completeStage();
        loadingMessage.completeLoading(playerImage, EmbedHelper.embedURL("View raw data", stats.getUrl()));
    }

    /**
     * Get the title to be used in the loading message
     *
     * @param name Player name
     * @param args Hiscores arguments
     * @return Loading message title
     */
    public abstract String getLoadingTitle(String name, T args);

    /**
     * Get the thumbnail to be used in the loading message
     *
     * @param args Hiscores arguments
     * @return Loading message thumbnail
     */
    public abstract String getLoadingThumbnail(T args);

    /**
     * Get the loading criteria to use
     *
     * @param args Hiscores arguments
     * @return Loading criteria
     */
    public abstract ArrayList<String> getLoadingCriteria(T args);

    /**
     * Build the hiscores image
     *
     * @param playerStats    PlayerStats instance
     * @param args           Hiscores arguments
     * @param loadingMessage Optional loading message
     * @return Hiscores image
     */
    public abstract BufferedImage buildHiscoresImage(S playerStats, T args, ImageLoadingMessage... loadingMessage);
}
