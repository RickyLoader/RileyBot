package Command.Structure;

import org.json.JSONObject;


public class LinkCommand extends DiscordCommand {

    final String link;

    public LinkCommand(String trigger, String desc, String helpname) {
        super(trigger, desc, helpname);
        this.link = parseJSON();
    }

    public LinkCommand(String trigger, String desc) {
        super(trigger, desc);
        this.link = parseJSON();
    }

    @Override
    public void execute(CommandContext context) {
        context.getMessageChannel().sendMessage(link).queue();
    }

    private String parseJSON() {
        String link = null;
        System.out.println("Loading " + getTrigger() + "...");
        try {
            JSONObject o = readJSONFile("links.json").getJSONObject(getTrigger());
            link = o.getString("link");
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        return link;
    }
}
