package Command.Structure;

import Network.ApiRequest;
import org.json.JSONObject;


public class LinkCommand extends DiscordCommand {

    String link;

    public LinkCommand(String trigger, String desc, String helpname, String json) {
        super(trigger, desc, helpname);
        this.link = parseJSON(json);
    }

    public LinkCommand(String trigger, String desc, String json) {
        super(trigger, desc);
        this.link = parseJSON(json);
    }

    public LinkCommand(String trigger, String desc) {
        super(trigger, desc);
    }

    @Override
    public void execute(CommandContext context) {
        context.getMessageChannel().sendMessage(link).queue();
    }

    private String parseJSON(String json) {
        String link = null;
        try {
            JSONObject o = new JSONObject(json).getJSONObject(String.valueOf(getTrigger()));
            link = o.getString("link");
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        return link;
    }

    public static String fetchLinks() {
        System.out.println("Fetching links...\n");
        return ApiRequest.executeQuery("commands/link", "GET", null, true);
    }
}
