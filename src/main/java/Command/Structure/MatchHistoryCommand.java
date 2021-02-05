package Command.Structure;

import Bot.DiscordCommandManager;
import COD.Assets.*;
import COD.LoadoutImageManager;
import COD.Match.*;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;
import org.json.JSONArray;
import org.json.JSONObject;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;

/**
 * View a COD player's match history
 */
public abstract class MatchHistoryCommand extends CODLookupCommand {
    private final HashMap<Long, MatchStats> matchMessages;
    private final LoadoutImageManager loadoutImageManager;
    private String win, loss, draw, matchID;
    private Emote stats, players, loadouts;

    /**
     * Create the command
     *
     * @param trigger Command trigger
     */
    public MatchHistoryCommand(String trigger) {
        super(
                trigger,
                "Have a gander at a player's match history!",
                getHelpText(trigger) + " [match id/latest]"
        );
        this.matchMessages = new HashMap<>();
        this.loadoutImageManager = new LoadoutImageManager();
    }

    @Override
    public String stripArguments(String query) {
        query = setPlatform(query);
        String[] args = query.split(" ");

        if(args.length == 1) {
            return query;
        }

        String lastArg = args[args.length - 1];
        if(lastArg.matches("\\d+") || lastArg.equals("latest")) {
            matchID = lastArg;
            return query.replace(lastArg, "").trim();
        }
        matchID = null;
        return query;
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
            context.getJDA().addEventListener(getMatchEmoteListener());
        }
        MessageChannel channel = context.getMessageChannel();
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

        channel.sendTyping().queue();
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
                total + " Missing attachments",
                null,
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
            @Override
            public void handleReaction(MessageReaction reaction, User user, Guild guild) {
                long id = reaction.getMessageIdLong();
                Emote emote = reaction.getReactionEmote().getEmote();
                if(!matchMessages.containsKey(id) || (emote != stats && emote != players && emote != loadouts)) {
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
                    if(content == null) {
                        return;
                    }
                    message.editMessage(content).queue();
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
        if(matchStats.hasLoadouts()) {
            matchStats.setLoadoutImage(buildLoadoutImage(matchStats.getLoadouts()));
        }
        MessageEmbed matchEmbed = buildMatchEmbed(matchStats);
        channel.sendMessage(matchEmbed)
                /*
                 * Setting the attached loadout image file as the embed footer icon prevents it from displaying as
                 * a separate message.
                 * When viewing the loadout embed, both the embed footer icon & embed image can use the file.
                 */
                .addFile(matchStats.getLoadoutImage(), "image.png")
                .queue(message -> {
                    matchMessages.put(message.getIdLong(), matchStats);
                    message.addReaction(stats).queue();
                    message.addReaction(players).queue();
                    if(matchStats.hasLoadouts()) {
                        message.addReaction(loadouts).queue();
                    }
                });
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
        return getDefaultMatchEmbedBuilder(matchStats)
                .addField("**Date**", matchStats.getDateString(), true)
                .addField("**Time**", matchStats.getTimeString(), true)
                .addField("**Duration**", matchStats.getDurationString(), true)
                .addField("**Mode**", matchStats.getMode().getName(), true)
                .addField("**Map**", matchStats.getMap().getName(), true)
                .addBlankField(true)
                .addField("**K/D**", matchStats.getKillDeathSummary(), true)
                .addField("**Shots Fired/Hit**", matchStats.getShotSummary(), true)
                .addField("**Accuracy**", matchStats.getAccuracySummary(), true)
                .addField("**Damage Dealt**", matchStats.getDamageDealt(), true)
                .addField("**Damage Taken**", matchStats.getDamageReceived(), true)
                .addField("**Highest Streak**", String.valueOf(matchStats.getLongestStreak()), true)
                .addField(
                        "**Distance Travelled**",
                        matchStats.getWobblies() + "\n" + matchStats.getDistanceTravelled(),
                        true
                )
                .addField("**Time Spent Moving**", matchStats.getPercentTimeMovingString(), true)
                .addBlankField(true)
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
                .setImage("attachment://image.png").build();
    }

    /**
     * Add the given team to the embed builder
     *
     * @param team    Team to add to embed builder
     * @param builder Embed builder with team name and players
     */
    private void addTeamToEmbed(Team team, EmbedBuilder builder) {
        ArrayList<MatchPlayer> players = team.getPlayers();
        for(int i = 0; i < players.size(); i++) {
            MatchPlayer player = players.get(i);
            String value = "**" + player.getName() + "**"
                    + "\n" + player.getPlatform().name()
                    + "\n#" + player.getUno();
            builder.addField(i == 0
                    ? EmbedHelper.getTitleField("__" + team.getName().toUpperCase() + "__", value)
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
        return getDefaultEmbedBuilder(name.toUpperCase())
                .setColor(EmbedHelper.RED)
                .setDescription(error)
                .build();
    }

    /**
     * Create the default embed builder
     *
     * @param name Player name
     * @return Default embed builder
     */
    private EmbedBuilder getDefaultEmbedBuilder(String name) {
        return new EmbedBuilder()
                .setTitle(getSummaryEmbedTitle(name))
                .setFooter(
                        "Type " + getTrigger() + " for help", "attachment://image.png"
                )
                .setThumbnail(getEmbedThumbnail());
    }

    /**
     * Create the default embed builder for a match
     *
     * @param matchStats Match
     * @return Default embed builder for a match
     */
    private EmbedBuilder getDefaultMatchEmbedBuilder(MatchStats matchStats) {
        return getDefaultEmbedBuilder(matchStats.getPlayer().getName().toUpperCase())
                .setColor(getResultColour(matchStats.getResult()))
                .setImage(matchStats.getMap().getImageURL());
    }

    /**
     * Get the title to use for the match summary embed
     *
     * @param name Player name
     * @return Embed title
     */
    public abstract String getSummaryEmbedTitle(String name);

    /**
     * Get the title to use for the match history embed
     *
     * @param name Player name
     * @return Embed title
     */
    public abstract String getHistoryEmbedTitle(String name);

    /**
     * Get the thumbnail to use in the embed
     *
     * @return Embed thumbnail
     */
    public abstract String getEmbedThumbnail();

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

        for(int i = 0; i < matchList.length(); i++) {
            JSONObject match = matchList.getJSONObject(i);
            JSONObject playerStats = match.getJSONObject("playerStats");
            JSONObject playerSummary = match.getJSONObject("player");
            String mapName = match.getString("map");
            MatchStats.RESULT result = parseResult(
                    (!match.getBoolean("isPresentAtEnd") || match.isNull("result")) ? "FORFEIT" : match.getString("result")
            );

            matchStats.add(
                    new MatchStats.MatchBuilder(
                            match.getString("matchID"),
                            DiscordCommandManager.codManager.getMapByCodename(mapName),
                            DiscordCommandManager.codManager.getModeByCodename(match.getString("mode")),
                            new Date(match.getLong("utcStartSeconds") * 1000),
                            new Date(match.getLong("utcEndSeconds") * 1000),
                            result,
                            player
                    )
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
                            .setPercentTimeMoving(playerStats.getDouble("percentTimeMoving"))
                            .setNemesis(getOptionalString(playerSummary, "nemesis"))
                            .setMostKilled(getOptionalString(playerSummary, "mostKilled"))
                            .setLongestStreak(getLongestStreak(playerStats))
                            .setDamageDealt(getDamageDealt(playerStats))
                            .setDamageReceived(getOptionalInt(playerStats, "damageTaken"))
                            .setXP(getOptionalInt(playerStats, "matchXp"))
                            .setDistanceTravelled(getOptionalInt(playerStats, "distanceTraveled"))
                            .setLoadouts(parseLoadouts(playerSummary))
                            .build()
            );
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
                            .setLethalEquipment(parseWeapon(loadoutData.getJSONObject("lethal")))
                            .setTacticalEquipment((TacticalWeapon) parseWeapon(loadoutData.getJSONObject("tactical")))
                            .setPerks(parsePerks(loadoutData.getJSONArray("perks")))
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
        Perk[] perks = new Perk[perkJSONArray.length()];
        for(int i = 0; i < perkJSONArray.length(); i++) {
            perks[i] = DiscordCommandManager.codManager.getPerkByCodename(
                    perkJSONArray.getJSONObject(i).getString("name")
            );
        }
        return perks;
    }

    /**
     * Parse a weapon from the match loadout weapon JSON
     *
     * @param loadoutWeapon Match loadout weapon JSON
     * @return Weapon
     */
    private Weapon parseWeapon(JSONObject loadoutWeapon) {
        return DiscordCommandManager.codManager.getWeaponByCodename(loadoutWeapon.getString("name"));
    }

    /**
     * Parse a loadout weapon from the match loadout weapon JSON
     *
     * @param loadoutWeapon Match loadout weapon JSON
     * @return Loadout weapon containing weapon & attachments
     */
    private LoadoutWeapon parseLoadoutWeapon(JSONObject loadoutWeapon) {
        Weapon weapon = parseWeapon(loadoutWeapon);
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
                            null
                    );
                }
                attachments.add(attachment);
            }
        }
        return new LoadoutWeapon(weapon, attachments);
    }

    /**
     * Get the match history JSON
     *
     * @param name     Player name
     * @param platform Player platform
     * @return Match history JSON
     */
    public abstract String getMatchHistoryJSON(String name, PLATFORM platform);

    /**
     * Get the match players JSON
     *
     * @param matchID  Match id
     * @param platform Match platform
     * @return Match players JSON
     */
    public abstract String getMatchPlayersJSON(String matchID, PLATFORM platform);

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
