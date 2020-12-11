package Command.Commands;

import Command.Structure.CommandContext;
import Command.Structure.DiscordCommand;
import Command.Structure.EmbedHelper;
import Network.NetworkRequest;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class GIFCommand extends DiscordCommand {
    private final HashMap<String, ArrayList<GIF>> gifs = new HashMap<>();
    private final ArrayList<String> queries = new ArrayList<>();

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
        if(queries.contains(query)) {
            return;
        }
        if(query.isEmpty()) {
            channel.sendMessage(getHelpNameCoded()).queue();
            return;
        }
        ArrayList<GIF> queryGifs = gifs.get(query);
        if(queryGifs == null) {
            channel.sendMessage("I don't have any " + query + " gifs at the moment, let me get some!").queue();
            queryGifs = searchGIFs(query);
            gifs.put(query, queryGifs);
        }
        queries.remove(query);
        if(queryGifs.isEmpty()) {
            channel.sendMessage("I didn't find anything for: " + query).queue();
            return;
        }
        channel.sendMessage(chooseGif(queryGifs, context.getSelfUser().getEffectiveAvatarUrl(), query)).queue();
    }

    /**
     * Choose a random gif and wrap it in a message embed
     *
     * @param gifs List of gifs
     * @return Gif wrapped in message embed
     */
    private MessageEmbed chooseGif(ArrayList<GIF> gifs, String thumbnail, String query) {
        EmbedBuilder builder = new EmbedBuilder();
        GIF gif = gifs.get(new Random().nextInt(gifs.size()));
        builder.setTitle(gif.getTitle(), gif.getUrl());
        builder.setImage(gif.getUrl());
        builder.setDescription("Pulled from search term: **" + query + "**\n\nSometimes they don't load, click the title if you *really* want to see it.");
        builder.setColor(EmbedHelper.PURPLE);
        builder.setThumbnail(thumbnail);
        builder.setFooter("Try: " + getHelpName(), gif.getWebsiteIcon());
        return builder.build();
    }

    /**
     * Search for GIFs related to the given query and save them
     *
     * @param query Search query
     * @return Array of GIFs
     */
    private ArrayList<GIF> searchGIFs(String query) {
        ArrayList<GIF> gifs = new ArrayList<>();
        try {
            String url = "https://api.redgifs.com/v1/gfycats/search?search_text="
                    + URLEncoder.encode(query, "UTF-8")
                    + "&count=50";
            String json = new NetworkRequest(url, false).get().body;
            JSONArray results = new JSONObject(json).getJSONArray("gfycats");
            for(int i = 0; i < results.length(); i++) {
                JSONObject o = results.getJSONObject(i);
                gifs.add(
                        new GIF(
                                o.getString("gifUrl"),
                                o.getString("title"),
                                o.getString("posterUrl")
                        )
                );
            }
            return gifs;
        }
        catch(Exception e) {
            return gifs;
        }
    }

    @Override
    public boolean matches(String query) {
        return query.startsWith(getTrigger());
    }

    private static class GIF {
        private final String url, title, poster;

        public GIF(String url, String title, String poster) {
            this.url = url;
            this.title = title;
            this.poster = poster;
        }

        public String getWebsiteIcon() {
            return "https://i.imgur.com/bTDNO9G.jpg";
        }

        public String getPoster() {
            return poster;
        }

        public String getTitle() {
            return title;
        }

        public String getUrl() {
            return url;
        }
    }
}
