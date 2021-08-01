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
            TRIGGER = "osrslookup",
            BOSS_HELP = ARGUMENT.BOSSES.getValue() + " " + TRIGGER + " " + DEFAULT_LOOKUP_ARGS;
    public static final String UPLOAD_ERROR_BOSS_IMAGE_URL = "https://i.imgur.com/GE3DI2N.png";
    private boolean league, virtual, xp, bosses, achievements, showBoxes, max;
    private OSRSHiscores hiscores;

    private enum ARGUMENT {
        LEAGUE,
        BOSSES,
        VIRTUAL,
        SHOW_BOXES,
        ACHIEVEMENTS,
        XP,
        MAX,
        NONE;

        /**
         * Get the value of the argument. By default this is the lower case name of the enum, but may be different.
         * E.g SHOW_BOXES -> "showboxes"
         *
         * @return Argument value
         */
        public String getValue() {
            switch(this) {
                case SHOW_BOXES:
                    return "showboxes";
                default:
                    return this.name().toLowerCase();
            }
        }

        /**
         * Check if the argument is exclusive and can only be used alone
         *
         * @return Argument is exclusive
         */
        public boolean isExclusive() {
            return this == BOSSES || this == NONE;
        }

        /**
         * Check if the given String matches one of the arguments
         *
         * @param input Input to check for argument
         * @return Input is an argument
         */
        public static boolean isArgument(String input) {
            return getArgument(input) != NONE;
        }

        /**
         * Get an argument from the given String
         *
         * @param input String to get argument for - e.g "xp"
         * @return Argument from String - e.g XP (or NONE)
         */
        public static ARGUMENT getArgument(String input) {
            for(ARGUMENT argument : ARGUMENT.values()) {
                if(argument.getValue().equalsIgnoreCase(input)) {
                    return argument;
                }
            }
            return NONE;
        }

        /**
         * Get a list of arguments that can be used together in the format "arg arg.."
         *
         * @return String containing list of available arguments
         */
        public static String getHelpMessage() {
            StringBuilder helpMessage = new StringBuilder();
            final String separator = ", ";

            for(ARGUMENT argument : ARGUMENT.values()) {
                if(argument.isExclusive()) {
                    continue;
                }
                helpMessage.append(argument.getValue()).append(separator);
            }

            String result = helpMessage.toString();
            if(result.endsWith(separator)) {
                result = result.substring(0, result.length() - separator.length());
            }
            return result;
        }
    }

    public OSRSLookupCommand() {
        super(
                TRIGGER,
                "Check out someone's stats on OSRS!",
                "[arguments*] " + TRIGGER + " " + DEFAULT_LOOKUP_ARGS + "\n"
                        + BOSS_HELP
                        + "\n\n*Arguments:\n\n" + ARGUMENT.getHelpMessage() + "\n\nCan use multiple (space separated)",
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
        OSRSHiscoresArgs args = new OSRSHiscoresArgs(
                virtual,
                league,
                xp,
                achievements,
                showBoxes,
                max
        );
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
            displayPageableBossMessage(context, stats, args);
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
     * @param args        Hiscores arguments
     */
    private void displayPageableBossMessage(CommandContext context, OSRSPlayerStats playerStats, OSRSHiscoresArgs args) {
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
                    BufferedImage boss = hiscores.buildBossSection(bossStats, args);
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
        showBoxes = false;
        max = false;
        achievements = false;

        if(query.equals(getTrigger())) {
            return query;
        }

        String[] args = query
                .split(getTrigger())[0] // xp virtual osrslookup me -> xp virtual
                .trim()
                .split(" "); // ["xp", "virtual"]

        for(String arg : args) {
            ARGUMENT argument = ARGUMENT.getArgument(arg);
            switch(argument) {
                case SHOW_BOXES:
                    showBoxes = true;
                    break;
                case LEAGUE:
                    league = true;
                    break;
                case ACHIEVEMENTS:
                    achievements = true;
                    break;
                case VIRTUAL:
                    virtual = true;
                    break;
                case XP:
                    xp = true;
                    break;
                case MAX:
                    max = true;
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

    @Override
    public boolean matches(String query, Message message) {
        String firstArg = query.split(" ")[0];
        return super.matches(query, message) || query.contains(getTrigger()) && ARGUMENT.isArgument(firstArg);
    }
}
