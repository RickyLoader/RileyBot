package Command.Structure;

import Bot.DiscordUser;
import Bot.FontManager;
import COD.*;
import COD.Assets.*;
import COD.Match.*;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.interactions.ActionRow;
import net.dv8tion.jda.api.interactions.button.Button;
import net.dv8tion.jda.api.requests.restaction.MessageAction;
import org.jetbrains.annotations.NotNull;
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
public class MatchHistoryCommand extends CODLookupCommand {
    public static final LoadoutImageManager loadoutImageManager = new LoadoutImageManager();
    private static final String LATEST = "latest";
    private final HashMap<Long, MatchStats> matchMessages;
    private final HashSet<String> leaderboardSeen;
    private final ArrayList<WobblyScore> leaderboard;
    private final CODManager codManager;
    private final Font font;
    private final String footer, statsId, defaultButtonId;
    private Button stats, loadouts;
    private String matchID;
    private EmoteHelper emoteHelper;

    /**
     * Create the command
     *
     * @param trigger    Command trigger
     * @param codManager COD asset manager
     */
    public MatchHistoryCommand(String trigger, CODManager codManager) {
        super(
                trigger,
                "Have a gander at a player's match history!",
                getHelpText(trigger) + " [match id/" + LATEST + "]\n\n"
                        + trigger + " missing\n"
                        + trigger + " wobblies\n"
                        + trigger + " wobblies [rank]"
        );
        this.matchMessages = new HashMap<>();
        this.codManager = codManager;
        this.footer = "Type " + getTrigger() + " for help";
        this.leaderboard = getWobblyLeaderboard();
        this.leaderboardSeen = leaderboard
                .stream()
                .map(WobblyScore::getKey)
                .collect(Collectors.toCollection(HashSet::new));
        this.font = codManager.getGame() == CODManager.GAME.MW
                ? FontManager.MODERN_WARFARE_FONT
                : FontManager.COLD_WAR_FONT;
        this.statsId = "stats";
        this.defaultButtonId = statsId; // Stats page is displayed first
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
            matchID = lastArg;
            return query.replace(lastArg, "").trim();
        }
        matchID = null;
        return query;
    }

    @Override
    public String getSavedName(long id) {
        return DiscordUser.getSavedName(
                id,
                codManager.getGame() == CODManager.GAME.MW ? DiscordUser.MW : DiscordUser.CW
        );
    }

    @Override
    public void saveName(String name, MessageChannel channel, User user) {
        DiscordUser.saveName(
                name,
                codManager.getGame() == CODManager.GAME.MW ? DiscordUser.MW : DiscordUser.CW,
                channel,
                user
        );
    }

    @Override
    public void onArgumentsSet(String name, CommandContext context) {
        if(emoteHelper == null) {
            this.emoteHelper = context.getEmoteHelper();
            this.stats = Button.primary(statsId, Emoji.ofEmote(emoteHelper.getStats()));
            this.loadouts = Button.primary("loadouts", Emoji.ofEmote(emoteHelper.getLoadouts()));
        }

        context.getJDA().addEventListener(getMatchButtonListener());

        MessageChannel channel = context.getMessageChannel();
        channel.sendTyping().queue();
        if(name.equals("missing")) {
            ArrayList<MissingWeaponAttachments> missing = MissingWeaponAttachments.getMissingAttachments();
            if(missing.isEmpty()) {
                channel.sendMessage("No missing attachments!").queue();
            }
            else {
                showMissingAttachments(context, missing);
            }
            return;
        }

        if(name.equals("wobblies")) {
            if(matchID != null) {
                showSpecificWobblyScore(channel);
                return;
            }
            showWobblyLeaderboard(context);
            return;
        }
        MatchHistory matchHistory = getMatchHistory(name, getPlatform(), channel);
        if(matchHistory == null) {
            return;
        }
        if(matchID != null) {
            sendMatchEmbed(matchHistory, channel);
            return;
        }
        getMatchHistoryEmbed(context, matchHistory).showMessage();
    }

    /**
     * Show a specific entry on the wobbly leaderboard
     *
     * @param channel Channel to send entry to
     */
    private void showSpecificWobblyScore(MessageChannel channel) {
        int index = 0;
        try {
            index = Integer.parseInt(matchID) - 1;
        }
        catch(NumberFormatException e) {
            e.printStackTrace();
        }
        if(index < 0 || index > leaderboard.size() - 1) {
            channel.sendMessage("That's not a rank!").queue();
            return;
        }
        WobblyScore score = leaderboard.get(index);
        MessageEmbed entryEmbed = new EmbedBuilder()
                .setTitle(codManager.getGame().name().toUpperCase() + " Wobbly Rank #" + (index + 1))
                .setThumbnail(getEmbedThumbnail())
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
                .setColor(EmbedHelper.PURPLE)
                .setImage(score.getMap().getImageUrl())
                .build();
        channel.sendMessage(entryEmbed).queue();
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
                            + codManager.getGame().name().toUpperCase() + "!"
            ).queue();
            return;
        }
        new PageableTableEmbed<WobblyScore>(
                context,
                leaderboard,
                getEmbedThumbnail(),
                codManager.getGame().name().toUpperCase() + " Wobbly Leaderboard",
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
        String json = DiscordUser.getWobbliesLeaderboard(codManager.getGame());
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
     * Show the missing weapon attachments in a pageable embed
     *
     * @param context Command context
     * @param missing Missing weapon attachments
     */
    private void showMissingAttachments(CommandContext context, ArrayList<MissingWeaponAttachments> missing) {
        int total = (int) missing.stream().mapToLong(m -> m.attachmentCodenames.size()).sum();
        new PageableSortEmbed<MissingWeaponAttachments>(
                context,
                missing,
                1
        ) {
            @Override
            public EmbedBuilder getEmbedBuilder(String pageDetails) {
                return new EmbedBuilder()
                        .setThumbnail(getEmbedThumbnail())
                        .setTitle(total + " Missing attachments (" + missing.size() + " Weapons)")
                        .setFooter(pageDetails + " | Type: " + getTrigger() + " for help");
            }

            @Override
            public void displayItem(EmbedBuilder builder, int currentIndex, MissingWeaponAttachments current) {
                Weapon weapon = current.getWeapon();
                ArrayList<String> attachments = current.getAttachmentCodenames();
                StringBuilder description = new StringBuilder();
                description
                        .append("**Weapon**: ")
                        .append(weapon.getName()).append(" (").append(weapon.getCodename()).append(")")
                        .append("\n")
                        .append("**Missing**: ").append(attachments.size()).append(" attachments")
                        .append("\n\n");

                for(int i = 0; i < attachments.size(); i++) {
                    description.append(i + 1).append(". ").append(attachments.get(i));
                    if(i < attachments.size() - 1) {
                        description.append("\n");
                    }
                }
                builder
                        .setDescription(description.toString())
                        .setImage(weapon.getImageURL());
            }

            @Override
            protected MessageEmbed getNoItemsEmbed() {
                return getEmbedBuilder(getPageDetails()).setDescription("There are no missing attachments!").build();
            }

            @Override
            public boolean nonPagingButtonPressed(String buttonId) {
                return false;
            }

            @Override
            public void sortItems(List<MissingWeaponAttachments> items, boolean defaultSort) {
                items.sort((o1, o2) -> {
                    String n1 = o1.getWeapon().getName();
                    String n2 = o2.getWeapon().getName();
                    return defaultSort ? n1.compareTo(n2) : n2.compareTo(n1);
                });
            }
        }.showMessage();
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
     * Attach emotes if the match has player information to toggle between
     * match stats and player information when clicked
     *
     * @param matchHistory Match history of player
     * @param channel      Channel to send to
     */
    private void sendMatchEmbed(MatchHistory matchHistory, MessageChannel channel) {
        MatchStats matchStats = matchID.equals(LATEST)
                ? matchHistory.getMatches().get(0)
                : matchHistory.getMatch(matchID);
        if(matchStats == null) {
            channel.sendMessage(
                    buildErrorEmbed(
                            "Error Fetching Match",
                            "No match found with id: **" + matchID + "**" +
                                    " for player: **" + matchHistory.getName().toUpperCase() + "**"
                    )
            ).queue();
            return;
        }

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
                        codManager.getGame().name().toUpperCase()
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
                        matchStats.getResult().toString()
                                + " (" + matchStats.getScore() + ") "
                                + PageableMatchHistoryEmbed.getResultEmote(matchStats.getResult(), emoteHelper),
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
                        codManager.getGame().name().toUpperCase()
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
                .setTitle(codManager.getGame().name().toUpperCase() + " Match History: " + title)
                .setThumbnail(getEmbedThumbnail())
                .setColor(EmbedHelper.RED)
                .setDescription(error)
                .setFooter(footer, getEmbedThumbnail())
                .build();
    }

    /**
     * Create the default embed builder for a match
     *
     * @param matchStats Match
     * @return Default embed builder for a match
     */
    private EmbedBuilder getDefaultMatchEmbedBuilder(MatchStats matchStats) {
        return new EmbedBuilder()
                .setFooter(footer, "attachment://image.png")
                .setThumbnail(matchStats.getMode().getImageURL())
                .setColor(getResultColour(matchStats.getResult()))
                .setImage(matchStats.getMap().getImageUrl());
    }

    /**
     * Get the thumbnail to use in the embed
     *
     * @return Embed thumbnail
     */
    private String getEmbedThumbnail() {
        return codManager.getGame() == CODManager.GAME.MW
                ? MWManager.THUMBNAIL
                : CWManager.THUMBNAIL;
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
                codManager.getGame(),
                getEmbedThumbnail(),
                getTrigger()
        );
    }

    /**
     * Get the player's match history
     *
     * @param name     Player name
     * @param platform Player platform
     * @param channel  Channel to send errors to
     * @return Match history
     */
    private MatchHistory getMatchHistory(String name, PLATFORM platform, MessageChannel channel) {
        ArrayList<MatchStats> matchStats = new ArrayList<>();
        JSONObject overview = new JSONObject(getMatchHistoryJSON(name, platform));
        if(overview.has("status")) {
            channel.sendMessage(
                    buildErrorEmbed(
                            "Error Fetching Player: " + name,
                            overview.getString("status")
                    )
            ).queue();
            return null;
        }

        JSONArray matchList = overview.getJSONArray("matches");
        JSONObject summary = overview.getJSONObject("summary").getJSONObject("all");
        ArrayList<WobblyScore> scores = new ArrayList<>();
        for(int i = 0; i < matchList.length(); i++) {
            JSONObject matchData = matchList.getJSONObject(i);
            MatchPlayer player = parseMatchPlayer(matchData, new MatchPlayer.MatchPlayerBuilder(name, platform));
            MatchStats.RESULT result = parseResult(
                    (!matchData.getBoolean("isPresentAtEnd") || matchData.isNull("result")) ? "FORFEIT" : matchData.getString("result")
            );

            MatchStats match = new MatchStats(
                    matchData.getString("matchID"),
                    codManager.getMapByCodename(matchData.getString("map")),
                    codManager.getModeByCodename(matchData.getString("mode")),
                    new Date(matchData.getLong("utcStartSeconds") * 1000),
                    new Date(matchData.getLong("utcEndSeconds") * 1000),
                    result,
                    player,
                    new Score(
                            matchData.getInt("team1Score"),
                            matchData.getInt("team2Score"),
                            result
                    )
            );
            matchStats.add(match);
            WobblyScore wobblyScore = new WobblyScore(
                    player.getWobblies(),
                    player.getMetres(),
                    player.getName(),
                    match.getStart().getTime(),
                    match.getMap(),
                    match.getMode(),
                    match.getId(),
                    codManager.getGame()
            );
            if(leaderboardSeen.contains(wobblyScore.getKey())) {
                continue;
            }
            scores.add(wobblyScore);
            leaderboardSeen.add(wobblyScore.getKey());
            leaderboard.add(wobblyScore);
        }
        if(!scores.isEmpty()) {
            DiscordUser.addWobbliesToLeaderboard(scores);
            WobblyScore.sortLeaderboard(leaderboard, true);
        }
        return new MatchHistory(
                name,
                matchStats,
                new Ratio(
                        summary.getInt("kills"),
                        summary.getInt("deaths")
                ),
                font
        );
    }

    /**
     * Parse a match player from the given match JSON
     *
     * @param matchData Match JSON
     * @param builder   Player builder
     * @return Match player
     */
    private MatchPlayer parseMatchPlayer(JSONObject matchData, MatchPlayer.MatchPlayerBuilder builder) {
        JSONObject player = matchData.getJSONObject("player");
        JSONObject playerStats = matchData.getJSONObject("playerStats");
        builder.setTimePlayed(playerStats.getLong("timePlayed") * 1000)
                .setKD(
                        new Ratio(
                                playerStats.getInt("kills"),
                                playerStats.getInt("deaths")
                        )
                )
                .setAccuracy(
                        playerStats.has("shotsLanded") ? new Ratio(
                                playerStats.getInt("shotsLanded"),
                                playerStats.getInt("shotsFired")
                        ) : null
                )
                .setTeam(getOptionalString(player, "team"))
                .setUno(getOptionalString(player, "uno"))
                .setNemesis(getOptionalString(player, "nemesis"))
                .setMostKilled(getOptionalString(player, "mostKilled"))
                .setLongestStreak(getLongestStreak(playerStats))
                .setDamageDealt(getDamageDealt(playerStats))
                .setDamageReceived(getOptionalInt(playerStats, "damageTaken"))
                .setXP(getOptionalInt(playerStats, "matchXp"))
                .setDistanceTravelled(getOptionalInt(playerStats, "distanceTraveled"))
                .setLoadouts(parseLoadouts(player.getJSONArray("loadout")));

        if(playerStats.has("percentTimeMoving")) {
            builder.setPercentTimeMoving(playerStats.getDouble("percentTimeMoving"));
        }

        return builder.build();
    }

    /**
     * Parse the provided loadout JSON into an array of loadouts
     *
     * @param loadoutList JSON loadout array
     * @return Array of player loadouts
     */
    private Loadout[] parseLoadouts(JSONArray loadoutList) {
        ArrayList<Loadout> loadouts = new ArrayList<>();
        for(int i = 0; i < loadoutList.length(); i++) {
            JSONObject loadoutData = loadoutList.getJSONObject(i);
            loadouts.add(
                    new Loadout.LoadoutBuilder()
                            .setPrimaryWeapon(parseLoadoutWeapon(loadoutData.getJSONObject("primaryWeapon")))
                            .setSecondaryWeapon(parseLoadoutWeapon(loadoutData.getJSONObject("secondaryWeapon")))
                            .setPerks(parsePerks(loadoutData.getJSONArray("perks")))
                            .setLethalEquipment(
                                    parseWeapon(
                                            loadoutData.getJSONObject("lethal"),
                                            Weapon.CATEGORY.LETHALS
                                    )
                            )
                            .setTacticalEquipment(
                                    (TacticalWeapon) parseWeapon(
                                            loadoutData.getJSONObject("tactical"),
                                            Weapon.CATEGORY.TACTICALS
                                    )
                            )
                            .build()
            );
        }
        return loadouts.stream().distinct().toArray(Loadout[]::new);
    }

    /**
     * Parse an array of perks from the match loadout JSON
     *
     * @param perkJSONArray JSON array of perks
     * @return Array of perks
     */
    private Perk[] parsePerks(JSONArray perkJSONArray) {
        ArrayList<Perk> perks = new ArrayList<>();
        for(int i = 0; i < perkJSONArray.length(); i++) {
            String codename = perkJSONArray.getJSONObject(i).getString("name");
            if(codename.equals("specialty_null")) {
                continue;
            }
            perks.add(codManager.getPerkByCodename(codename));
        }
        return perks.toArray(new Perk[0]);
    }

    /**
     * Parse a weapon from the match loadout weapon JSON
     *
     * @param loadoutWeapon Match loadout weapon JSON
     * @param category      Weapon category
     * @return Weapon
     */
    private Weapon parseWeapon(JSONObject loadoutWeapon, Weapon.CATEGORY category) {
        String codename = loadoutWeapon.getString("name");
        if(codename.equals("none")) {
            return null;
        }
        if(category == Weapon.CATEGORY.UNKNOWN) {
            category = Weapon.getCategoryFromWeaponCodename(codename);
        }
        return codManager.getWeaponByCodename(codename, category);
    }

    /**
     * Parse a loadout weapon from the match loadout weapon JSON
     *
     * @param loadoutWeapon Match loadout weapon JSON
     * @return Loadout weapon containing weapon & attachments
     */
    private LoadoutWeapon parseLoadoutWeapon(JSONObject loadoutWeapon) {
        Weapon weapon = parseWeapon(loadoutWeapon, Weapon.CATEGORY.UNKNOWN);
        if(weapon == null) {
            return null;
        }
        ArrayList<Attachment> attachments = new ArrayList<>();
        if(loadoutWeapon.has("attachments")) {
            JSONArray attachmentData = loadoutWeapon.getJSONArray("attachments");
            for(int i = 0; i < attachmentData.length(); i++) {
                String attachmentName = attachmentData.getJSONObject(i).getString("name");
                if(attachmentName.equals("none")) {
                    continue;
                }
                Attachment attachment = weapon.getAttachmentByCodename(attachmentName);
                if(attachment == null) {
                    MissingWeaponAttachments.addMissingAttachment(attachmentName, weapon.getCodename());
                    attachment = new Attachment(
                            attachmentName,
                            "MISSING: " + attachmentName,
                            Attachment.CATEGORY.UNKNOWN,
                            Attachment.CATEGORY.UNKNOWN,
                            null,
                            null
                    );
                }
                attachments.add(attachment);
            }
        }
        return new LoadoutWeapon(weapon, attachments, loadoutWeapon.getInt("variant"));
    }

    /**
     * Get the match history JSON
     *
     * @param name     Player name
     * @param platform Player platform
     * @return Match history JSON
     */
    private String getMatchHistoryJSON(String name, PLATFORM platform) {
        return codManager.getGame() == CODManager.GAME.MW
                ? CODAPI.getMWMatchHistory(name, platform)
                : CODAPI.getCWMatchHistory(name, platform);
    }

    /**
     * Get an optional integer value from the player stats match JSON
     * Return 0 if absent
     *
     * @param playerStats Player stats match JSON
     * @param key         Value key
     * @return Value or 0
     */
    private int getOptionalInt(JSONObject playerStats, String key) {
        return playerStats.has(key) ? playerStats.getInt(key) : 0;
    }

    /**
     * Get an optional String value from the player stats match JSON
     * Return '-' if absent
     *
     * @param playerStats Player stats match JSON
     * @param key         Value key
     * @return Value or '-'
     */
    private String getOptionalString(JSONObject playerStats, String key) {
        return playerStats.has(key) && !playerStats.getString(key).isEmpty() ? playerStats.getString(key) : "-";
    }

    /**
     * Get the damage dealt value from the match JSON
     * The key varies by game
     *
     * @param playerStats Player stats match JSON
     * @return Damage dealt
     */
    private int getDamageDealt(JSONObject playerStats) {
        return Math.max(
                getOptionalInt(playerStats, "damageDone"),
                getOptionalInt(playerStats, "damageDealt")
        );
    }

    /**
     * Get the longest streak value from the match JSON
     * The key varies by game
     *
     * @param playerStats Player stats match JSON
     * @return Longest streak
     */
    private int getLongestStreak(JSONObject playerStats) {
        return Math.max(
                getOptionalInt(playerStats, "longestStreak"),
                getOptionalInt(playerStats, "highestStreak")
        );
    }

    /**
     * Parse the result of the match from the given String
     *
     * @param result Result of match - win, loss, draw, forfeit
     * @return Match result
     */
    private MatchStats.RESULT parseResult(String result) {
        switch(result.toLowerCase()) {
            case "win":
                return MatchStats.RESULT.WIN;
            case "loss":
            case "lose":
                return MatchStats.RESULT.LOSS;
            case "forfeit":
                return MatchStats.RESULT.FORFEIT;
            default:
                return MatchStats.RESULT.DRAW;
        }
    }
}
