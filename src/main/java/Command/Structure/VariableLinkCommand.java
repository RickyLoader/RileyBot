package Command.Structure;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

import java.util.HashMap;

public class VariableLinkCommand extends DiscordCommand {
    private final HashMap<String, String> versions;

    public VariableLinkCommand(String[] variations, String desc) {
        super(StringUtils.join(variations, "\n"), desc);
        this.versions = parseJSON(variations);
    }

    private HashMap<String, String> parseJSON(String[] variations) {
        HashMap<String, String> versions = new HashMap<>();
        JSONObject o = readJSONFile("/Commands/links.json");
        if(o == null) {
            return versions;
        }
        for(String trigger : variations) {
            JSONObject command = o.getJSONObject(trigger);
            versions.put(trigger, command.getString("link"));
        }
        return versions;
    }

    @Override
    public void execute(CommandContext context) {
        context.getMessageChannel().sendMessage(versions.get(context.getLowerCaseMessage())).queue();
    }

    @Override
    public boolean matches(String query) {
        return versions.containsKey(query);
    }
}
