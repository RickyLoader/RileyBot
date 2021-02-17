package Command.Commands.Lookup;

import Bot.DiscordUser;
import Command.Structure.CommandContext;
import Command.Structure.EmbedHelper;
import Command.Structure.LookupCommand;
import Command.Structure.PageableTableEmbed;
import Countdown.Countdown;
import Network.NetworkRequest;
import Network.Secret;
import Twitch.Game;
import Twitch.OAuth;
import Twitch.Stream;
import Twitch.Streamer;
import Twitch.Streamer.StreamerBuilder;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;
import org.json.JSONArray;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Look up a Twitch.tv streamer!
 */
public class TTVLookupCommand extends LookupCommand {
    private final String
            baseURL = "https://api.twitch.tv/helix/",
            twitchUrl = "https://www.twitch.tv/";
    private final HashMap<String, Game> games = new HashMap<>();
    private final OAuth oAuth;
    private final String footer;

    public TTVLookupCommand() {
        super("ttvlookup", "Look up a Twitch.tv streamer!", 50);
        this.oAuth = new OAuth(Secret.TWITCH_CLIENT_ID, Secret.TWITCH_CLIENT_SECRET);
        this.footer = "Type: " + getTrigger() + " for help";
        setBotInput(true);
    }

    @Override
    public void processName(String name, CommandContext context) {
        MessageChannel channel = context.getMessageChannel();
        Member member = context.getMember();
        User user = member.getUser();
        if(user.isBot() && member != context.getGuild().getSelfMember()) {
            return;
        }
        if(name.startsWith(twitchUrl)) {
            showTwitchDetailsEmbed(name, context.getMessage(), channel);
            return;
        }
        channel.sendTyping().queue();
        ArrayList<Streamer> streamers = searchStreamers(name);
        if(streamers.isEmpty()) {
            channel.sendMessage(
                    member.getAsMention() + " I didn't find any streamers matching: **" + name + "**"
            ).queue();
            return;
        }
        if(streamers.size() == 1) {
            channel.sendMessage(buildStreamerEmbed(streamers.get(0), false)).queue();
            return;
        }
        showSearchResults(name, streamers, context);
    }

    /**
     * Create and send a message embed when a Twitch.tv URL is posted and discord creates an embed for it.
     * Discord's embed only shows the streamer name and URL, delete and display an embed with
     * followers, current stream info, etc.
     *
     * @param streamerUrl URL to streamer Twitch.tv page
     * @param message     Message containing twitch URL
     * @param channel     Channel to send better message embed to
     */
    private void showTwitchDetailsEmbed(String streamerUrl, Message message, MessageChannel channel) {
        List<MessageEmbed> embeds = message.getEmbeds();
        if(embeds.isEmpty()) {
            return;
        }
        String name = streamerUrl
                .replace(twitchUrl, "")
                .replace("/", "")
                .trim();
        ArrayList<Streamer> streamers = searchStreamers(name);
        if(streamers.size() != 1) {
            return;
        }
        message.delete().queue(
                deleted -> channel.sendMessage(buildStreamerEmbed(streamers.get(0), true)).queue()
        );
    }

    /**
     * Show a pageable message embed displaying the streamer search results for the given query
     *
     * @param query     Search query used to find the results
     * @param streamers List of streamers found via query
     * @param context   Command context
     */
    private void showSearchResults(String query, ArrayList<Streamer> streamers, CommandContext context) {
        new PageableTableEmbed(
                context,
                streamers,
                "https://i.imgur.com/w1zOkVd.png",
                "TTVLookup: " + streamers.size() + " results found",
                "**Query**: " + query,
                footer,
                new String[]{"Name", "Category", "URL"},
                5
        ) {
            @Override
            public String[] getRowValues(int index, List<?> items, boolean defaultSort) {
                Streamer streamer = (Streamer) items.get(index);
                return new String[]{
                        streamer.getLoginName(),
                        streamer.isStreaming()
                                ? streamer.getStream().getGame().getName()
                                : "OFFLINE",
                        EmbedHelper.embedURL("Visit", streamer.getUrl())
                };
            }

            @Override
            public void sortItems(List<?> items, boolean defaultSort) {
                items.sort((Comparator<Object>) (o1, o2) -> {
                    String t1 = ((Streamer) o1).getLoginName();
                    String t2 = ((Streamer) o2).getLoginName();
                    if(defaultSort) {
                        return t1.compareTo(t2);
                    }
                    return t2.compareTo(t1);
                });

            }
        }.showMessage();
    }

    /**
     * Build a message embed detailing the given Twitch streamer
     *
     * @param streamer Twitch streamer to create message embed for
     * @param replace  Replacing discord Twitch embed (show author)
     * @return Message embed detailing the given Twitch streamer
     */
    private MessageEmbed buildStreamerEmbed(Streamer streamer, boolean replace) {
        boolean live = streamer.isStreaming();
        String footer = this.footer;
        String description = "**Followers**: " + streamer.formatFollowers();
        String title = streamer.getDisplayName() + " | " + (live ? "LIVE" : "OFFLINE");

        if(streamer.hasLanguage() && !streamer.getLanguage().equalsIgnoreCase("english")) {
            title += " (" + streamer.getLanguage() + ")";
        }
        EmbedBuilder builder = new EmbedBuilder()
                .setThumbnail(streamer.getThumbnail())
                .setTitle(
                        title,
                        streamer.getUrl()
                )
                .setColor(live ? EmbedHelper.GREEN : EmbedHelper.RED);

        if(live) {
            Stream stream = streamer.getStream();
            Game game = stream.getGame();
            String started = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(stream.getStarted());
            footer += " | Online since: " + started;
            Countdown streamDuration = Countdown.from(stream.getStarted().getTime(), System.currentTimeMillis());
            builder.setImage(game.getThumbnail());
            description = "__**Current Stream Details**__\n\n"
                    + "**Title**: " + stream.getTitle()
                    + "\n\n**Streaming**: " + game.getName()
                    + "\n**Online for**: "
                    + streamDuration.formatHoursMinutesSeconds()
                    + "\n**Viewers**: " + stream.formatViewers()
                    + "\n\n" + description;
        }
        if(replace) {
            builder.setAuthor("Twitch");
        }
        return builder
                .setFooter(footer)
                .setDescription(description)
                .build();
    }

    /**
     * Search Twitch for the given query. Return all channels found.
     *
     * @param query Query to search for
     * @return List of Twitch streamer results
     */
    private ArrayList<Streamer> searchStreamers(String query) {
        JSONArray results = getStreamerSearchResults(query);
        ArrayList<Streamer> streamers = new ArrayList<>();
        for(int i = 0; i < results.length(); i++) {
            JSONObject streamerData = results.getJSONObject(i);
            streamers.add(
                    parseStreamer(
                            streamerData,
                            streamerData.getString("broadcaster_login").equalsIgnoreCase(query)
                    )
            );
        }
        ArrayList<Streamer> matching = streamers
                .stream()
                .filter(s -> s.getLoginName().equalsIgnoreCase(query))
                .collect(Collectors.toCollection(ArrayList::new));
        return matching.isEmpty() ? streamers : matching;
    }

    /**
     * Parse a streamer JSONObject to a Streamer
     *
     * @param streamer      Streamer JSON
     * @param showFollowers Make an extra request to retrieve streamer followers
     * @return Streamer
     */
    private Streamer parseStreamer(JSONObject streamer, boolean showFollowers) {
        String id = streamer.getString("id");
        StreamerBuilder builder = new StreamerBuilder()
                .setLoginName(streamer.getString("broadcaster_login"))
                .setDisplayName(streamer.getString("display_name"))
                .setId(id)
                .setThumbnail(streamer.getString("thumbnail_url"));

        String langISO = streamer.getString("broadcaster_language");
        if(!langISO.isEmpty()) {
            builder.setLanguage(EmbedHelper.getLanguageFromISO(langISO));
        }
        if(streamer.getBoolean("is_live")) {
            builder.setStream(
                    new Stream(
                            streamer.getString("title"),
                            fetchGame(streamer.getString("game_id")),
                            parseDate(streamer.getString("started_at")),
                            fetchViewers(id)
                    )
            );
        }
        if(showFollowers) {
            builder.setFollowers(fetchFollowers(id));
        }
        return builder.build();
    }

    @Override
    public String stripArguments(String query) {
        if(query.startsWith(twitchUrl)) {
            query = getTrigger() + " " + query;
        }
        return query;
    }

    /**
     * Fetch the total followers for the given Twitch channel id
     *
     * @param id Twitch channel id
     * @return Total followers
     */
    private int fetchFollowers(String id) {
        JSONObject response = new JSONObject(
                new NetworkRequest("https://api.twitch.tv/helix/users/follows?to_id=" + id, false)
                        .get(getAuthHeaders())
                        .body
        );
        return response.getInt("total");
    }

    /**
     * Parse a Twitch.tv stream date from a String
     *
     * @param dateString Date String
     * @return Date of String
     */
    private Date parseDate(String dateString) {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
            dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            return dateFormat.parse(dateString);
        }
        catch(ParseException e) {
            return new Date();
        }
    }

    /**
     * Fetch the number of stream viewers via the streamer's unique id
     *
     * @param id Streamer id
     * @return Number of viewers watching streamer's current stream
     */
    private int fetchViewers(String id) {
        JSONObject response = new JSONObject(
                new NetworkRequest(baseURL + "streams?user_id=" + id, false).get(getAuthHeaders()).body
        ).getJSONArray("data").getJSONObject(0);
        return response.getInt("viewer_count");
    }

    /**
     * Fetch a game via the unique id and parse in to a Game object
     *
     * @param gameId Unique id of game
     * @return Game object
     */
    private Game fetchGame(String gameId) {
        if(games.containsKey(gameId)) {
            return games.get(gameId);
        }
        String url = baseURL + "games?id=" + gameId;
        JSONObject gameData = new JSONObject(
                new NetworkRequest(url, false).get(getAuthHeaders()).body
        ).getJSONArray("data").getJSONObject(0);
        Game game = new Game(
                gameData.getString("name"),
                gameData.getString("id"),
                gameData.getString("box_art_url")
                        .replace("-{width}x{height}", "")
                        .replace("/./", "/")
        );
        games.put(game.getId(), game);
        return game;
    }

    /**
     * Get streamer search results for the given query
     *
     * @param query Query to search for in streamers
     * @return JSONArray search results
     */
    private JSONArray getStreamerSearchResults(String query) {
        String url = baseURL + "search/channels?first=20&query=" + query;
        return new JSONObject(
                new NetworkRequest(url, false).get(getAuthHeaders()).body
        ).getJSONArray("data");
    }

    /**
     * Get the auth headers required to make requests to Twitch.tv
     *
     * @return Map of header name -> header value
     */
    private HashMap<String, String> getAuthHeaders() {
        HashMap<String, String> headers = new HashMap<>();
        headers.put("Client-Id", oAuth.getClientId());
        headers.put("Authorization", "Bearer " + oAuth.getAccessToken());
        return headers;
    }

    @Override
    public String getSavedName(long id) {
        return DiscordUser.getSavedName(id, DiscordUser.TTV);
    }

    @Override
    public void saveName(String name, MessageChannel channel, User user) {
        DiscordUser.saveName(name, DiscordUser.TTV, channel, user);
    }

    @Override
    public boolean matches(String query, Message message) {
        String regex = twitchUrl + "(\\w)+/?";
        return super.matches(query, message) || query.matches(regex);
    }
}
