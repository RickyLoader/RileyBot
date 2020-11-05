package Command.Structure;

import org.json.JSONObject;


public class LinkCommand extends DiscordCommand {

    private final String link;

    public LinkCommand(String trigger, String desc, String helpName) {
        super(trigger, desc, helpName);
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
        JSONObject o = readJSONFile("/Commands/links.json");
        if(o == null) {
            return null;
        }
        return o.getJSONObject(getTrigger()).getString("link");
    }
}
