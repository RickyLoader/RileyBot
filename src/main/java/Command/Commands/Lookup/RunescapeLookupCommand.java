package Command.Commands.Lookup;

import Command.Structure.CommandContext;
import Command.Structure.EmoteHelper;
import Command.Structure.LookupCommand;
import Runescape.Hiscores.Hiscores;
import Runescape.Hiscores.HiscoresPlayer;
import Runescape.ImageBuilding.HiscoresImageBuilder;
import Runescape.Stats.PlayerStats;
import Runescape.Stats.PlayerStats.ACCOUNT;
import Runescape.Stats.Skill;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.List;
import java.util.Random;

/**
 * Look up a Runescape player and build an image with their stats
 */
public abstract class RunescapeLookupCommand<S extends PlayerStats, H extends Hiscores<S>, B extends HiscoresImageBuilder<S, H>> extends LookupCommand {
    private final HashSet<ARGUMENT> activatedArguments, acceptableArguments;
    private final HashSet<ACCOUNT> accountTypes;
    private static final String
            RANK_PREFIX = "#",
            LOWEST_RANK = "lowest",
            RANDOM_RANK = "random";

    public static final String LOOKUP_ARGS = "[" + LookupCommand.LOOKUP_ARGS + "/"
            + RANK_PREFIX + "{rank/" + LOWEST_RANK + "/" + RANDOM_RANK + "}]";

    private ACCOUNT accountType;
    protected final H hiscores;
    protected B imageBuilder;

    public enum ARGUMENT {
        VIRTUAL("Display virtual levels for skills above the max level."),
        SHOW_BOXES("Debug - fill image containers with random colours"),
        ACHIEVEMENTS(
                "Fetch and display player achievements from the Wise Old Man tracker."
                        + " Some account types may not be supported."
        ),
        XP_TRACKER(
                "Fetch and display player weekly XP gains & records from the Wise Old Man tracker."
                        + " Some account types may not be supported."
        ),
        SKILL_XP(
                "- Display current XP under each skill."
                        + "\n\n- Display progress until next level under each (non maxed (*)) skill."
                        + "\n\n- Outline the highest XP skill(s) in black (Hidden if all skills are the same XP)."
                        + "\n\n- Outline the closest to leveling (*) skill(s) in purple."
                        + " (Hidden if all skills are equal distance to leveling)."
                        + "\n\n(*) Affected by virtual argument"
        ),
        MAX(
                "- Highlight skills that are maxed (*)"
                        + "\n\n- Display the number of maxed skills in the total level box e.g \"Maxed: 1/23\" (*)"
                        + "\n\n- Display the XP progress towards max in the total XP box (*)"
                        + "\n\n(*) Affected by virtual argument"
        ),
        CLAN("Fetch and display the clan that the player is a member of (if they belong to a clan)."),
        RUNEMETRICS("Fetch and display player quest completions from RuneMetrics."),
        BOSS_BACKGROUNDS("Add boss lair backgrounds to the boss section of the image."),
        BOSSES(
                "View player bosses in a pageable message," +
                        " arguments that don't alter the boss section of the image won't do anything."
        );

        private final String description;

        /**
         * Create the argument. Set a description of what it does.
         *
         * @param description Argument description
         */
        ARGUMENT(String description) {
            this.description = description;
        }

        /**
         * Get the description of the argument
         *
         * @return Argument description
         */
        public String getDescription() {
            return description;
        }

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
                case BOSS_BACKGROUNDS:
                    return "bossbg";
                case XP_TRACKER:
                    return "xptracker";
                case SKILL_XP:
                    return "skillxp";
                default:
                    return this.name().toLowerCase();
            }
        }

        /**
         * Check if the given String matches one of the arguments
         *
         * @param input Input to check for argument
         * @return Input is an argument
         */
        public static boolean isArgument(String input) {
            return fromValue(input) != null;
        }

        /**
         * Get an argument from the given value
         *
         * @param input Value to get argument for - e.g "xp"
         * @return Argument from value - e.g XP (or NONE)
         */
        @Nullable
        public static ARGUMENT fromValue(String input) {
            for(ARGUMENT argument : ARGUMENT.values()) {
                if(argument.getValue().equalsIgnoreCase(input)) {
                    return argument;
                }
            }
            return null;
        }
    }

    /**
     * Create a Runescape lookup command
     *
     * @param trigger             Command trigger - e.g "osrslookup"
     * @param desc                Description of the command
     * @param acceptableArguments List of acceptable arguments for the hiscores lookup
     * @param accountTypes        List of acceptable account types for the hiscores lookup
     * @param hiscores            Hiscores instance
     */
    public RunescapeLookupCommand(String trigger, String desc, List<ARGUMENT> acceptableArguments, List<ACCOUNT> accountTypes, H hiscores) {
        super(
                trigger,
                desc,
                "[*] [**] " + trigger + " " + LOOKUP_ARGS
                        + "\n\n*Account Type (Located if not provided):\n\n"
                        + StringUtils.join(
                        accountTypes.stream().map(account -> account.name().toLowerCase()).toArray(),
                        ", "
                )
                        + "\n\n**Arguments:\n\n"
                        + StringUtils.join(
                        acceptableArguments.stream().map(ARGUMENT::getValue).toArray(),
                        ", "
                )
                        + "\n\nCan use multiple (space separated).\n\nTry: " + trigger
                        + " [arg] to view arg description",
                LOOKUP_ARGS,
                32
        );

        this.hiscores = hiscores;
        this.acceptableArguments = new HashSet<>();
        this.acceptableArguments.addAll(acceptableArguments);
        this.accountTypes = new HashSet<>();
        this.accountTypes.addAll(accountTypes);
        this.activatedArguments = new HashSet<>();
    }

    @Override
    public void processName(String name, CommandContext context) {
        Member member = context.getMember();
        MessageChannel channel = context.getMessageChannel();

        final ARGUMENT helpArgument = ARGUMENT.fromValue(name);

        // "trigger [arg]" - Show help text for arg
        if(helpArgument != null && acceptableArguments.contains(helpArgument)) {
            channel.sendMessage("```Argument help: " + helpArgument.getValue()
                    + "\n\n" + helpArgument.getDescription() + "```").queue();
            return;
        }

        // Search by rank (locate the name of the player at the given rank & account type)
        if(name.contains(RANK_PREFIX)) {

            // An account type is required to search by rank - switch LOCATE to NORMAL (can't locate a rank)
            if(accountType == ACCOUNT.LOCATE) {
                accountType = ACCOUNT.NORMAL;
            }

            // "1234" or "lowest" or "random"
            final String rankText = name
                    .replaceAll(RANK_PREFIX, "")
                    .replaceAll(",", "")
                    .trim();

            int rank;

            // Get a random rank between 1 & the current lowest rank
            if(rankText.equals(RANDOM_RANK)) {
                channel.sendTyping().queue();

                // May default to lowest possible rank if unable to find current lowest
                final int lowestRank = hiscores.getLowestRank(accountType);

                rank = new Random().nextInt(lowestRank) + 1;
                name = hiscores.getNameByRank(rank, accountType);
            }

            // Get the current lowest rank player
            else if(rankText.equals(LOWEST_RANK)) {
                channel.sendTyping().queue();

                final HiscoresPlayer lowestRankPlayer = hiscores.getLowestRankPlayer(accountType);

                // Unable to locate player, default to rank 2 million
                if(lowestRankPlayer == null) {
                    rank = Skill.MAX_RANK;
                    name = hiscores.getNameByRank(rank, accountType);
                }
                else {
                    rank = lowestRankPlayer.getRank();
                    name = lowestRankPlayer.getName();
                }
            }

            // Get the specified rank
            else {
                rank = toInteger(rankText);

                // Not a rank
                if(rank <= 0) {
                    channel.sendMessage(
                            member.getAsMention() + " Look mate, `" + rankText + "` isn't a rank"
                    ).queue();
                    return;
                }

                channel.sendTyping().queue();
                name = hiscores.getNameByRank(rank, accountType);
            }

            // Player name not found for rank
            if(name == null) {

                // Get the URL used to view a rank in the rank list
                final String rankPageUrl = hiscores.getHiscoresUrlByRank(accountType, rank);

                channel.sendMessage(
                        member.getAsMention()
                                + " Sorry bro, there is no rank `" + rank + "` on the "
                                + accountType.name().toLowerCase() + " hiscores!\n\n" + rankPageUrl
                ).queue();
                return;
            }
        }
        hiscoresLookup(name, activatedArguments, accountType, context);
    }

    /**
     * Perform a hiscores lookup for the player of the given name, arguments, and account type.
     *
     * @param name        Player name
     * @param arguments   Hiscores arguments
     * @param accountType Account type to search
     * @param context     Command context
     */
    protected void hiscoresLookup(String name, HashSet<ARGUMENT> arguments, ACCOUNT accountType, CommandContext context) {
        imageBuilder.buildStatsImage(name, accountType, context.getMessageChannel(), arguments);
    }

    @Override
    public String stripArguments(String query) {

        // Reset arguments
        activatedArguments.clear();
        accountType = ACCOUNT.LOCATE;

        // No args
        if(query.equals(getTrigger())) {
            return query;
        }

        String[] args = query
                .split(getTrigger())[0] // "iron xp virtual osrslookup me" -> "iron xp virtual"
                .trim()
                .split(" "); // ["iron", "xp", "virtual"]

        // Set account type and arguments
        for(String arg : args) {
            if(ARGUMENT.isArgument(arg)) {
                ARGUMENT argument = ARGUMENT.fromValue(arg);
                activatedArguments.add(argument);
            }
            else if(ACCOUNT.isAccountType(arg)) {
                accountType = ACCOUNT.fromName(arg);
            }
            query = query.replaceFirst(arg, "").trim();
        }
        return query;
    }

    @Override
    public void onReady(JDA jda, EmoteHelper emoteHelper) {
        this.imageBuilder = initialiseImageBuilder(hiscores, jda, emoteHelper);
    }

    /**
     * Initialise the Runescape hiscores image builder to use
     *
     * @param hiscores    Hiscores to use
     * @param jda         JDA
     * @param emoteHelper Emote helper
     * @return Hiscores image builder
     */
    protected abstract B initialiseImageBuilder(H hiscores, JDA jda, EmoteHelper emoteHelper);

    @Override
    public boolean matches(String query, Message message) {
        final String firstArg = query.split(" ")[0];

        // Either "trigger" or "[account type/arguments] trigger"
        return super.matches(query, message)
                || (acceptableArguments.contains(ARGUMENT.fromValue(firstArg))
                || accountTypes.contains(ACCOUNT.fromName(firstArg)))
                && query.contains(getTrigger());
    }
}
