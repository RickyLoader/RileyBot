package Command.Structure;

import Bot.DiscordUser;
import Bot.FontManager;
import COD.Assets.*;
import COD.Assets.Map;
import COD.CODAPI;
import COD.CODManager;
import COD.Gunfight;
import COD.LoadoutImageManager;
import COD.Match.*;
import Command.Commands.COD.CWCountdownCommand;
import Command.Structure.PieChart.Section;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.requests.restaction.MessageAction;
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
    public final static LoadoutImageManager loadoutImageManager = new LoadoutImageManager();
    private final HashMap<Long, MatchStats> matchMessages;
    private final HashSet<String> leaderboardSeen;
    private final ArrayList<WobblyScore> leaderboard;
    private final CODManager codManager;
    private final Font font;
    private final String footer;
    private String win, loss, draw, matchID;
    private Emote stats, players, loadouts, switchImage;

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
                getHelpText(trigger) + " [match id/latest/maps/modes]\n\n"
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
    }

    @Override
    public String stripArguments(String query) {
        query = setPlatform(query);
        String[] args = query.split(" ");

        if(args.length == 1) {
            return query;
        }

        String lastArg = args[args.length - 1];
        if(lastArg.matches("\\d+") || lastArg.equals("latest") || lastArg.equals("maps") || lastArg.equals("modes")) {
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
        if(win == null) {
            EmoteHelper helper = context.getEmoteHelper();
            win = EmoteHelper.formatEmote(helper.getComplete());
            loss = EmoteHelper.formatEmote(helper.getFail());
            draw = EmoteHelper.formatEmote(helper.getDraw());
            stats = helper.getStats();
            players = helper.getPlayers();
            loadouts = helper.getLoadouts();
            switchImage = helper.getNextImage();
            context.getJDA().addEventListener(getMatchEmoteListener());
        }
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
            switch(matchID) {
                case "maps":
                    sendMapBreakdownEmbed(matchHistory, channel);
                    break;
                case "modes":
                    sendModeBreakdownEmbed(matchHistory, channel);
                    break;
                case "latest":
                default:
                    sendMatchEmbed(matchHistory, channel);
            }
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
        MessageEmbed entryEmbed = getDefaultEmbedBuilder(
                codManager.getGame().name().toUpperCase() + " Wobbly Rank #" + (index + 1)
        )
                .setDescription("Use **" + getTrigger() + " wobblies** to view the full leaderboard.")
                .addField("Name", score.getPlayerName(), true)
                .addField("Date", score.getDateString(), true)
                .addBlankField(true)
                .addField("Wobblies", MatchStats.formatDistance(score.getWobblies(), "wobblies"), true)
                .addField("Metres", MatchStats.formatDistance(score.getMetres(), "metres"), true)
                .addBlankField(true)
                .addField("Map", score.getMap().getName(), true)
                .addField("Mode", score.getMode().getName(), true)
                .addBlankField(true)
                .addField("Match ID", score.getMatchId(), true)
                .setColor(EmbedHelper.PURPLE)
                .setImage(score.getMap().getLoadingImageURL())
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
                    "There are no wobblies on the leaderboard for " + codManager.getGame().name().toUpperCase() + "!"
            ).queue();
            return;
        }
        new PageableTableEmbed(
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
            public String[] getRowValues(int index, List<?> items, boolean defaultSort) {
                WobblyScore score = (WobblyScore) items.get(index);
                int rank = defaultSort ? (index + 1) : (items.size() - index);
                return new String[]{
                        String.valueOf(rank),
                        score.getPlayerName(),
                        MatchStats.formatDistance(score.getWobblies(), "wobblies")
                };
            }

            @Override
            public void sortItems(List<?> items, boolean defaultSort) {
                ArrayList<WobblyScore> scores = new ArrayList<>();
                for(Object o : items) {
                    scores.add((WobblyScore) o);
                }
                WobblyScore.sortLeaderboard(scores, defaultSort);
                updateItems(scores);
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
     * Build and send a message embed breaking down the maps played in the match history
     *
     * @param matchHistory Player match history
     * @param channel      Channel to send embed to
     */
    private void sendMapBreakdownEmbed(MatchHistory matchHistory, MessageChannel channel) {
        ArrayList<Map> mapsPlayed = matchHistory
                .getMatches()
                .stream()
                .map(MatchStats::getMap).collect(Collectors.toCollection(ArrayList::new));
        HashMap<Map, Integer> mapFrequency = new HashMap<>();
        for(Map map : mapsPlayed) {
            if(!mapFrequency.containsKey(map)) {
                mapFrequency.put(map, 0);
            }
            mapFrequency.put(map, mapFrequency.get(map) + 1);
        }
        Section[] sections = mapFrequency
                .keySet()
                .stream()
                .map(m -> new Section(m.getName(), mapFrequency.get(m), EmbedHelper.getRandomColour()))
                .toArray(Section[]::new);
        sendBreakdownEmbed(sections, matchHistory, "Map", channel);
    }

    /**
     * Build and send a message embed breaking down the modes played in the match history
     *
     * @param matchHistory Player match history
     * @param channel      Channel to send embed to
     */
    private void sendModeBreakdownEmbed(MatchHistory matchHistory, MessageChannel channel) {
        ArrayList<Mode> modesPlayed = matchHistory
                .getMatches()
                .stream()
                .map(MatchStats::getMode).collect(Collectors.toCollection(ArrayList::new));
        HashMap<Mode, Integer> modeFrequency = new HashMap<>();
        for(Mode mode : modesPlayed) {
            if(!modeFrequency.containsKey(mode)) {
                modeFrequency.put(mode, 0);
            }
            modeFrequency.put(mode, modeFrequency.get(mode) + 1);
        }
        Section[] sections = modeFrequency
                .keySet()
                .stream()
                .map(m -> new Section(m.getName(), modeFrequency.get(m), EmbedHelper.getRandomColour()))
                .toArray(Section[]::new);
        sendBreakdownEmbed(sections, matchHistory, "Mode", channel);
    }

    /**
     * Build and send a message embed displaying a breakdown of the match history
     *
     * @param sections      Donut chart sections showing frequency of the breakdown items
     * @param breakdownItem Name of item being broken down
     * @param channel       Channel to send embed to
     */
    private void sendBreakdownEmbed(Section[] sections, MatchHistory matchHistory, String breakdownItem, MessageChannel channel) {
        PieChart pieChart = new PieChart(sections, font, false);
        MessageEmbed embed = getDefaultEmbedBuilder(getBreakdownEmbedTitle(matchHistory.getName(), breakdownItem))
                .setImage("attachment://image.png")
                .setDescription("Breakdown for the last " + matchHistory.getMatches().size() + " matches:")
                .setColor(EmbedHelper.GREEN)
                .build();

        channel.sendMessage(embed).
                addFile(ImageLoadingMessage.imageToByteArray(pieChart.getFullImage()), "image.png")
                .queue();
    }

    /**
     * Show the missing weapon attachments in a pageable embed
     *
     * @param context Command context
     * @param missing Missing weapon attachments
     */
    private void showMissingAttachments(CommandContext context, ArrayList<MissingWeaponAttachments> missing) {
        int total = (int) missing.stream().mapToLong(m -> m.attachmentCodenames.size()).sum();
        new PageableEmbed(
                context,
                missing,
                getEmbedThumbnail(),
                total + " Missing attachments (" + missing.size() + " Weapons)",
                null,
                "Type: " + getTrigger() + " for help",
                1,
                EmbedHelper.GREEN
        ) {
            @Override
            public void addFields(EmbedBuilder builder, int currentIndex) {
                MissingWeaponAttachments current = (MissingWeaponAttachments) getItems().get(currentIndex);
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
            public void sortItems(List<?> items, boolean defaultSort) {
                items.sort((o1, o2) -> {
                    String a = ((MissingWeaponAttachments) o1).getWeapon().getName();
                    String b = ((MissingWeaponAttachments) o2).getWeapon().getName();
                    if(defaultSort) {
                        return a.compareTo(b);
                    }
                    return b.compareTo(a);
                });
            }
        }.showMessage();
    }

    /**
     * Create an emote listener to handle toggling a match embed between match stats
     * and a list of players
     *
     * @return Emote listener
     */
    private EmoteListener getMatchEmoteListener() {
        return new EmoteListener() {
            private Emote last = stats;

            @Override
            public void handleReaction(MessageReaction reaction, User user, Guild guild) {
                long id = reaction.getMessageIdLong();
                Emote emote = reaction.getReactionEmote().getEmote();
                if(!matchMessages.containsKey(id) || (emote != stats && emote != players && emote != loadouts && emote != switchImage)) {
                    return;
                }
                reaction.getChannel().retrieveMessageById(id).queue(message -> {
                    MatchStats matchStats = matchMessages.get(id);
                    MessageEmbed content = null;
                    if(emote == stats) {
                        content = buildMatchEmbed(matchStats);
                    }
                    else if(emote == players) {
                        content = buildMatchPlayersEmbed(matchStats);
                    }
                    else if(emote == loadouts && matchStats.hasLoadouts()) {
                        content = buildMatchLoadoutEmbed(matchStats);
                    }
                    else if(emote == switchImage && last != loadouts) {
                        matchStats.switchDisplayImageURL();
                        content = last == stats ? buildMatchEmbed(matchStats) : buildMatchPlayersEmbed(matchStats);
                    }
                    if(content == null) {
                        return;
                    }
                    message.editMessage(content).queue(messageEdit -> {
                        if(emote != switchImage) {
                            last = emote;
                        }
                    });
                });
            }
        };
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
        MatchStats matchStats = matchID.equals("latest") ? matchHistory.getMatches().get(0) : matchHistory.getMatch(matchID);
        if(matchStats == null) {
            channel.sendMessage(
                    buildErrorEmbed(
                            matchHistory.getName(),
                            "No match found with id: **" + matchID + "**" +
                                    " for player: **" + matchHistory.getName().toUpperCase() + "**"
                    )
            ).queue();
            return;
        }

        MessageEmbed matchEmbed = buildMatchEmbed(matchStats);
        MessageAction sendMessage = channel.sendMessage(matchEmbed);
        Consumer<Message> callback = message -> {
            matchMessages.put(message.getIdLong(), matchStats);
            message.addReaction(stats).queue();
            message.addReaction(players).queue();
            if(matchStats.hasLoadouts()) {
                message.addReaction(loadouts).queue();
            }
            message.addReaction(switchImage).queue();
        };
        if(matchStats.hasLoadouts()) {
            matchStats.setLoadoutImage(buildLoadoutImage(matchStats.getLoadouts()));
            /*
             * Setting the attached loadout image file as the embed footer icon prevents it from displaying as
             * a separate message.
             * When viewing the loadout embed, both the embed footer icon & embed image can use the file.
             */
            sendMessage.addFile(matchStats.getLoadoutImage(), "image.png").queue(callback);
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
     * Attempt to get and add the team information for the provided match
     *
     * @param matchStats Match to add teams to
     */
    private void addTeams(MatchStats matchStats) {
        JSONObject matchDetails = new JSONObject(getMatchPlayersJSON(matchStats.getId(), getPlatform()));
        if(matchDetails.has("status")) {
            System.out.println("Error getting team data:" + matchDetails.getString("status"));
            return;
        }
        JSONArray playerList = matchDetails.getJSONArray("allPlayers");
        Team allies = new Team("Allies");
        Team axis = new Team("Axis");
        for(int i = 0; i < playerList.length(); i++) {
            JSONObject playerData = playerList.getJSONObject(i).getJSONObject("player");
            MatchPlayer player = new MatchPlayer(
                    playerData.getString("username"),
                    PLATFORM.byName(playerData.getString("platform"))
            );
            player.setUno(playerData.getString("uno"));
            if(playerData.getString("team").equals("allies")) {
                allies.addPlayer(player);
            }
            else {
                axis.addPlayer(player);
            }
            if(player.getPlatform() == PLATFORM.NONE) {
                System.out.println("Unable to match: " + playerData.getString("platform"));
            }
        }
        matchStats.setTeams(allies, axis);
    }

    /**
     * Create a message embed detailing a player's stats during the given match
     *
     * @param matchStats Match stats
     * @return Message embed detailing match stats
     */
    private MessageEmbed buildMatchEmbed(MatchStats matchStats) {
        int wobblyRank = getWobblyRank(matchStats);
        EmbedBuilder builder = getDefaultMatchEmbedBuilder(matchStats)
                .addField("**Date**", matchStats.getDateString(), true)
                .addField("**Time**", matchStats.getTimeString(), true)
                .addField("**Duration**", matchStats.getMatchDurationString(), true)
                .addField("**Mode**", matchStats.getMode().getName(), true)
                .addField("**Map**", matchStats.getMap().getName(), true);
        if(matchStats.playerCompleted()) {
            builder.addBlankField(true);
        }
        else {
            builder.addField("**Time Played**", matchStats.getTimePlayedString(), true);
        }
        return builder
                .addField("**K/D**", matchStats.getKillDeathSummary(), true)
                .addField("**Shots Fired/Hit**", matchStats.getShotSummary(), true)
                .addField("**Accuracy**", matchStats.getAccuracySummary(), true)
                .addField("**Damage Dealt**", matchStats.getDamageDealt(), true)
                .addField("**Damage Taken**", matchStats.getDamageReceived(), true)
                .addField("**Highest Streak**", String.valueOf(matchStats.getLongestStreak()), true)
                .addField(
                        "**Distance Travelled**",
                        matchStats.formatWobblies() + "\n" + matchStats.formatMetres(),
                        true
                )
                .addField("**Time Spent Moving**", matchStats.getPercentTimeMovingString(), true)
                .addField(
                        "**Wobbly Rank**",
                        wobblyRank == 0 ? "-" : String.valueOf(wobblyRank),
                        true
                )
                .addField("**Nemesis**", matchStats.getNemesis(), true)
                .addField("**Most Killed**", matchStats.getMostKilled(), true)
                .addBlankField(true)
                .addField("**Match XP**", matchStats.getExperience(), true)
                .addField(
                        "**Result**",
                        matchStats.getResult().toString()
                                + " (" + matchStats.getScore() + ") "
                                + getResultEmote(matchStats.getResult()),
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
            if(score.getKey().equals(matchStats.getId() + matchStats.getPlayer().getName())) {
                return i + 1;
            }
        }
        return 0;
    }

    /**
     * Create a message embed showing the list of players in the given match
     *
     * @param matchStats Match stats to display players from
     * @return Message embed showing list of match players
     */
    private MessageEmbed buildMatchPlayersEmbed(MatchStats matchStats) {
        if(!matchStats.hasTeams()) {
            addTeams(matchStats);
        }
        EmbedBuilder builder = getDefaultMatchEmbedBuilder(matchStats);
        addTeamToEmbed(matchStats.getTeam1(), builder);
        addTeamToEmbed(matchStats.getTeam2(), builder);
        return builder.build();
    }

    /**
     * Create a message embed showing the loadouts used by the player in the given match
     *
     * @param matchStats Match stats to display loadouts from
     * @return Message embed showing player loadouts
     */
    private MessageEmbed buildMatchLoadoutEmbed(MatchStats matchStats) {
        int size = matchStats.getLoadouts().length;
        String summary = "**Match Loadouts**: " + size;
        if(size > 5) {
            summary += " (A good builder would never need " + size + " sets of tools!)";
        }
        summary += "\n\nSome attachments haven't been mapped yet and they will be **RED**!";
        return getDefaultMatchEmbedBuilder(matchStats)
                .setDescription(summary)
                .setImage("attachment://image.png")
                .build();
    }

    /**
     * Add the given team to the embed builder
     *
     * @param team    Team to add to embed builder
     * @param builder Embed builder with team name and players
     */
    private void addTeamToEmbed(Team team, EmbedBuilder builder) {
        ArrayList<MatchPlayer> players = team.getPlayers();
        String teamName = "__" + team.getName().toUpperCase() + "__";
        if(players.isEmpty()) {
            builder.addField(teamName, "No players found!", true);
            return;
        }
        for(int i = 0; i < players.size(); i++) {
            MatchPlayer player = players.get(i);
            String value = "**" + player.getName() + "**"
                    + "\n" + player.getPlatform().name()
                    + "\n#" + player.getUno();
            builder.addField(i == 0
                    ? EmbedHelper.getTitleField(teamName, value)
                    : EmbedHelper.getValueField(value)
            );
        }
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
     * Get the result formatted for use in a message embed with an emote and score
     *
     * @param matchStats Match
     * @return Formatted result
     */
    public String getFormattedResult(MatchStats matchStats) {
        MatchStats.RESULT result = matchStats.getResult();
        return result.toString() + " " + getResultEmote(result) + "\n(" + matchStats.getScore() + ")";
    }

    /**
     * Get the emote to use for the match result
     *
     * @param result Match result
     * @return Emote indicating the result of the match
     */
    public String getResultEmote(MatchStats.RESULT result) {
        switch(result) {
            case WIN:
                return win;
            case LOSS:
                return loss;
            default:
                return draw;
        }
    }

    /**
     * Build a message embed detailing an error which has occurred
     *
     * @param name  Player name to use in embed title
     * @param error Error which has occurred
     * @return Message embed detailing error
     */
    private MessageEmbed buildErrorEmbed(String name, String error) {
        return getDefaultEmbedBuilder(getSummaryEmbedTitle(name.toUpperCase()))
                .setColor(EmbedHelper.RED)
                .setDescription(error)
                .build();
    }

    /**
     * Create the default embed builder
     *
     * @param title       Title to use
     * @param footerImage Footer image
     * @return Default embed builder
     */
    private EmbedBuilder getDefaultEmbedBuilder(String title, String footerImage) {
        return new EmbedBuilder()
                .setTitle(title)
                .setFooter(footer, footerImage)
                .setThumbnail(getEmbedThumbnail());
    }

    /**
     * Create the default embed builder with the default game thumbnail
     *
     * @param title Title to use in embed
     * @return Default embed builder
     */
    private EmbedBuilder getDefaultEmbedBuilder(String title) {
        return getDefaultEmbedBuilder(title, getEmbedThumbnail());
    }

    /**
     * Create the default embed builder for a match
     *
     * @param matchStats Match
     * @return Default embed builder for a match
     */
    private EmbedBuilder getDefaultMatchEmbedBuilder(MatchStats matchStats) {
        return getDefaultEmbedBuilder(
                getSummaryEmbedTitle(matchStats.getPlayer().getName().toUpperCase()),
                "attachment://image.png"
        )
                .setColor(getResultColour(matchStats.getResult()))
                .setImage(matchStats.getDisplayImageURL());
    }

    /**
     * Get the title to use for the match summary embed
     *
     * @param name Player name
     * @return Embed title
     */
    public String getSummaryEmbedTitle(String name) {
        return codManager.getGame().name().toUpperCase() + " Match Summary: " + name.toUpperCase();
    }

    /**
     * Get the title to use for the match history embed
     *
     * @param name Player name
     * @return Embed title
     */
    private String getHistoryEmbedTitle(String name) {
        return codManager.getGame().name().toUpperCase() + " Match History: " + name.toUpperCase();
    }

    /**
     * Get the title to use for breakdown embeds
     *
     * @param name          Player name
     * @param breakdownItem Item that is being broken down e.g - "map"
     * @return Embed title
     */
    private String getBreakdownEmbedTitle(String name, String breakdownItem) {
        return codManager.getGame().name().toUpperCase()
                + " Match History " + breakdownItem + " Breakdown: " + name.toUpperCase();
    }

    /**
     * Get the thumbnail to use in the embed
     *
     * @return Embed thumbnail
     */
    private String getEmbedThumbnail() {
        return codManager.getGame() == CODManager.GAME.MW ? Gunfight.thumbnail : CWCountdownCommand.thumbnail;
    }

    /**
     * Create the match history pageable embed
     *
     * @param context      Command context
     * @param matchHistory Player match history
     * @return Match history pageable embed
     */
    private PageableTableEmbed getMatchHistoryEmbed(CommandContext context, MatchHistory matchHistory) {
        return new PageableTableEmbed(
                context,
                matchHistory.getMatches(),
                getEmbedThumbnail(),
                getHistoryEmbedTitle(matchHistory.getName().toUpperCase()),
                matchHistory.getSummary(),
                "Type: " + getTrigger() + " for help",
                new String[]{"Match", "Details", "Result"},
                3
        ) {
            @Override
            public String[] getRowValues(int index, List<?> items, boolean defaultSort) {
                MatchStats matchStats = (MatchStats) items.get(index);
                int position = defaultSort ? (index + 1) : (items.size() - index);
                return new String[]{
                        String.valueOf(position),
                        matchStats.getMatchSummary(),
                        getFormattedResult(matchStats)
                };
            }

            @Override
            public void sortItems(List<?> items, boolean defaultSort) {
                items.sort((Comparator<Object>) (o1, o2) -> {
                    Date d1 = ((MatchStats) o1).getStart();
                    Date d2 = ((MatchStats) o2).getStart();
                    return defaultSort ? d2.compareTo(d1) : d1.compareTo(d2);
                });
            }
        };
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
        JSONObject overview = new JSONObject(getMatchHistoryJSON(getLookupName(), platform));
        if(overview.has("status")) {
            channel.sendMessage(buildErrorEmbed(name, overview.getString("status"))).queue();
            return null;
        }

        JSONArray matchList = overview.getJSONArray("matches");
        JSONObject summary = overview.getJSONObject("summary").getJSONObject("all");
        MatchPlayer player = new MatchPlayer(name, platform);
        ArrayList<WobblyScore> scores = new ArrayList<>();

        for(int i = 0; i < matchList.length(); i++) {
            JSONObject match = matchList.getJSONObject(i);
            JSONObject playerStats = match.getJSONObject("playerStats");
            JSONObject playerSummary = match.getJSONObject("player");
            MatchStats.RESULT result = parseResult(
                    (!match.getBoolean("isPresentAtEnd") || match.isNull("result")) ? "FORFEIT" : match.getString("result")
            );

            MatchStats.MatchBuilder matchBuilder = new MatchStats.MatchBuilder(
                    match.getString("matchID"),
                    codManager.getMapByCodename(match.getString("map")),
                    codManager.getModeByCodename(match.getString("mode")),
                    new Date(match.getLong("utcStartSeconds") * 1000),
                    new Date(match.getLong("utcEndSeconds") * 1000),
                    playerStats.getLong("timePlayed") * 1000,
                    result,
                    player
            );

            matchBuilder
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
                    .setMatchScore(
                            new Score(
                                    match.getInt("team1Score"),
                                    match.getInt("team2Score"),
                                    result
                            )
                    )
                    .setNemesis(getOptionalString(playerSummary, "nemesis"))
                    .setMostKilled(getOptionalString(playerSummary, "mostKilled"))
                    .setLongestStreak(getLongestStreak(playerStats))
                    .setDamageDealt(getDamageDealt(playerStats))
                    .setDamageReceived(getOptionalInt(playerStats, "damageTaken"))
                    .setXP(getOptionalInt(playerStats, "matchXp"))
                    .setDistanceTravelled(getOptionalInt(playerStats, "distanceTraveled"))
                    .setLoadouts(parseLoadouts(playerSummary));
            if(playerStats.has("percentTimeMoving")) {
                matchBuilder.setPercentTimeMoving(playerStats.getDouble("percentTimeMoving"));
            }
            MatchStats stats = matchBuilder.build();
            matchStats.add(stats);
            WobblyScore wobblyScore = new WobblyScore(
                    stats.getWobblies(),
                    stats.getMetres(),
                    stats.getPlayer().getName(),
                    stats.getStart().getTime(),
                    stats.getMap(),
                    stats.getMode(),
                    stats.getId(),
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
                )
        );
    }

    /**
     * Parse the provided match JSON for the player loadouts
     *
     * @param playerSummary Player match summary JSON
     * @return Array of player loadouts
     */
    private Loadout[] parseLoadouts(JSONObject playerSummary) {
        JSONArray loadoutList = playerSummary.getJSONArray("loadout");
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
     * Get the match players JSON
     *
     * @param matchID  Match id
     * @param platform Match platform
     * @return Match players JSON
     */
    private String getMatchPlayersJSON(String matchID, PLATFORM platform) {
        return codManager.getGame() == CODManager.GAME.MW
                ? CODAPI.getMWMatchPlayers(matchID, platform)
                : CODAPI.getCWMatchPlayers(matchID, platform);
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
