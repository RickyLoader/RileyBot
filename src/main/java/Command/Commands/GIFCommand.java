package Command.Commands;

import Command.Structure.CommandContext;
import Command.Structure.CyclicalPageableEmbed;
import Command.Structure.DiscordCommand;
import Command.Structure.EmbedHelper;
import Network.NetworkRequest;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * View cool GIFs
 */
public class GIFCommand extends DiscordCommand {
    private final HashMap<String, ArrayList<GIF>> gifs = new HashMap<>();
    private final HashSet<String> currentQueries = new HashSet<>();
    private final static String BASE_URL = "https://api.redgifs.com/v1/gfycats/";
    private final String thumbnail = "https://i.imgur.com/bTDNO9G.jpg", helpText;

    public GIFCommand() {
        super("gif", "Search for a random GIF!", "gif [search term]");
        this.helpText = "Try: " + getHelpName();
    }

    /**
     * Search for and send a random GIF, remember the previous queries for faster lookup
     *
     * @param context Command context
     */
    @Override
    public void execute(CommandContext context) {
        String query = context.getLowerCaseMessage().replaceFirst(getTrigger(), "").trim();
        MessageChannel channel = context.getMessageChannel();
        Member member = context.getMember();

        if(currentQueries.contains(query)) {
            channel.sendMessage(member.getAsMention() + " I'm still searching for **" + query + "**").queue();
            return;
        }

        if(query.isEmpty()) {
            channel.sendMessage(getHelpNameCoded()).queue();
            return;
        }
        new Thread(() -> {
            ArrayList<GIF> queryGifs = gifs.get(query);
            if(queryGifs == null) {
                channel.sendTyping().queue();
                queryGifs = searchGIFs(query);
                gifs.put(query, queryGifs);
            }
            if(queryGifs.isEmpty()) {
                channel.sendMessage(
                        member.getAsMention() + " I didn't find anything for: **" + query + "**"
                ).queue();
                return;
            }
            showGifs(context, query, queryGifs);
        }).start();
    }

    /**
     * Display the given list of GIFs in a pageable message embed
     *
     * @param context Command context
     * @param query   Query used to find GIFs
     * @param gifs    List of GIFS found for query
     */
    private void showGifs(CommandContext context, String query, ArrayList<GIF> gifs) {
        new CyclicalPageableEmbed<GIF>(
                context,
                gifs,
                1
        ) {

            @Override
            public EmbedBuilder getEmbedBuilder(String pageDetails) {
                return new EmbedBuilder()
                        .setColor(EmbedHelper.PURPLE)
                        .setThumbnail(thumbnail);
            }

            @Override
            public void displayItem(EmbedBuilder builder, int currentIndex, GIF gif) {
                if(gif.hasTags()) {
                    builder.setDescription(
                            "Tags: "
                                    + Arrays
                                    .stream(gif.getTags())
                                    .collect(Collectors.joining(", ", "**", "**"))
                    );
                }
                builder
                        .setTitle("GIF Search | " + query + " | " + (currentIndex + 1) + "/" + getItems().size())
                        .setImage(gif.getUrl())
                        .setFooter(
                                "Uploaded: "
                                        + new SimpleDateFormat("dd/MM/yyyy").format(gif.getUploadDate())
                                        + " | " + helpText,
                                thumbnail
                        );
            }

            @Override
            protected MessageEmbed getNoItemsEmbed() {
                return getEmbedBuilder(helpText).setDescription("There's nothing here!").build();
            }

            @Override
            public boolean nonPagingButtonPressed(String buttonId) {
                return false;
            }
        }.showMessage();
    }

    /**
     * Search for GIFs related to the given query and save them
     *
     * @param query Search query
     * @return Array of GIFs
     */
    private ArrayList<GIF> searchGIFs(String query) {
        currentQueries.add(query);
        ArrayList<GIF> gifs = new ArrayList<>();
        try {
            String url = BASE_URL + "search?search_text="
                    + EmbedHelper.urlEncode(query)
                    + "&count=50";
            JSONArray results = new JSONObject(
                    new NetworkRequest(url, false).get().body
            ).getJSONArray("gfycats");

            for(int i = 0; i < results.length(); i++) {
                gifs.add(parseGif(results.getJSONObject(i)));
            }
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        currentQueries.remove(query);
        return gifs;
    }

    /**
     * Get a GIF by its unique id
     *
     * @param id    ID of gif
     * @param adult Search on RedGIF vs Gfycat
     * @return GIF
     */
    public static GIF getGifById(String id, boolean adult) {
        try {
            JSONObject gif = new JSONObject(
                    new NetworkRequest(
                            (adult ? BASE_URL : "https://api.gfycat.com/v1/gfycats/") + id, false
                    ).get().body
            ).getJSONObject("gfyItem");
            return parseGif(gif);
        }
        catch(Exception e) {
            return null;
        }
    }

    /**
     * Check if the given URL is a Gfycat or RedGIFs URL
     *
     * @param url URL to check
     * @return URL is Gfycat/RedGIFs URL
     */
    public static boolean isGifUrl(String url) {
        return isRedGifsUrl(url) || isGfycatUrl(url);

    }

    /**
     * Check if the given URL is a RedGIFs URL
     *
     * @param url URL to check
     * @return URL is RedGIFs URL
     */
    private static boolean isRedGifsUrl(String url) {
        return url.matches("https?://(www\\.)?redgifs.com/watch/.+");
    }

    /**
     * Check if the given URL is a Gfycat URL
     *
     * @param url URL to check
     * @return URL is Gfycat URL
     */
    private static boolean isGfycatUrl(String url) {
        return url.matches("https://(www\\.)?gfycat.com/.+");
    }

    /**
     * Get a Gfycat or RedGIFs GIF by URL.
     *
     * @param url URL to GIF
     * @return GIF or null (if unable to retrieve)
     */
    @Nullable
    public static GIF getGifByUrl(String url) {

        // Invalid URL
        if(!isGifUrl(url)){
            return null;
        }

        // Remove trailing slash
        if(url.endsWith("/")) {
            url = url.substring(0, url.length() - 1);
        }

        String[] urlArgs = url.split("/");
        final String id = urlArgs[urlArgs.length - 1];

        return getGifById(id, isRedGifsUrl(url));
    }

    /**
     * Parse a GIF from the given JSON
     *
     * @param gif Gif JSON
     * @return GIF
     */
    private static GIF parseGif(JSONObject gif) {
        return new GIF(
                gif.getString("gifUrl"),
                parseTags(gif.getJSONArray("tags")),
                new Date(gif.getLong("createDate") * 1000)
        );
    }

    /**
     * Parse the list of GIF category tags from the given JSONArray
     * and return an array containing the first 5 tags.
     *
     * @param tagJSON JSONArray of tags, may be empty
     * @return Array of first 5 tags
     */
    private static String[] parseTags(JSONArray tagJSON) {
        int size = Math.min(5, tagJSON.length());
        String[] tags = new String[size];
        for(int i = 0; i < size; i++) {
            tags[i] = tagJSON.getString(i);
        }
        return tags;
    }

    @Override
    public boolean matches(String query, Message message) {
        return query.startsWith(getTrigger());
    }

    /**
     * Hold a GIF search result
     */
    public static class GIF {
        private final String url;
        private final String[] tags;
        private final Date uploadDate;

        /**
         * Create a GIF
         *
         * @param url        URL to GIF
         * @param tags       Category tags of GIF
         * @param uploadDate Date of upload
         */
        public GIF(String url, String[] tags, Date uploadDate) {
            this.url = url;
            this.tags = tags;
            this.uploadDate = uploadDate;
        }

        /**
         * Get the date that the GIF was uploaded
         *
         * @return Upload date
         */
        public Date getUploadDate() {
            return uploadDate;
        }

        /**
         * Get the category tags of the GIF
         *
         * @return Category tags
         */
        public String[] getTags() {
            return tags;
        }

        /**
         * Get the URL to the GIF
         *
         * @return URL to GIF
         */
        public String getUrl() {
            return url;
        }

        /**
         * Check if the GIF has any category tags
         *
         * @return GIF has category tags
         */
        public boolean hasTags() {
            return tags.length > 0;
        }
    }
}
