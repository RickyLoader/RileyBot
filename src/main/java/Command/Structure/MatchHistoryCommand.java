package Command.Structure;

import Bot.DiscordUser;
import COD.API.*;
import COD.API.CODStatsManager.PLATFORM;
import COD.API.Parsing.CODAPIParser;
import COD.API.Parsing.CODTrackerParser;
import COD.Assets.Map;
import COD.Gunfight.*;
import COD.Loadouts.LoadoutImageManager;
import COD.Match.*;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.interactions.ActionRow;
import net.dv8tion.jda.api.interactions.button.Button;
import net.dv8tion.jda.api.requests.restaction.MessageAction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONObject;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * View a COD player's match history
 */
public abstract class MatchHistoryCommand extends CODLookupCommand {
    private final HashMap<Long, MatchStats> matchMessages;
    private final HashSet<String> leaderboardSeen;
    private final ArrayList<WobblyScore> leaderboard;
    private final CODManager codManager;
    private final String footer, defaultButtonId;
    private final LoadoutImageManager loadoutImageManager;
    private final String thumbnail;
    private final CODAPIParser<CODManager> apiParser;
    private final CODTrackerParser<CODManager> trackerParser;

    private Button stats, loadouts;
    private String matchId;
    private EmoteHelper emoteHelper;
    private static final String
            STATS_BUTTON_ID = "stats",
            STATUS_KEY = "status",
            LATEST = "latest",
            WOBBLIES = "wobblies",
            WOBBLIES_SPECIFIC = WOBBLIES + " [rank]";

    /**
     * Create the command
     *
     * @param trigger    Command trigger
     * @param codManager COD asset manager
     * @param thumbnail  Thumbnail to use in the match embeds
     */
    public MatchHistoryCommand(String trigger, CODManager codManager, String thumbnail) {
        super(
                trigger,
                "Have a gander at a player's match history!",
                getHelpText(trigger) + " [match id/" + LATEST + "]\n\n"
                        + trigger + " " + WOBBLIES + "\n"
                        + trigger + " " + WOBBLIES_SPECIFIC
        );
        this.thumbnail = thumbnail;
        this.loadoutImageManager = LoadoutImageManager.getInstance();
        this.matchMessages = new HashMap<>();
        this.codManager = codManager;
        this.footer = "Type " + getTrigger() + " for help";
        this.defaultButtonId = STATS_BUTTON_ID; // Stats page is displayed first
        this.apiParser = new CODAPIParser<>(codManager);
        this.trackerParser = new CODTrackerParser<>(codManager);
        this.leaderboard = getWobblyLeaderboard();
        this.leaderboardSeen = leaderboard
                .stream()
                .map(WobblyScore::getKey)
                .collect(Collectors.toCollection(HashSet::new));
    }

    @Override
    public String stripArguments(String query) {
        query = setPlatform(query);
        String[] args = query.split(" ");

        if(args.length == 1) {
            return query;
        }

        String lastArg = args[args.length - 1];
        if(lastArg.matches("\\d+") || lastArg.equals(LATEST)) {
            matchId = lastArg;
            return query.replace(lastArg, "").trim();
        }
        matchId = null;
        return query;
    }

    @Override
    public void onArgumentsSet(String name, CommandContext context) {
        MessageChannel channel = context.getMessageChannel();

        if(name.equals(WOBBLIES)) {
            if(matchId != null) {
                showSpecificWobblyScore(channel);
                return;
            }
            showWobblyLeaderboard(context);
            return;
        }

        channel.sendTyping().queue();
        MatchHistory matchHistory = getMatchHistory(name, getPlatform(), channel);

        // Error fetching match history - error message has already been sent
        if(matchHistory == null) {
            return;
        }

        // No match history
        if(!matchHistory.hasMatches()) {
            channel.sendMessage(buildErrorEmbed("EMPTY", "This player has no recent matches!")).queue();
            return;
        }

        addMissingWobblyScores(matchHistory.getMatches());

        // Show specific match
        if(matchId != null) {
            if(matchId.equals(LATEST)) {
                sendMatchEmbed(matchHistory.getMatches().get(0), channel);
                return;
            }

            MatchStats matchStats = matchHistory.getMatch(matchId);

            // Not found in recent matches, attempt to locate by ID
            if(matchStats == null) {

                /*
                 * Player's UNO is required as matches retrieved by match ID return all player stats with only UNO
                 * available to identify player. If their UNO is not in any of their recent matches
                 * (it's always returned now, but wasn't always so if the match history is old it won't be in there),
                 * there's no way to identify which player they are.
                 */
                String uno = null;
                for(MatchStats stats : matchHistory.getMatches()) {
                    if(stats.getMainPlayer().getUno().equals(MatchPlayer.UNAVAILABLE)) {
                        continue;
                    }
                    uno = stats.getMainPlayer().getUno();
                }

                if(uno == null) {
                    channel.sendMessage(
                            buildErrorEmbed(
                                    "UNO",
                                    "I am unable to display matches outside of this player's most recent matches."
                            )
                    ).queue();
                    return;
                }

                matchStats = getMatchById(matchId, name, uno, getPlatform(), channel);

                // Error fetching match stats - error message has already been sent
                if(matchStats == null) {
                    return;
                }
            }
            sendMatchEmbed(matchStats, channel);
            return;
        }

        getMatchHistoryEmbed(context, matchHistory).showMessage();
    }

    /**
     * Get match stats for a player from outside their recent matches.
     * This requires the match ID and the player's UNO ID. When requesting match stats like this, the stats
     * of all players are returned with the only identifying info being the player's UNO ID.
     * The UNO ID can be found in any match stats within a player's recent matches, if the player has no recent matches,
     * there is no way to get from an e.g Battle.net name to an UNO.
     * If any errors occur, send a message to the given channel detailing the error.
     *
     * @param matchID  Match ID - e.g "7267476866035099410"
     * @param name     Player name
     * @param uno      Player UNO ID - e.g "9621104712623016829"
     * @param platform Player platform - e.g BATTLE
     * @param channel  Channel to send errors to
     * @return Match stats or null (if an error occurs)
     */
    @Nullable
    private MatchStats getMatchById(String matchID, String name, String uno, PLATFORM platform, MessageChannel channel) {
        final String json = getSpecificMatchJSON(matchID, platform);

        // No response, send an error
        if(json == null || new JSONObject(json).has(STATUS_KEY)) {
            channel.sendMessage(
                    buildErrorEmbed(
                            "FAIL",
                            json == null
                                    ? "I was unable to get the API on the phone!"
                                    : new JSONObject(json).getString(STATUS_KEY)
                    )
            ).queue();
            return null;
        }

        JSONArray players = new JSONObject(json).getJSONArray("allPlayers");

        // Match doesn't exist
        if(players.isEmpty()) {
            channel.sendMessage(
                    buildErrorEmbed("NO MATCH", "No match with the ID: **" + matchID + "** exists!")
            ).queue();
            return null;
        }

        // Attempt to locate player within list of returned players
        for(int i = 0; i < players.length(); i++) {
            MatchStats matchStats = apiParser.parseMatchStats(name, platform, players.getJSONObject(i));
            if(!matchStats.getMainPlayer().getUno().equals(uno)) {
                continue;
            }
            return matchStats;
        }

        channel.sendMessage(
                buildErrorEmbed("NOT PRESENT", "This player was not present in that match!")
        ).queue();
        return null;
    }

    /**
     * Show a specific entry on the wobbly leaderboard
     *
     * @param channel Channel to send entry to
     */
    private void showSpecificWobblyScore(MessageChannel channel) {
        int index = 0;
        try {
            index = Integer.parseInt(matchId) - 1;
        }
        catch(NumberFormatException e) {
            e.printStackTrace();
        }
        if(index < 0 || index > leaderboard.size() - 1) {
            channel.sendMessage("That's not a rank!").queue();
            return;
        }
        WobblyScore score = leaderboard.get(index);
        Map map = score.getMap();

        EmbedBuilder entryEmbedBuilder = new EmbedBuilder()
                .setTitle(codManager.getGameName() + " Wobbly Rank #" + (index + 1))
                .setThumbnail(thumbnail)
                .setDescription("Use **" + getTrigger() + " wobblies** to view the full leaderboard.")
                .addField("Name", score.getPlayerName(), true)
                .addField("Date", score.getDateString(), true)
                .addBlankField(true)
                .addField("Wobblies", MatchPlayer.formatDistance(score.getWobblies(), "wobblies"), true)
                .addField("Metres", MatchPlayer.formatDistance(score.getMetres(), "metres"), true)
                .addBlankField(true)
                .addField("Map", score.getMap().getName(), true)
                .addField("Mode", score.getMode().getName(), true)
                .addBlankField(true)
                .addField("Match ID", score.getMatchId(), true)
                .setColor(EmbedHelper.PURPLE);

        if(map.hasImageUrl()) {
            entryEmbedBuilder.setImage(map.getImageUrl());
        }
        channel.sendMessage(entryEmbedBuilder.build()).queue();
    }

    /**
     * Build and send the wobbly leaderboard (top distance travelled)
     *
     * @param context Command context
     */
    private void showWobblyLeaderboard(CommandContext context) {
        if(leaderboard.isEmpty()) {
            context.getMessageChannel().sendMessage(
                    "There are no wobblies on the leaderboard for "
                            + codManager.getGameName() + "!"
            ).queue();
            return;
        }
        new PageableTableEmbed<WobblyScore>(
                context,
                leaderboard,
                thumbnail,
                codManager.getGameName() + " Wobbly Leaderboard",
                "Use **" + getTrigger() + " wobblies [rank]** to view more details.\n\n"
                        + "Here are the " + leaderboard.size() + " wobbly scores:",
                footer,
                new String[]{"Rank", "Name", "Wobblies"},
                5
        ) {
            @Override
            public String getNoItemsDescription() {
                return "There are no wobblies on the leaderboard!";
            }

            @Override
            public String[] getRowValues(int index, WobblyScore score, boolean defaultSort) {
                int rank = defaultSort ? (index + 1) : (getItems().size() - index);
                return new String[]{
                        String.valueOf(rank),
                        score.getPlayerName(),
                        MatchPlayer.formatDistance(score.getWobblies(), "wobblies")
                };
            }

            @Override
            public void sortItems(List<WobblyScore> items, boolean defaultSort) {
                WobblyScore.sortLeaderboard(items, defaultSort);
            }
        }.showMessage();
    }

    /**
     * Get the list of wobbly scores from the database
     *
     * @return List of wobbly scores
     */
    private ArrayList<WobblyScore> getWobblyLeaderboard() {
        ArrayList<WobblyScore> wobblyScores = new ArrayList<>();
        String json = DiscordUser.getWobbliesLeaderboard(codManager.getGameId());
        if(json == null) {
            return wobblyScores;
        }
        JSONArray scores = new JSONArray(json);
        for(int i = 0; i < scores.length(); i++) {
            wobblyScores.add(WobblyScore.fromJSON(scores.getJSONObject(i), codManager));
        }
        WobblyScore.sortLeaderboard(wobblyScores, true);
        return wobblyScores;
    }

    /**
     * Check if the given button ID is a valid button to control the gunfight
     *
     * @param buttonId ID of button
     * @return Button is valid
     */
    private boolean isValidButton(String buttonId) {
        return buttonId.equals(stats.getId()) || buttonId.equals(loadouts.getId());
    }

    /**
     * Create a button listener to handle toggling a match embed between match stats,
     * loadouts, and players
     *
     * @return Emote listener
     */
    private ButtonListener getMatchButtonListener() {
        return new ButtonListener() {
            @Override
            public void handleButtonClick(@NotNull ButtonClickEvent event) {
                String buttonId = event.getComponentId();
                long messageId = event.getMessageIdLong();
                if(!matchMessages.containsKey(messageId) || !isValidButton(buttonId)) {
                    return;
                }

                MatchStats matchStats = matchMessages.get(messageId);
                MessageEmbed content = null;

                if(buttonId.equals(stats.getId())) {
                    content = buildMatchEmbed(matchStats);
                }
                else if(buttonId.equals(loadouts.getId()) && shouldDisplayLoadoutsButton(matchStats)) {
                    content = buildMatchLoadoutEmbed(matchStats);
                }

                // Shouldn't happen but would if buttons weren't removed when unavailable
                if(content == null) {
                    return;
                }

                event.deferEdit().setEmbeds(content).setActionRows(getButtons(buttonId, matchStats)).queue();
            }
        };
    }

    /**
     * Get the action row of buttons to use in the message embed for the given match stats.
     * This includes disabling the button associated with the currently displayed embed, and removing
     * buttons when they should not be accessible (e.g switch map image button when viewing loadouts)
     *
     * @param currentButtonId Button ID of the currently displayed embed
     * @param matchStats      Match stats to determine if certain buttons should be displayed
     * @return Buttons for given match stats
     */
    private ActionRow getButtons(String currentButtonId, MatchStats matchStats) {
        ArrayList<Button> buttons = new ArrayList<>();
        buttons.add(currentButtonId.equals(stats.getId()) ? stats.asDisabled() : stats);

        if(shouldDisplayLoadoutsButton(matchStats)) {
            buttons.add(currentButtonId.equals(loadouts.getId()) ? loadouts.asDisabled() : loadouts);
        }

        return ActionRow.of(buttons);
    }

    /**
     * Check if the loadouts button should be displayed on the message embed for the given match stats
     * This is if the match player has any loadouts
     *
     * @param matchStats Match stats
     * @return Loadouts button should be displayed
     */
    private boolean shouldDisplayLoadoutsButton(MatchStats matchStats) {
        return matchStats.getMainPlayer().hasLoadouts();
    }

    /**
     * Create and send an embed for a specific match
     * Attach emotes if the match has information to toggle between.
     *
     * @param matchStats Player match stats
     * @param channel    Channel to send to
     */
    private void sendMatchEmbed(MatchStats matchStats, MessageChannel channel) {

        MessageEmbed matchEmbed = buildMatchEmbed(matchStats);
        MessageAction sendMessage = channel.sendMessage(matchEmbed).setActionRows(getButtons(defaultButtonId, matchStats));
        MatchPlayer player = matchStats.getMainPlayer();
        Consumer<Message> callback = message -> matchMessages.put(message.getIdLong(), matchStats);

        if(player.hasLoadouts()) {
            player.setLoadoutImage(buildLoadoutImage(player.getLoadouts()));

            /*
             * Setting the attached loadout image file as the embed footer icon prevents it from displaying as
             * a separate message.
             * When viewing the loadout embed, both the embed footer icon & embed image can use the file.
             */
            sendMessage.addFile(player.getLoadoutImage(), "image.png").queue(callback);
            return;
        }
        sendMessage.queue(callback);
    }

    /**
     * Build an image displaying all of the given loadouts
     *
     * @param loadouts Loadouts to display
     * @return Byte array of image
     */
    private byte[] buildLoadoutImage(Loadout[] loadouts) {
        BufferedImage[] loadoutImages = new BufferedImage[loadouts.length];
        int tallest = 0;
        for(int i = 0; i < loadouts.length; i++) {
            BufferedImage loadoutImage = loadoutImageManager.buildLoadoutImage(
                    loadouts[i],
                    "Loadout " + (i + 1) + "/" + loadouts.length
            );
            if(loadoutImage.getHeight() > tallest) {
                tallest = loadoutImage.getHeight();
            }
            loadoutImages[i] = loadoutImage;
        }
        BufferedImage background = new BufferedImage(
                (loadoutImages[0].getWidth() * loadoutImages.length) + (loadoutImages.length + 1) * 10,
                tallest + 20,
                BufferedImage.TYPE_INT_RGB
        );
        Graphics g = background.getGraphics();
        int x = 10, y = 10;
        for(BufferedImage image : loadoutImages) {
            g.drawImage(image, x, y, null);
            x += image.getWidth() + 10;
        }
        g.dispose();
        return ImageLoadingMessage.imageToByteArray(background);
    }

    /**
     * Create a message embed detailing a player's stats during the given match
     *
     * @param matchStats Match stats
     * @return Message embed detailing match stats
     */
    private MessageEmbed buildMatchEmbed(MatchStats matchStats) {
        int wobblyRank = getWobblyRank(matchStats);
        MatchPlayer player = matchStats.getMainPlayer();
        EmbedBuilder builder = getDefaultMatchEmbedBuilder(matchStats)
                .setTitle(
                        codManager.getGameName()
                                + " Match Summary: " + matchStats.getMainPlayer().getName().toUpperCase()
                )
                .addField("**Date**", matchStats.getDateString(), true)
                .addField("**Time**", matchStats.getTimeString(), true)
                .addField("**Duration**", matchStats.getMatchDurationString(), true)
                .addField("**Mode**", matchStats.getMode().getName(), true)
                .addField("**Map**", matchStats.getMap().getName(), true);
        if(matchStats.playerCompleted()) {
            builder.addBlankField(true);
        }
        else {
            builder.addField("**Time Played**", player.getTimePlayedString(), true);
        }
        return builder
                .addField("**K/D**", player.getKillDeathSummary(), true)
                .addField("**Shots Fired/Hit**", player.getShotSummary(), true)
                .addField("**Accuracy**", player.getAccuracySummary(), true)
                .addField("**Damage Dealt**", player.getDamageDealt(), true)
                .addField("**Damage Taken**", player.getDamageReceived(), true)
                .addField("**Highest Streak**", String.valueOf(player.getLongestStreak()), true)
                .addField(
                        "**Distance Travelled**",
                        player.formatWobblies() + "\n" + player.formatMetres(),
                        true
                )
                .addField("**Time Spent Moving**", player.getPercentTimeMovingString(), true)
                .addField(
                        "**Wobbly Rank**",
                        wobblyRank == 0 ? "-" : String.valueOf(wobblyRank),
                        true
                )
                .addField("**Nemesis**", player.getNemesis(), true)
                .addField("**Most Killed**", player.getMostKilled(), true)
                .addBlankField(true)
                .addField("**Match XP**", player.getExperience(), true)
                .addField(
                        "**Result**",
                        matchStats.getScore().getResult().toString()
                                + " (" + matchStats.getScore().getFormattedScore() + ") "
                                + PageableMatchHistoryEmbed.getResultEmote(matchStats.getScore().getResult(), emoteHelper),
                        true
                )
                .build();
    }

    /**
     * Get the wobbly leaderboard rank for the given match stats
     *
     * @param matchStats Match stats to get wobbly rank for
     * @return Wobbly leaderboard rank
     */
    private int getWobblyRank(MatchStats matchStats) {
        for(int i = 0; i < leaderboard.size(); i++) {
            WobblyScore score = leaderboard.get(i);
            if(score.getKey().equals(matchStats.getId() + matchStats.getMainPlayer().getName())) {
                return i + 1;
            }
        }
        return 0;
    }

    /**
     * Create a message embed showing the loadouts used by the player in the given match
     *
     * @param matchStats Match stats to display loadouts from
     * @return Message embed showing player loadouts
     */
    private MessageEmbed buildMatchLoadoutEmbed(MatchStats matchStats) {
        MatchPlayer player = matchStats.getMainPlayer();
        int size = player.getLoadouts().length;
        String summary = "**Match Loadouts**: " + size;
        if(size > 5) {
            summary += " (A good builder would never need " + size + " sets of tools!)";
        }
        summary += "\n\nSome attachments haven't been mapped yet and they will be **RED**!";
        return getDefaultMatchEmbedBuilder(matchStats)
                .setTitle(
                        codManager.getGameName()
                                + " Match Loadouts: " + player.getName().toUpperCase()
                )
                .setDescription(summary)
                .setImage("attachment://image.png")
                .build();
    }

    /**
     * Get the colour to use in a Match embed based on the match result
     *
     * @param result Match result
     * @return Colour to use
     */
    public int getResultColour(MatchStats.RESULT result) {
        switch(result) {
            case WIN:
                return EmbedHelper.GREEN;
            case LOSS:
                return EmbedHelper.RED;
            case DRAW:
                return EmbedHelper.YELLOW;
            default:
                return EmbedHelper.PURPLE;
        }
    }

    /**
     * Build a message embed detailing an error which has occurred
     *
     * @param title Title to use for error embed
     * @param error Error which has occurred
     * @return Message embed detailing error
     */
    private MessageEmbed buildErrorEmbed(String title, String error) {
        return new EmbedBuilder()
                .setTitle(codManager.getGameName() + " Match History: " + title)
                .setThumbnail(thumbnail)
                .setColor(EmbedHelper.RED)
                .setDescription(error)
                .setFooter(footer, thumbnail)
                .build();
    }

    /**
     * Create the default embed builder for a match
     *
     * @param matchStats Match
     * @return Default embed builder for a match
     */
    private EmbedBuilder getDefaultMatchEmbedBuilder(MatchStats matchStats) {
        EmbedBuilder builder = new EmbedBuilder()
                .setFooter(footer, "attachment://image.png")
                .setThumbnail(matchStats.getMode().getImageURL())
                .setColor(getResultColour(matchStats.getScore().getResult()));

        Map map = matchStats.getMap();
        if(map.hasImageUrl()) {
            builder.setImage(map.getImageUrl());
        }

        return builder;
    }

    /**
     * Create the match history pageable embed
     *
     * @param context      Command context
     * @param matchHistory Player match history
     * @return Match history pageable embed
     */
    private PageableMatchHistoryEmbed getMatchHistoryEmbed(CommandContext context, MatchHistory matchHistory) {
        return new PageableMatchHistoryEmbed(
                context,
                matchHistory,
                codManager.getGameName(),
                thumbnail,
                getTrigger()
        );
    }

    /**
     * Get a player's match history.
     * If any errors occur, send a message to the given channel detailing the error.
     *
     * @param name     Player name
     * @param platform Player platform
     * @param channel  Channel to send errors to
     * @return Match history or null
     */
    @Nullable
    private MatchHistory getMatchHistory(String name, PLATFORM platform, MessageChannel channel) {
        final String json = getMatchHistoryJSON(name, platform);

        // No response, try the tracker
        if(json == null || new JSONObject(json).has(STATUS_KEY)) {
            final JSONArray trackerJson = getTrackerMatchHistoryJson(name, platform);
            if(trackerJson == null) {
                channel.sendMessage(
                        buildErrorEmbed(
                                "FAIL",
                                json == null
                                        ? "I was unable to get the API on the phone!"
                                        : new JSONObject(json).getString(STATUS_KEY)
                        )
                ).queue();
                return null;
            }
            return trackerParser.parseMatchHistory(name, platform, trackerJson);
        }

        return apiParser.parseMatchHistory(name, platform, new JSONObject(json));
    }

    /**
     * Add any unseen matches to the wobbly scores
     *
     * @param matches List of match stats
     */
    private void addMissingWobblyScores(ArrayList<MatchStats> matches) {
        ArrayList<WobblyScore> scores = new ArrayList<>();

        for(MatchStats matchStats : matches) {
            MatchPlayer player = matchStats.getMainPlayer();

            WobblyScore wobblyScore = new WobblyScore(
                    player.getWobblies(),
                    player.getMetres(),
                    player.getName(),
                    matchStats.getStart().getTime(),
                    matchStats.getMap(),
                    matchStats.getMode(),
                    matchStats.getId(),
                    codManager.getGameId()
            );

            if(leaderboardSeen.contains(wobblyScore.getKey())) {
                continue;
            }

            scores.add(wobblyScore);
            leaderboardSeen.add(wobblyScore.getKey());
            leaderboard.add(wobblyScore);
        }

        // No new scores to add
        if(scores.isEmpty()) {
            return;
        }

        DiscordUser.addWobbliesToLeaderboard(scores);
        WobblyScore.sortLeaderboard(leaderboard, true);
    }

    /**
     * Get the match history JSON from the API
     *
     * @param name     Player name
     * @param platform Player platform
     * @return Match history JSON or null
     */
    @Nullable
    public abstract String getMatchHistoryJSON(String name, PLATFORM platform);

    /**
     * Get the JSON of a specific match from the API
     *
     * @param matchId  Match ID
     * @param platform Player platform
     * @return Match JSON or null
     */
    @Nullable
    public abstract String getSpecificMatchJSON(String matchId, PLATFORM platform);

    /**
     * Get the match history JSON from the tracker API
     *
     * @param name     Player name
     * @param platform Player platform
     * @return Match history JSON or null
     */
    @Nullable
    public abstract JSONArray getTrackerMatchHistoryJson(String name, PLATFORM platform);

    @Override
    public void onReady(JDA jda, EmoteHelper emoteHelper) {
        this.emoteHelper = emoteHelper;
        this.stats = Button.primary(STATS_BUTTON_ID, Emoji.ofEmote(emoteHelper.getStats()));
        this.loadouts = Button.primary("loadouts", Emoji.ofEmote(emoteHelper.getLoadouts()));
        jda.addEventListener(getMatchButtonListener());
    }

    @Override
    public String getSavedName(long id) {
        return DiscordUser.getSavedName(id, codManager.getGameId());
    }

    @Override
    public void saveName(String name, MessageChannel channel, User user) {
        DiscordUser.saveName(name, codManager.getGameId(), channel, user);
    }
}
