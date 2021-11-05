package Runescape.ImageBuilding;

import Command.Commands.Lookup.RunescapeLookupCommand.ARGUMENT;
import Command.Structure.*;
import Runescape.Hiscores.Hiscores;
import Runescape.Hiscores.HiscoresStatsResponse;
import Runescape.Stats.PlayerStats;
import Runescape.Stats.PlayerStats.ACCOUNT;
import net.dv8tion.jda.api.entities.MessageChannel;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashSet;

/**
 * Runescape hiscores image builder
 */
public abstract class HiscoresImageBuilder<S extends PlayerStats, H extends Hiscores<S>> extends ImageBuilder {
    protected final H hiscores;
    private final String helpMessage;

    /**
     * Create a Runescape Hiscores image builder
     *
     * @param hiscores     Hiscores to use
     * @param emoteHelper  Emote helper
     * @param resourcePath Path to resources
     * @param font         Font to use in image
     * @param helpMessage  Help message to display in loading message
     */
    public HiscoresImageBuilder(H hiscores, EmoteHelper emoteHelper, String resourcePath, Font font, String helpMessage) {
        super(emoteHelper, resourcePath, font);
        this.hiscores = hiscores;
        this.helpMessage = helpMessage;
    }

    /**
     * Look a player up on the hiscores and build an image displaying their skills.
     * During this process, update a loading message within the channel to indicate the current progress.
     *
     * @param nameQuery   Player name
     * @param accountType Account type to fetch stats for
     * @param channel     Channel to send the image to
     * @param args        Arguments for building image/looking up stats
     */
    public void buildStatsImage(String nameQuery, ACCOUNT accountType, MessageChannel channel, HashSet<ARGUMENT> args) {
        ArrayList<String> loadingCriteria = hiscores.getLoadingCriteria(args, accountType);
        loadingCriteria.add("Building image...");

        ImageLoadingMessage loadingMessage = new ImageLoadingMessage(
                channel,
                getEmoteHelper(),
                hiscores.getLoadingTitle(nameQuery, args, accountType, getEmoteHelper()),
                "Give me a second, their website can be slow as fuck.",
                hiscores.getLoadingThumbnail(args, accountType),
                helpMessage,
                loadingCriteria.toArray(new String[0])
        );

        // Display the loading message
        loadingMessage.showLoading();

        HiscoresStatsResponse<S> response = hiscores.getHiscoresStatsResponse(nameQuery, accountType, args, loadingMessage);
        S stats = response.getStats();

        // Request failed/player doesn't exist
        if(stats == null) {

            // Request failed, player may exist but there was an issue checking
            if(response.requestFailed()) {
                loadingMessage.failLoading(
                        "I wasn't able to connect to the "
                                + EmbedHelper.embedURL("hiscores", response.getUrl())
                );
                return;
            }

            // Player doesn't exist/doesn't have stats for the given account type
            final String message = accountType == ACCOUNT.LOCATE
                    ? "doesn't exist bro"
                    : "doesn't have any " + accountType.name().toLowerCase() + " stats!";

            loadingMessage.failLoading("That name " + EmbedHelper.embedURL(message, response.getUrl()));
            return;
        }

        BufferedImage playerImage = buildHiscoresImage(stats, args);
        loadingMessage.completeStage();
        loadingMessage.completeLoading(playerImage, EmbedHelper.embedURL("View raw data", stats.getUrl()));
    }

    /**
     * Build the hiscores image
     *
     * @param playerStats    PlayerStats instance
     * @param args           Hiscores arguments
     * @param loadingMessage Optional loading message
     * @return Hiscores image
     */
    public abstract BufferedImage buildHiscoresImage(S playerStats, HashSet<ARGUMENT> args, ImageLoadingMessage... loadingMessage);
}
