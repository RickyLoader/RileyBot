package Command.Commands.Lookup;

import Bot.DiscordUser;
import Command.Structure.*;
import Network.ImgurManager;
import Runescape.OSRS.Boss.BossManager;
import Runescape.OSRS.Boss.BossStats;
import Runescape.HiscoresStatsResponse;
import Runescape.OSRS.Stats.OSRSHiscores;
import Runescape.OSRS.Stats.OSRSPlayerStats;
import Runescape.OSRSHiscoresArgs;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;

import java.awt.image.BufferedImage;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * Look up a OSRS player and build an image with their stats
 */
public class OSRSLookupCommand extends LookupCommand {
    private static final String
            LEAGUE = "league",
            VIRTUAL = "virtual",
            XP = "xp",
            BOSSES = "bosses",
            TRIGGER = "osrslookup",
            BOSS_HELP = BOSSES + " " + TRIGGER + " " + DEFAULT_LOOKUP_ARGS;
    public static final String UPLOAD_ERROR_BOSS_IMAGE_URL = "https://i.imgur.com/GE3DI2N.png";
    private boolean league, virtual, xp, bosses, achievements;
    private OSRSHiscores hiscores;

    public OSRSLookupCommand() {
        super(
                TRIGGER,
                "Check out someone's stats on OSRS!",
                "[" + LEAGUE + "] [" + VIRTUAL + "] [" + XP + "] " + TRIGGER + " " + DEFAULT_LOOKUP_ARGS + "\n"
                        + BOSS_HELP,
                12
        );
    }

    @Override
    public void onReady(JDA jda, EmoteHelper emoteHelper) {
        this.hiscores = new OSRSHiscores(
                emoteHelper,
                "Type: " + getTrigger() + " for help"
        );
    }

    @Override
    public void processName(String name, CommandContext context) {
        OSRSHiscoresArgs args = new OSRSHiscoresArgs(virtual, league, xp, achievements);
        MessageChannel channel = context.getMessageChannel();
        Member member = context.getMember();

        if(bosses) {
            channel.sendTyping().queue();
            HiscoresStatsResponse<OSRSPlayerStats> response = hiscores.getHiscoresStatsResponse(name, args);
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
            displayPageableBossMessage(context, stats);
        }
        else {
            hiscores.buildImage(
                    name,
                    context.getMessageChannel(),
                    args
            );
        }
    }

    /**
     * Display the given player's boss kills as a pageable message
     *
     * @param context     Command context
     * @param playerStats Player stats
     */
    private void displayPageableBossMessage(CommandContext context, OSRSPlayerStats playerStats) {
        final HashMap<Integer, String> pageImages = new HashMap<>(); // page -> page image URL
        final List<BossStats> bossStats = playerStats.getBossStats();

        new PageableTemplateEmbed<BossStats>(
                context,
                bossStats,
                EmbedHelper.OSRS_LOGO,
                "OSRS Boss Hiscores: " + playerStats.getName().toUpperCase(),
                "This player is ranked in **"
                        + bossStats.size() + "/" + BossManager.getIdsInHiscoresOrder().length + "** bosses.",
                "Try: " + BOSS_HELP,
                OSRSHiscores.MAX_BOSSES
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
                pageImages.clear(); // Sort direction has flipped, page numbers are invalid
            }

            @Override
            public void displayPageItems(EmbedBuilder builder, List<BossStats> bossStats, int startingAt) {
                String imageUrl = pageImages.get(getPage());
                if(imageUrl == null) {
                    BufferedImage boss = hiscores.buildBossSection(bossStats);
                    imageUrl = ImgurManager.uploadImage(boss, false);
                    pageImages.put(getPage(), imageUrl);
                }

                // Upload failed, tell user to try refresh the page
                boolean uploadFailed = imageUrl == null;
                if(uploadFailed) {
                    builder.setImage(UPLOAD_ERROR_BOSS_IMAGE_URL)
                            .setDescription(
                                    "**Error**: Something went wrong displaying these bosses, try leaving this page and trying again!"
                            );
                }
                else {
                    builder.setImage(imageUrl);
                }
            }

            // Don't need as items aren't displayed individually but as a page
            @Override
            public void displayItem(EmbedBuilder builder, int currentIndex, BossStats item) {

            }
        }.showMessage();
    }

    @Override
    public String stripArguments(String query) {
        league = false;
        virtual = false;
        xp = false;
        bosses = false;
        achievements = true; // Default fetch achievements

        if(query.equals(getTrigger())) {
            return query;
        }
        String[] args = query
                .split(getTrigger())[0] // xp virtual osrslookup me -> xp virtual
                .trim()
                .split(" "); // ["xp", "virtual"]

        for(String arg : args) {
            switch(arg) {
                case LEAGUE:
                    league = true;
                    break;
                case VIRTUAL:
                    virtual = true;
                    break;
                case XP:
                    xp = true;
                    break;
                // Don't fetch achievements or xp tracker when doing boss message
                case BOSSES:
                    bosses = true;
                    achievements = false;
                    xp = false;
                    break;
            }
            query = query.replaceFirst(arg, "").trim();
        }
        return query;
    }

    @Override
    public String getSavedName(long id) {
        return DiscordUser.getSavedName(id, DiscordUser.OSRS);
    }

    @Override
    public void saveName(String name, MessageChannel channel, User user) {
        DiscordUser.saveName(name, DiscordUser.OSRS, channel, user);
    }

    /**
     * Check if the given query is one of the lookup arguments
     *
     * @param query Query to check
     * @return Query is a lookup arg
     */
    private boolean isArg(String query) {
        return query.equals(LEAGUE) || query.equals(XP) || query.equals(VIRTUAL) || query.equals(BOSSES);
    }

    @Override
    public boolean matches(String query, Message message) {
        String firstArg = query.split(" ")[0];
        return super.matches(query, message) || query.contains(getTrigger()) && isArg(firstArg);
    }
}
