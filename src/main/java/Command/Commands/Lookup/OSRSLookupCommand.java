package Command.Commands.Lookup;

import Bot.DiscordUser;
import Command.Structure.*;
import Network.ImgurManager;
import Runescape.Hiscores.HiscoresStatsResponse;
import Runescape.Hiscores.OSRSHiscores;
import Runescape.OSRS.Boss.BossManager;
import Runescape.OSRS.Boss.BossStats;
import Runescape.ImageBuilding.OSRSHiscoresImageBuilder;
import Runescape.Stats.OSRSPlayerStats;
import Runescape.Stats.PlayerStats;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.interactions.components.Button;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

import java.awt.image.BufferedImage;
import java.util.*;

import static Command.Commands.Lookup.RunescapeLookupCommand.ARGUMENT.*;

/**
 * Look up a OSRS player and build an image with their stats
 */
public class OSRSLookupCommand extends RunescapeLookupCommand<OSRSPlayerStats, OSRSHiscores, OSRSHiscoresImageBuilder> {
    public static final String UPLOAD_ERROR_BOSS_IMAGE_URL = "https://i.imgur.com/GE3DI2N.png";
    private Emote retryEmote;
    private Button retryButton;

    public OSRSLookupCommand() {
        super(
                "osrslookup",
                "Check out someone's stats on OSRS!",
                Arrays.asList(
                        BOSSES,
                        VIRTUAL,
                        ACHIEVEMENTS,
                        XP_TRACKER,
                        SKILL_XP,
                        MAX,
                        BOSS_BACKGROUNDS
                ),
                Arrays.asList(PlayerStats.ACCOUNT.values()),
                new OSRSHiscores()
        );
    }

    /**
     * Display the given player's boss kills as a pageable message
     *
     * @param context     Command context
     * @param playerStats Player stats
     * @param args        Hiscores arguments
     */
    private void displayPageableBossMessage(CommandContext context, OSRSPlayerStats playerStats, HashSet<ARGUMENT> args) {
        final HashMap<Integer, PageImage> pageImages = new HashMap<>(); // page -> boss image displayed on page
        final List<BossStats> bossStats = playerStats.getBossStats();

        new PageableTemplateEmbed<BossStats>(
                context,
                bossStats,
                EmbedHelper.OSRS_LOGO,
                "OSRS Boss Hiscores: " + playerStats.getName().toUpperCase()
                        + " (" + StringUtils.capitalize(playerStats.getAccountType().name().toLowerCase()) + ")",
                "This player is ranked in **"
                        + bossStats.size() + "/" + BossManager.getIdsInHiscoresOrder().length + "** bosses.",
                "Type: " + getTrigger() + " for help",
                OSRSHiscoresImageBuilder.MAX_BOSSES
        ) {
            @Override
            public String getNoItemsDescription() {
                return "This player has no boss kills!";
            }

            @Override
            public void sortItems(List<BossStats> bossStats, boolean defaultSort) {
                if(defaultSort) {
                    Collections.sort(bossStats);
                }
                else {
                    Collections.reverse(bossStats);
                }
                // Sort direction has flipped, page numbers are invalid
                pageImages.clear();
            }

            @Override
            public void displayPageItems(EmbedBuilder builder, List<BossStats> bossStats, int startingAt) {
                PageImage pageImage = pageImages.get(getPage());

                // Upload and store the boss image for the current page
                if(pageImage == null) {
                    pageImage = new PageImage(imageBuilder.buildBossSection(bossStats, args));
                    pageImages.put(getPage(), pageImage);
                }

                final String imageUrl = pageImage.getUrl();

                // Failed to upload image, use an error image
                if(imageUrl == null) {
                    builder.setImage(UPLOAD_ERROR_BOSS_IMAGE_URL)
                            .setDescription(
                                    getDescription() + "\n\n**Error**: " +
                                            "Something went wrong displaying these bosses, try the "
                                            + retryEmote.getAsMention() + "  button!"
                            );
                }
                else {
                    builder.setImage(imageUrl);
                }
            }

            @Override
            public ArrayList<Button> getButtonList() {
                ArrayList<Button> buttons = super.getButtonList();
                buttons.add(retryButton);
                return buttons;
            }

            @Override
            public boolean nonPagingButtonPressed(String buttonId) {

                // Force it to fall through without paging and refresh the current embed
                if(buttonId.equals(retryButton.getId())) {
                    return true;
                }

                return super.nonPagingButtonPressed(buttonId);
            }

            // Don't need as items aren't displayed individually but as a page
            @Override
            public void displayItem(EmbedBuilder builder, int currentIndex, BossStats item) {

            }
        }.showMessage();
    }

    @Override
    protected void hiscoresLookup(String name, HashSet<ARGUMENT> arguments, PlayerStats.ACCOUNT accountType, CommandContext context) {
        MessageChannel channel = context.getMessageChannel();
        Member member = context.getMember();

        // Pageable boss message
        if(arguments.contains(ARGUMENT.BOSSES)) {
            channel.sendTyping().queue();
            HiscoresStatsResponse<OSRSPlayerStats> response = hiscores.getHiscoresStatsResponse(name, accountType, arguments);
            OSRSPlayerStats stats = response.getStats();
            if(stats == null) {
                if(response.requestFailed()) {
                    channel.sendMessage(
                            member.getAsMention()
                                    + " I couldn't get the hiscores on the phone!"
                    ).queue();
                }
                else {
                    channel.sendMessage(
                            member.getAsMention()
                                    + " I didn't find a **" + response.getName() + "** on the hiscores!"
                    ).queue();
                }
                return;
            }
            displayPageableBossMessage(context, stats, arguments);
        }

        // Display hiscores
        else {
            super.hiscoresLookup(name, arguments, accountType, context);
        }
    }

    @Override
    public String getSavedName(long id) {
        return DiscordUser.getSavedName(id, DiscordUser.OSRS);
    }

    @Override
    public void saveName(String name, MessageChannel channel, User user) {
        DiscordUser.saveName(name, DiscordUser.OSRS, channel, user);
    }

    @Override
    public void onReady(JDA jda, EmoteHelper emoteHelper) {
        super.onReady(jda, emoteHelper);
        this.retryEmote = emoteHelper.getRefresh();
        this.retryButton = Button.primary("retry", Emoji.fromEmote(retryEmote));
    }

    @Override
    protected OSRSHiscoresImageBuilder initialiseImageBuilder(OSRSHiscores hiscores, JDA jda, EmoteHelper emoteHelper) {
        return new OSRSHiscoresImageBuilder(
                hiscores,
                emoteHelper,
                "Type: " + getTrigger() + " for help"
        );
    }

    /**
     * Boss image paired with uploaded image URL
     */
    private static class PageImage {
        private final BufferedImage image;
        private boolean imgur;
        private String url;

        /**
         * Attempt to upload the given image
         * and store the URL.
         *
         * @param image Image to upload
         */
        public PageImage(BufferedImage image) {
            this.image = image;
            this.url = getUrl();
        }

        /**
         * Get the URL to the image.
         * If there is none, upload the image and store the URL.
         * This is null if the upload has failed.
         *
         * @return Image URL
         */
        @Nullable
        public String getUrl() {
            if(url == null) {

                // Alternate between image hosts
                url = imgur
                        ? ImgurManager.uploadImage(image, false)
                        : ImgurManager.alternativeUpload(image, false);
                imgur = !imgur;
            }
            return url;
        }

        /**
         * Get the image to upload
         *
         * @return Image
         */
        public BufferedImage getImage() {
            return image;
        }
    }
}
