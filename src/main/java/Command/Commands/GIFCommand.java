package Command.Commands;

import Command.Structure.CommandContext;
import Command.Structure.DiscordCommand;
import Network.NetworkRequest;
import net.dv8tion.jda.api.entities.MessageChannel;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class GIFCommand extends DiscordCommand {
    private final HashMap<String, ArrayList<String>> gifs = new HashMap<>();
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
        ArrayList<String> queryGifs = gifs.get(query);
        if(queryGifs == null) {
            channel.sendMessage("I don't have any " + query + " gifs at the moment, let me check").queue();
            queryGifs = searchGIFs(query);
            gifs.put(query, queryGifs);
        }
        queries.remove(query);
        if(queryGifs.isEmpty()) {
            channel.sendMessage("I didn't find anything for: " + query).queue();
            return;
        }
        channel.sendMessage(queryGifs.get(new Random().nextInt(queryGifs.size()))).queue();
    }

    /**
     * Search for GIFs related to the given query and save them
     *
     * @param query Search query
     * @return Array of GIFs
     */
    private ArrayList<String> searchGIFs(String query) {
        ArrayList<String> gifs = new ArrayList<>();
        try {
            String url = "https://api.redgifs.com/v1/gfycats/search?search_text=" + URLEncoder.encode(query, "UTF-8") + "&count=50";
            String json = new NetworkRequest(url, false).get();
            JSONArray results = new JSONObject(json).getJSONArray("gfycats");
            for(int i = 0; i < results.length(); i++) {
                gifs.add(results.getJSONObject(i).getString("gifUrl"));
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
}
