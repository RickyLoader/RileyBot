package Command.Commands;

import Command.Structure.CommandContext;
import Command.Structure.DiscordCommand;
import Command.Structure.EmbedHelper;
import Network.NetworkRequest;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

public class GIFCommand extends DiscordCommand {
    private final HashMap<String, ArrayList<GIF>> gifs = new HashMap<>();
    private final HashSet<String> currentQueries = new HashSet<>();
    private final Random random = new Random();

    public GIFCommand() {
        super("gif", "Search for a random GIF!", "gif [search term]");
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
                channel.sendMessage("I don't have any " + query + " gifs at the moment, let me get some!").queue();
                queryGifs = searchGIFs(query);
                gifs.put(query, queryGifs);
            }
            if(queryGifs.isEmpty()) {
                channel.sendMessage(member.getAsMention() + " I didn't find anything for: " + query).queue();
                return;
            }
            channel.sendMessage(
                    buildGifEmbed(
                            queryGifs.get(random.nextInt(queryGifs.size())),
                            query,
                            queryGifs.size()
                    )
            ).queue();
        }).start();
    }

    /**
     * Create a message embed displaying the given GIF
     *
     * @param gif     GIF to build embed for
     * @param query   Search query used to find GIF
     * @param results Total number of results for the query
     * @return Message embed displaying GIF
     */
    private MessageEmbed buildGifEmbed(GIF gif, String query, int results) {
        String thumbnail = "https://i.imgur.com/bTDNO9G.jpg";
        String description = "Search term: **" + query + "**" + "\nResults: **" + results + "**";
        if(gif.hasTags()) {
            description += "\nTags: "
                    + Arrays.stream(gif.getTags()).collect(Collectors.joining(", ", "**", "**"));
        }
        description += "\n\nSometimes they don't load, click the title if you *really* want to see it.";
        return new EmbedBuilder()
                .setTitle(gif.getTitle(), gif.getUrl())
                .setImage(gif.getUrl())
                .setDescription(description)
                .setColor(EmbedHelper.PURPLE)
                .setThumbnail(thumbnail)
                .setFooter(
                        "Uploaded: " + new SimpleDateFormat("dd/MM/yyyy").format(gif.getUploadDate())
                                + " | Try: " + getHelpName(), thumbnail
                )
                .build();
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
            String url = "https://api.redgifs.com/v1/gfycats/search?search_text="
                    + URLEncoder.encode(query, "UTF-8")
                    + "&count=50";

            JSONArray results = new JSONObject(
                    new NetworkRequest(url, false).get().body
            ).getJSONArray("gfycats");

            for(int i = 0; i < results.length(); i++) {
                JSONObject o = results.getJSONObject(i);
                if(o.isNull("title")) {
                    continue;
                }
                gifs.add(
                        new GIF(
                                o.getString("gifUrl"),
                                o.getString("title"),
                                parseTags(o.getJSONArray("tags")),
                                new Date(o.getLong("createDate") * 1000)
                        )
                );
            }
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        currentQueries.remove(query);
        return gifs;
    }

    /**
     * Parse the list of GIF category tags from the given JSONArray
     * and return an array containing the first 5 tags.
     *
     * @param tagJSON JSONArray of tags, may be empty
     * @return Array of first 5 tags
     */
    private String[] parseTags(JSONArray tagJSON) {
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
    private static class GIF {
        private final String url, title;
        private final String[] tags;
        private final Date uploadDate;

        /**
         * Create a GIF
         *
         * @param url        URL to GIF
         * @param title      Title of GIF
         * @param tags       Category tags of GIF
         * @param uploadDate Date of upload
         */
        public GIF(String url, String title, String[] tags, Date uploadDate) {
            this.url = url;
            this.title = title;
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
         * Get the title of the GIF
         *
         * @return GIF title
         */
        public String getTitle() {
            return title;
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
