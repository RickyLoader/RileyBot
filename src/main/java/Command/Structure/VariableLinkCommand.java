package Command.Structure;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.HashMap;

public class VariableLinkCommand extends DiscordCommand {
    private final HashMap<String, String> versions = new HashMap<>();

    public VariableLinkCommand(String[] variations, String desc) {
        super(StringUtils.join(variations, "\n"), desc);
        parseJSON(variations);
    }

    private void parseJSON(String[] variations) {
        System.out.println("Loading " + Arrays.toString(variations) + "...");
        try {
            JSONObject o = readJSONFile("links.json");
            for(String trigger : variations) {
                JSONObject command = o.getJSONObject(trigger);
                versions.put(trigger, command.getString("link"));
            }
        }
        catch(Exception e) {
            e.printStackTrace();
        }
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
